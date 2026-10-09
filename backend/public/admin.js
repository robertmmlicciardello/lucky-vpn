const API = location.origin + '/api/v1';
// localStorage can throw in private/incognito mode — guard it so the whole
// script (including the login button handler) doesn't die on page load.
const store = {
  get(k) { try { return localStorage.getItem(k) || ''; } catch (e) { return ''; } },
  set(k, v) { try { localStorage.setItem(k, v); } catch (e) {} },
  del(k) { try { localStorage.removeItem(k); } catch (e) {} }
};
let token = store.get('admin_token');

const $ = id => document.getElementById(id);
function showMsg(text, ok) {
  const m = $('msg'); m.textContent = text;
  m.className = 'msg ' + (ok ? 'ok' : 'err');
  m.classList.remove('hidden');
  setTimeout(() => m.classList.add('hidden'), 4000);
}
async function api(path, method, body) {
  const r = await fetch(API + path, {
    method: method || 'GET',
    headers: Object.assign({ 'Content-Type': 'application/json' }, token ? { 'Authorization': 'Bearer ' + token } : {}),
    body: body ? JSON.stringify(body) : undefined
  });
  const j = await r.json().catch(() => ({}));
  if (!r.ok) throw new Error(j.message || ('HTTP ' + r.status));
  return j;
}

// ---- login ----
async function doLogin() {
  const e = $('loginError'); e.classList.add('hidden');
  try {
    const j = await api('/auth/admin/login', 'POST', { email: $('email').value.trim(), password: $('password').value });
    token = j.data.token;
    store.set('admin_token', token);
    enterApp();
  } catch (err) { e.textContent = err.message; e.classList.remove('hidden'); }
}
function enterApp() { $('loginView').classList.add('hidden'); $('appView').classList.remove('hidden'); loadDashboard(); loadServers(); }
$('loginBtn').onclick = doLogin;
$('password').addEventListener('keydown', e => { if (e.key === 'Enter') doLogin(); });
$('logoutBtn').onclick = () => { token = ''; store.del('admin_token'); location.reload(); };

// ---- tabs ----
document.querySelectorAll('.tabs button').forEach(b => b.onclick = () => {
  document.querySelectorAll('.tabs button').forEach(x => x.classList.remove('active'));
  b.classList.add('active');
  $('tab-dashboard').classList.toggle('hidden', b.dataset.tab !== 'dashboard');
  $('tab-servers').classList.toggle('hidden', b.dataset.tab !== 'servers');
});

// ---- dashboard ----
async function loadDashboard() {
  try {
    const j = await api('/admin/stats');
    const d = j.data;
    $('stUsers').textContent = d.totalUsers;
    $('stPremium').textContent = d.premiumUsers;
    $('stServers').textContent = d.activeServers;
    $('stPayments').textContent = d.pendingPayments;
  } catch (err) { showMsg('Stats: ' + err.message, false); }
}
$('refreshBtn').onclick = loadDashboard;
$('syncBtn').onclick = async () => {
  $('syncBtn').disabled = true; $('syncBtn').textContent = 'Syncing…';
  try { const j = await api('/admin/vpngate-sync', 'POST'); showMsg('VPNGate sync done: ' + JSON.stringify(j.data || j.message || 'ok'), true); loadDashboard(); loadServers(); }
  catch (err) { showMsg('Sync failed: ' + err.message, false); }
  $('syncBtn').disabled = false; $('syncBtn').textContent = '🔄 Sync VPNGate now';
};

// ---- servers ----
let servers = [];
async function loadServers() {
  try {
    const j = await api('/servers');
    servers = j.data.servers || j.data || [];
    const tb = $('serverRows');
    if (!servers.length) { tb.innerHTML = '<tr><td colspan="7" style="text-align:center;color:#aaa">No servers</td></tr>'; return; }
    tb.innerHTML = servers.map(s =>
      '<tr><td>' + s.id + '</td><td>' + esc(s.name) + '</td><td>' + esc(s.country) + '</td>' +
      '<td>' + esc(s.ip) + '</td><td>' + esc(s.type) + '</td><td>' + esc(s.status) + '</td>' +
      '<td><button class="small" onclick="editServer(' + s.id + ')">Edit</button> ' +
      '<button class="small danger" onclick="delServer(' + s.id + ')">Del</button></td></tr>'
    ).join('');
  } catch (err) { $('serverRows').innerHTML = '<tr><td colspan="7" style="color:#c62828">' + esc(err.message) + '</td></tr>'; }
}
function esc(s) { return String(s == null ? '' : s).replace(/[&<>"]/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;'}[c])); }
$('reloadServersBtn').onclick = loadServers;
$('addServerBtn').onclick = () => openModal(null);
function openModal(s) {
  $('modalTitle').textContent = s ? 'Edit server #' + s.id : 'Add server';
  $('f_id').value = s ? s.id : '';
  $('f_name').value = s ? s.name || '' : '';
  $('f_country').value = s ? s.country || '' : '';
  $('f_city').value = s ? s.city || '' : '';
  $('f_ip').value = s ? s.ip || '' : '';
  $('f_port').value = s ? s.port || '' : '';
  $('f_protocol').value = s ? s.protocol || 'OpenVPN' : 'OpenVPN';
  $('f_type').value = s ? s.type || 'free' : 'free';
  $('f_config').value = s ? s.config_file || '' : '';
  $('serverModal').classList.remove('hidden');
}
window.editServer = id => openModal(servers.find(s => s.id === id));
window.delServer = async id => {
  if (!confirm('Delete server #' + id + '?')) return;
  try { await api('/servers/' + id, 'DELETE'); showMsg('Server deleted', true); loadServers(); }
  catch (err) { showMsg('Delete failed: ' + err.message, false); }
};
$('cancelModalBtn').onclick = () => $('serverModal').classList.add('hidden');
$('saveServerBtn').onclick = async () => {
  const id = $('f_id').value;
  const body = {
    name: $('f_name').value.trim(), country: $('f_country').value.trim(), city: $('f_city').value.trim(),
    ip: $('f_ip').value.trim(), port: $('f_port').value.trim() || '1194',
    protocol: $('f_protocol').value, type: $('f_type').value, config_file: $('f_config').value
  };
  if (!body.name || !body.ip) { showMsg('Name and IP are required', false); return; }
  try {
    if (id) await api('/servers/' + id, 'PUT', body);
    else await api('/servers', 'POST', body);
    $('serverModal').classList.add('hidden');
    showMsg('Server saved', true); loadServers(); loadDashboard();
  } catch (err) { showMsg('Save failed: ' + err.message, false); }
};

// auto-login if token saved
if (token) enterApp();
