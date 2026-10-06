/**
 * VPNGate free-server sync job.
 *
 * Pulls the public VPNGate server list (University of Tsukuba, no signup,
 * updated hourly) and upserts it into the `Servers` table as
 * provider='vpngate', type='free', protocol='OpenVPN'.
 *
 * Each row ships a ready-to-use OpenVPN config (base64) which is stored in
 * `config_file` — the Android app downloads it via GET /api/v1/servers/:id/config
 * and hands it to "OpenVPN for Android" through its external API.
 *
 * Usage:
 *   node jobs/sync-vpngate.js            # one-shot (cron-friendly)
 *   POST /api/v1/admin/vpngate-sync      # admin-triggered (see routes/admin.js)
 */
'use strict';

const https = require('https');
const http = require('http');

const SOURCES = [
  'https://www.vpngate.net/api/iphone/',
  'http://www.vpngate.net/api/iphone/',
];

/** Minimal CSV parser that honours quoted fields. */
function parseCsv(text) {
  const rows = [];
  let row = [], field = '', inQuotes = false;
  for (let i = 0; i < text.length; i++) {
    const c = text[i];
    if (inQuotes) {
      if (c === '"') {
        if (text[i + 1] === '"') { field += '"'; i++; }
        else inQuotes = false;
      } else field += c;
    } else if (c === '"') inQuotes = true;
    else if (c === ',') { row.push(field); field = ''; }
    else if (c === '\n') { row.push(field); rows.push(row); row = []; field = ''; }
    else if (c === '\r') { /* skip */ }
    else field += c;
  }
  if (field !== '' || row.length) { row.push(field); rows.push(row); }
  return rows;
}

function fetchText(url, timeoutMs = 15000) {
  const lib = url.startsWith('https') ? https : http;
  return new Promise((resolve, reject) => {
    const req = lib.get(url, { headers: { 'User-Agent': 'lucky-vpn-sync/1.0' }, timeout: timeoutMs }, (res) => {
      if (res.statusCode >= 300 && res.statusCode < 400 && res.headers.location) {
        resolve(fetchText(res.headers.location, timeoutMs));
        return;
      }
      if (res.statusCode !== 200) {
        reject(new Error(`HTTP ${res.statusCode} from ${url}`));
        res.resume();
        return;
      }
      let data = '';
      res.setEncoding('utf8');
      res.on('data', (d) => { data += d; });
      res.on('end', () => resolve(data));
    });
    req.on('timeout', () => { req.destroy(new Error('timeout')); });
    req.on('error', reject);
  });
}

async function fetchVpnGateCsv() {
  let lastErr;
  for (const url of SOURCES) {
    try {
      const text = await fetchText(url);
      if (text && text.includes('OpenVPN_ConfigData_Base64')) return text;
      lastErr = new Error('unexpected body from ' + url);
    } catch (e) { lastErr = e; }
  }
  throw lastErr || new Error('could not fetch VPNGate list');
}

/** Parse the VPNGate CSV into server objects. Exported for tests. */
function parseVpnGateCsv(text) {
  const rows = parseCsv(text);
  const out = [];
  for (const r of rows) {
    if (!r.length) continue;
    if (r[0] === '*vpn_servers' || r[0].startsWith('#')) continue;
    if (r.length < 15 || !r[1]) continue;
    const [
      hostName, ip, _score, ping, speed, countryLong, countryShort,
      sessions, _uptime, _totalUsers, _totalTraffic, _logType, _operator,
      _message, ovpnB64,
    ] = r;
    let ovpn = '';
    try { ovpn = Buffer.from((ovpnB64 || '').trim(), 'base64').toString('utf8'); }
    catch { /* leave empty */ }
    if (!ovpn.includes('remote ')) continue;

    let port = '443';
    const m = ovpn.match(/^remote\s+\S+\s+(\d+)/m);
    if (m) port = m[1];
    const protoM = ovpn.match(/^proto\s+(tcp|udp)/m);

    out.push({
      name: hostName,
      ip,
      port,
      country: countryLong || countryShort || 'Unknown',
      city: '',
      protocol: 'OpenVPN',
      type: 'free',
      status: 'online',
      load: parseInt(sessions, 10) || 0,
      users: 0,
      provider: 'vpngate',
      config_file: ovpn,
      extra: {
        ping: parseInt(ping, 10) || 0,
        speed: parseInt(speed, 10) || 0,
        transport: protoM ? protoM[1] : 'tcp',
      },
    });
  }
  return out;
}

/** Upsert parsed servers into the DB. `db` = require('../models'). */
async function syncToDb(db, servers) {
  const { Server, sequelize } = db;

  // Make sure the provider ENUM accepts 'vpngate' on existing MySQL databases.
  // (sequelize.sync() without alter:true will not change the column.)
  // Skipped on SQLite, where Sequelize creates the column fresh from the model.
  if (sequelize.getDialect() === 'mysql') {
    try {
      await sequelize.query(
        "ALTER TABLE `Servers` MODIFY `provider` ENUM('manual','oneconnect','vpngate') NOT NULL DEFAULT 'manual'"
      );
    } catch (e) {
      // ER_DUP_FIELDNAME / syntax variants on already-migrated DBs — safe to ignore
      if (!/1060|1064|duplicate/i.test(e.message)) throw e;
    }
  }

  const seen = new Set();
  let added = 0, updated = 0;
  for (const s of servers) {
    seen.add(s.name);
    const { extra, ...fields } = s;
    const [row, created] = await Server.findOrCreate({
      where: { provider: 'vpngate', name: s.name },
      defaults: { ...fields, last_sync: new Date() },
    });
    if (created) { added++; continue; }
    await row.update({ ...fields, status: 'online', last_sync: new Date() });
    updated++;
  }

  // Servers that disappeared from the feed go offline (kept for history).
  const stale = await Server.findAll({ where: { provider: 'vpngate', status: 'online' } });
  let offlined = 0;
  for (const row of stale) {
    if (!seen.has(row.name)) { await row.update({ status: 'offline' }); offlined++; }
  }
  return { added, updated, offlined, total: servers.length };
}

async function main() {
  const text = await fetchVpnGateCsv();
  const servers = parseVpnGateCsv(text);
  console.log(`[vpngate] fetched ${servers.length} servers`);
  // Lazy-load models only when actually syncing to a DB.
  const db = require('../models');
  await db.sequelize.authenticate();
  const res = await syncToDb(db, servers);
  console.log('[vpngate] sync result:', res);
  await db.sequelize.close();
}

if (require.main === module) {
  main().catch((e) => { console.error('[vpngate] FAILED:', e.message); process.exit(1); });
}

module.exports = { fetchVpnGateCsv, parseVpnGateCsv, syncToDb };
