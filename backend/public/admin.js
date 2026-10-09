/* Lucky VPN — Admin Panel (external JS, CSP-safe: no inline scripts/handlers) */
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
function esc(s) {
  return String(s == null ? '' : s)
    .replace(/[&<>"]/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[c]));
}
function fmtDate(d) {
  if (!d) return '–';
  try { return new Date(d).toLocaleString(); } catch (e) { return String(d); }
}
function badge(text, color) {
  return '<span class="badge ' + color + '">' + esc(text) + '</span>';
}
function statusBadge(s) {
  s = String(s || '');
  if (s === 'online' || s === 'active' || s === 'approved' || s === 'published' || s === 'resolved') return badge(s, 'green');
  if (s === 'offline' || s === 'blocked' || s === 'rejected' || s === 'closed') return badge(s, 'red');
  if (s === 'pending' || s === 'open') return badge(s, 'orange');
  if (s === 'premium') return badge(s, 'blue');
  return badge(s || '–', 'grey');
}
function showMsg(text, ok) {
  const m = $('msg'); m.textContent = text;
  m.className = 'msg ' + (ok ? 'ok' : 'err');
  m.classList.remove('hidden');
  clearTimeout(showMsg._t);
  showMsg._t = setTimeout(() => m.classList.add('hidden'), 5000);
  window.scrollTo(0, 0);
}
async function api(path, method, body) {
  const r = await fetch(API + path, {
    method: method || 'GET',
    headers: Object.assign(
      { 'Content-Type': 'application/json' },
      token ? { 'Authorization': 'Bearer ' + token } : {}
    ),
    body: body ? JSON.stringify(body) : undefined
  });
  const j = await r.json().catch(() => ({}));
  if (!r.ok) throw new Error(j.message || ('HTTP ' + r.status));
  return j;
}

// ---------- login ----------
async function doLogin() {
  const e = $('loginError'); e.classList.add('hidden');
  try {
    const j = await api('/auth/admin/login', 'POST',
      { email: $('email').value.trim(), password: $('password').value });
    token = j.data.token;
    store.set('admin_token', token);
    enterApp();
  } catch (err) { e.textContent = err.message; e.classList.remove('hidden'); }
}
function enterApp() {
  $('loginView').classList.add('hidden');
  $('appView').classList.remove('hidden');
  switchTab('dashboard');
}
$('loginBtn').onclick = doLogin;
$('password').addEventListener('keydown', e => { if (e.key === 'Enter') doLogin(); });
$('logoutBtn').onclick = () => { token = ''; store.del('admin_token'); location.reload(); };

// ---------- tabs ----------
const loadedTabs = {};
function switchTab(name) {
  document.querySelectorAll('#tabs button').forEach(b =>
    b.classList.toggle('active', b.dataset.tab === name));
  document.querySelectorAll('.content > div[id^="tab-"]').forEach(p =>
    p.classList.toggle('hidden', p.id !== 'tab-' + name));
  if (!loadedTabs[name]) { loadedTabs[name] = true; loadTab(name); }
}
function loadTab(name) {
  ({ dashboard: loadDashboard, servers: loadServers, users: loadUsers,
     payments: loadPayments, notifications: loadNotifications, support: loadTickets,
     blog: loadPosts, ads: loadAds }[name] || (() => {}))();
}
document.querySelectorAll('#tabs button').forEach(b => b.onclick = () => switchTab(b.dataset.tab));

// ---------- dashboard ----------
async function loadDashboard() {
  try {
    const j = await api('/admin/stats');
    const d = j.data || {};
    $('stUsers').textContent = d.totalUsers != null ? d.totalUsers : '–';
    $('stPremium').textContent = d.premiumUsers != null ? d.premiumUsers : '–';
    $('stServers').textContent = d.activeServers != null ? d.activeServers : '–';
    $('stPayments').textContent = d.pendingPayments != null ? d.pendingPayments : '–';
    $('stRevenue').textContent = d.totalRevenue != null ? d.totalRevenue : '–';
  } catch (err) { showMsg('Stats: ' + err.message, false); }
  try {
    const j = await api('/admin/activity');
    const d = j.data || {};
    const users = d.recentUsers || [];
    $('actUsers').innerHTML = users.length ? users.map(u =>
      '<tr><td>' + esc(u.name) + '</td><td>' + esc(u.email) + '</td><td>' + statusBadge(u.plan) +
      '</td><td>' + fmtDate(u.createdAt) + '</td></tr>').join('')
      : '<tr><td colspan="4" style="text-align:center;color:#aaa">No users yet</td></tr>';
    const pays = d.recentPayments || [];
    $('actPayments').innerHTML = pays.length ? pays.map(p =>
      '<tr><td>' + esc((p.User && p.User.email) || p.user_id) + '</td><td>' + esc(p.amount) +
      '</td><td>' + statusBadge(p.status) + '</td><td>' + fmtDate(p.createdAt) + '</td></tr>').join('')
      : '<tr><td colspan="4" style="text-align:center;color:#aaa">No payments yet</td></tr>';
  } catch (err) { showMsg('Activity: ' + err.message, false); }
}
$('refreshBtn').onclick = loadDashboard;
$('syncBtn').onclick = async () => {
  $('syncBtn').disabled = true; $('syncBtn').textContent = 'Syncing…';
  try {
    const j = await api('/admin/vpngate-sync', 'POST');
    showMsg('VPNGate sync done: ' + JSON.stringify(j.data || j.message || 'ok'), true);
    loadDashboard(); loadServers();
  } catch (err) { showMsg('Sync failed: ' + err.message, false); }
  $('syncBtn').disabled = false; $('syncBtn').textContent = '🔄 Sync VPNGate now';
};

// ---------- servers ----------
let servers = [];
async function loadServers() {
  try {
    const j = await api('/servers');
    servers = j.data.servers || j.data || [];
    const tb = $('serverRows');
    if (!servers.length) {
      tb.innerHTML = '<tr><td colspan="9" style="text-align:center;color:#aaa">No servers</td></tr>';
      return;
    }
    tb.innerHTML = servers.map(s =>
      '<tr><td>' + s.id + '</td><td>' + esc(s.name) + '</td><td>' + esc(s.country) + '</td>' +
      '<td>' + esc(s.city) + '</td><td>' + esc(s.ip) + '</td><td>' + esc(s.protocol) + '</td>' +
      '<td>' + statusBadge(s.type) + '</td><td>' + statusBadge(s.status) + '</td>' +
      '<td style="white-space:nowrap">' +
      '<button class="small" data-action="edit-server" data-id="' + s.id + '">Edit</button>' +
      '<button class="small danger" data-action="del-server" data-id="' + s.id + '">Del</button>' +
      '</td></tr>').join('');
  } catch (err) {
    $('serverRows').innerHTML = '<tr><td colspan="9" style="color:#c62828">' + esc(err.message) + '</td></tr>';
  }
}
$('reloadServersBtn').onclick = loadServers;
$('addServerBtn').onclick = () => openServerModal(null);
function openServerModal(s) {
  $('serverModalTitle').textContent = s ? 'Edit server #' + s.id : 'Add server';
  $('f_id').value = s ? s.id : '';
  $('f_name').value = s ? s.name || '' : '';
  $('f_country').value = s ? s.country || '' : '';
  $('f_city').value = s ? s.city || '' : '';
  $('f_ip').value = s ? s.ip || '' : '';
  $('f_port').value = s ? s.port || '' : '';
  $('f_protocol').value = s ? s.protocol || 'OpenVPN' : 'OpenVPN';
  $('f_type').value = s ? s.type || 'free' : 'free';
  $('f_status').value = s ? s.status || 'online' : 'online';
  $('f_config').value = s ? s.config_file || '' : '';
  $('serverModal').classList.remove('hidden');
}
$('cancelServerBtn').onclick = () => $('serverModal').classList.add('hidden');
$('saveServerBtn').onclick = async () => {
  const id = $('f_id').value;
  const body = {
    name: $('f_name').value.trim(), country: $('f_country').value.trim(),
    city: $('f_city').value.trim(), ip: $('f_ip').value.trim(),
    port: $('f_port').value.trim() || '1194', protocol: $('f_protocol').value,
    type: $('f_type').value, status: $('f_status').value,
    config_file: $('f_config').value
  };
  if (!body.name || !body.ip) { showMsg('Name and IP are required', false); return; }
  try {
    if (id) await api('/servers/' + id, 'PUT', body);
    else await api('/servers', 'POST', body);
    $('serverModal').classList.add('hidden');
    showMsg('Server saved', true); loadServers(); loadDashboard();
  } catch (err) { showMsg('Save failed: ' + err.message, false); }
};

// ---------- users ----------
let userPage = 1, userPages = 1, userSearch = '';
async function loadUsers() {
  try {
    const q = '?page=' + userPage + '&limit=10' + (userSearch ? '&search=' + encodeURIComponent(userSearch) : '');
    const j = await api('/users' + q);
    const users = j.data || [];
    if (j.pagination) {
      userPage = j.pagination.page; userPages = j.pagination.pages;
      $('userPageInfo').textContent = 'Page ' + userPage + ' of ' + userPages + ' (' + j.pagination.total + ' users)';
    }
    const tb = $('userRows');
    tb.innerHTML = users.length ? users.map(u =>
      '<tr><td>' + u.id + '</td><td>' + esc(u.name) + '</td><td>' + esc(u.email) + '</td>' +
      '<td>' + statusBadge(u.plan) + '</td><td>' + statusBadge(u.status) + '</td>' +
      '<td>' + fmtDate(u.createdAt) + '</td>' +
      '<td><button class="small ' + (u.status === 'blocked' ? 'ok' : 'danger') + '"' +
      ' data-action="toggle-user" data-id="' + u.id + '" data-status="' + esc(u.status) + '">' +
      (u.status === 'blocked' ? 'Unblock' : 'Block') + '</button></td></tr>').join('')
      : '<tr><td colspan="7" style="text-align:center;color:#aaa">No users</td></tr>';
  } catch (err) {
    $('userRows').innerHTML = '<tr><td colspan="7" style="color:#c62828">' + esc(err.message) + '</td></tr>';
  }
}
$('userSearchBtn').onclick = () => { userSearch = $('userSearch').value.trim(); userPage = 1; loadUsers(); };
$('userClearBtn').onclick = () => { $('userSearch').value = ''; userSearch = ''; userPage = 1; loadUsers(); };
$('userSearch').addEventListener('keydown', e => { if (e.key === 'Enter') $('userSearchBtn').click(); });
$('userPrev').onclick = () => { if (userPage > 1) { userPage--; loadUsers(); } };
$('userNext').onclick = () => { if (userPage < userPages) { userPage++; loadUsers(); } };

// ---------- payments ----------
let payments = [];
async function loadPayments() {
  try {
    const j = await api('/payments/pending');
    payments = j.data || [];
    const tb = $('paymentRows');
    tb.innerHTML = payments.length ? payments.map(p => {
      const u = p.User || {};
      return '<tr><td>' + p.id + '</td><td>' + esc(u.name || '') + '<br><span class="muted">' + esc(u.email || '') +
        '</span></td><td>' + esc(p.amount) + '</td><td>' + esc(p.payment_method) + '</td>' +
        '<td>' + esc(p.transaction_id) + '</td><td>' + esc(p.plan_type) + ' / ' + esc(p.duration) + 'd</td>' +
        '<td>' + fmtDate(p.createdAt) + '</td>' +
        '<td><button class="small" data-action="review-payment" data-id="' + p.id + '">Review</button></td></tr>';
    }).join('')
      : '<tr><td colspan="8" style="text-align:center;color:#aaa">No pending payments</td></tr>';
  } catch (err) {
    $('paymentRows').innerHTML = '<tr><td colspan="8" style="color:#c62828">' + esc(err.message) + '</td></tr>';
  }
}
$('reloadPaymentsBtn').onclick = loadPayments;
function openPaymentModal(p) {
  const u = p.User || {};
  $('paymentModalTitle').textContent = 'Review payment #' + p.id;
  $('p_id').value = p.id;
  $('p_notes').value = '';
  $('paymentDetail').innerHTML =
    '<div class="kv"><b>User:</b> ' + esc(u.name) + ' (' + esc(u.email) + ')</div>' +
    '<div class="kv"><b>Amount:</b> ' + esc(p.amount) + '</div>' +
    '<div class="kv"><b>Method:</b> ' + esc(p.payment_method) + '</div>' +
    '<div class="kv"><b>Txn ID:</b> ' + esc(p.transaction_id) + '</div>' +
    '<div class="kv"><b>Plan:</b> ' + esc(p.plan_type) + ' / ' + esc(p.duration) + ' days</div>' +
    (p.screenshot ? '<div class="kv"><b>Screenshot:</b> <a href="' + esc(p.screenshot) + '" target="_blank">view</a></div>' : '');
  $('paymentModal').classList.remove('hidden');
}
$('cancelPaymentBtn').onclick = () => $('paymentModal').classList.add('hidden');
async function decidePayment(approve) {
  const id = $('p_id').value;
  const notes = $('p_notes').value.trim();
  if (!confirm((approve ? 'Approve' : 'Reject') + ' payment #' + id + '?')) return;
  try {
    await api('/payments/' + id + (approve ? '/approve' : '/reject'), 'POST',
      notes ? { admin_notes: notes } : {});
    $('paymentModal').classList.add('hidden');
    showMsg('Payment ' + (approve ? 'approved' : 'rejected'), true);
    loadPayments(); loadDashboard();
  } catch (err) { showMsg('Failed: ' + err.message, false); }
}
$('approvePaymentBtn').onclick = () => decidePayment(true);
$('rejectPaymentBtn').onclick = () => decidePayment(false);

// ---------- notifications ----------
async function loadNotifications() {
  try {
    const j = await api('/notifications/admin/all');
    const list = j.data || [];
    $('notifRows').innerHTML = list.length ? list.map(n =>
      '<tr><td>' + esc((n.User && n.User.email) || n.user_id) + '</td><td>' + esc(n.title) + '</td>' +
      '<td>' + esc(String(n.message || '').slice(0, 80)) + '</td><td>' + esc(n.type) + '</td>' +
      '<td>' + fmtDate(n.createdAt) + '</td></tr>').join('')
      : '<tr><td colspan="5" style="text-align:center;color:#aaa">No notifications</td></tr>';
  } catch (err) {
    $('notifRows').innerHTML = '<tr><td colspan="5" style="color:#c62828">' + esc(err.message) + '</td></tr>';
  }
}
$('sendNotifBtn').onclick = async () => {
  const body = {
    title: $('n_title').value.trim(), message: $('n_message').value.trim(),
    target_users: $('n_target').value, type: $('n_type').value
  };
  if (!body.title || !body.message) { showMsg('Title and message are required', false); return; }
  if (!confirm('Send to "' + body.target_users + '" users?')) return;
  try {
    const j = await api('/notifications/send', 'POST', body);
    showMsg(j.message || 'Sent', true);
    $('n_title').value = ''; $('n_message').value = '';
    loadNotifications();
  } catch (err) { showMsg('Send failed: ' + err.message, false); }
};

// ---------- support ----------
let tickets = [];
async function loadTickets() {
  try {
    const st = $('ticketFilter').value;
    const j = await api('/support/admin/tickets' + (st ? '?status=' + st : ''));
    tickets = j.data || [];
    $('ticketRows').innerHTML = tickets.length ? tickets.map(t => {
      const u = t.User || {};
      return '<tr><td>' + t.id + '</td><td>' + esc(u.name || '') + '<br><span class="muted">' + esc(u.email || '') +
        '</span></td><td>' + esc(t.subject) + '</td><td>' + esc(t.category) + '</td>' +
        '<td>' + statusBadge(t.status) + '</td><td>' + fmtDate(t.createdAt) + '</td>' +
        '<td><button class="small" data-action="open-ticket" data-id="' + t.id + '">View</button></td></tr>';
    }).join('')
      : '<tr><td colspan="7" style="text-align:center;color:#aaa">No tickets</td></tr>';
  } catch (err) {
    $('ticketRows').innerHTML = '<tr><td colspan="7" style="color:#c62828">' + esc(err.message) + '</td></tr>';
  }
}
$('reloadTicketsBtn').onclick = loadTickets;
$('ticketFilter').onchange = loadTickets;
function openTicketModal(t) {
  const u = t.User || {};
  $('ticketModalTitle').textContent = 'Ticket #' + t.id;
  $('t_id').value = t.id;
  $('t_status').value = t.status || 'open';
  $('t_response').value = t.admin_response || '';
  $('ticketDetail').innerHTML =
    '<div class="kv"><b>From:</b> ' + esc(u.name) + ' (' + esc(u.email) + ')</div>' +
    '<div class="kv"><b>Subject:</b> ' + esc(t.subject) + '</div>' +
    '<div class="kv"><b>Category:</b> ' + esc(t.category) + '</div>' +
    '<div class="kv"><b>Status:</b> ' + statusBadge(t.status) + '</div>' +
    '<div class="kv"><b>Message:</b></div><div class="panel" style="margin-top:4px">' + esc(t.message) + '</div>';
  $('ticketModal').classList.remove('hidden');
}
$('cancelTicketBtn').onclick = () => $('ticketModal').classList.add('hidden');
$('saveTicketBtn').onclick = async () => {
  const id = $('t_id').value;
  try {
    await api('/support/admin/tickets/' + id, 'PUT',
      { status: $('t_status').value, admin_response: $('t_response').value });
    $('ticketModal').classList.add('hidden');
    showMsg('Ticket updated', true); loadTickets();
  } catch (err) { showMsg('Save failed: ' + err.message, false); }
};

// ---------- blog ----------
let posts = [];
async function loadPosts() {
  try {
    const j = await api('/blog/admin/posts');
    posts = j.data || [];
    $('postRows').innerHTML = posts.length ? posts.map(p =>
      '<tr><td>' + p.id + '</td><td>' + esc(p.title) + '</td>' +
      '<td>' + statusBadge(p.status) + '</td><td>' + fmtDate(p.updatedAt || p.createdAt) + '</td>' +
      '<td style="white-space:nowrap">' +
      '<button class="small" data-action="edit-post" data-id="' + p.id + '">Edit</button>' +
      '<button class="small danger" data-action="del-post" data-id="' + p.id + '">Del</button>' +
      '</td></tr>').join('')
      : '<tr><td colspan="5" style="text-align:center;color:#aaa">No posts</td></tr>';
  } catch (err) {
    $('postRows').innerHTML = '<tr><td colspan="5" style="color:#c62828">' + esc(err.message) + '</td></tr>';
  }
}
$('reloadPostsBtn').onclick = loadPosts;
$('addPostBtn').onclick = () => openPostModal(null);
function openPostModal(p) {
  $('blogModalTitle').textContent = p ? 'Edit post #' + p.id : 'New post';
  $('b_id').value = p ? p.id : '';
  $('b_title').value = p ? p.title || '' : '';
  $('b_excerpt').value = p ? p.excerpt || '' : '';
  $('b_image').value = p ? p.featured_image || '' : '';
  $('b_content').value = p ? p.content || '' : '';
  $('b_status').value = p ? p.status || 'draft' : 'draft';
  $('blogModal').classList.remove('hidden');
}
$('cancelPostBtn').onclick = () => $('blogModal').classList.add('hidden');
$('savePostBtn').onclick = async () => {
  const id = $('b_id').value;
  const body = {
    title: $('b_title').value.trim(), excerpt: $('b_excerpt').value.trim(),
    featured_image: $('b_image').value.trim(), content: $('b_content').value,
    status: $('b_status').value
  };
  if (!body.title) { showMsg('Title is required', false); return; }
  try {
    if (id) await api('/blog/admin/posts/' + id, 'PUT', body);
    else await api('/blog/admin/posts', 'POST', body);
    $('blogModal').classList.add('hidden');
    showMsg('Post saved', true); loadPosts();
  } catch (err) { showMsg('Save failed: ' + err.message, false); }
};

// ---------- ads ----------
const AD_FIELDS = ['primary_network', 'show_ads', 'ad_frequency',
  'admob_app_id', 'admob_banner_id', 'admob_interstitial_id', 'admob_rewarded_id',
  'facebook_app_id', 'facebook_banner_id', 'facebook_interstitial_id', 'facebook_rewarded_id',
  'unity_game_id', 'unity_banner_id', 'unity_interstitial_id', 'unity_rewarded_id'];
async function loadAds() {
  try {
    const j = await api('/ads/admin/config');
    const c = j.data || {};
    AD_FIELDS.forEach(f => {
      const el = $('ad_' + f);
      if (!el) return;
      if (f === 'show_ads') el.value = c[f] === false || c[f] === 'false' ? 'false' : 'true';
      else el.value = c[f] != null ? c[f] : '';
    });
  } catch (err) { showMsg('Ads config: ' + err.message, false); }
}
$('saveAdsBtn').onclick = async () => {
  const body = {};
  AD_FIELDS.forEach(f => {
    const el = $('ad_' + f);
    if (!el) return;
    if (f === 'show_ads') body[f] = el.value === 'true';
    else if (f === 'ad_frequency') body[f] = el.value ? parseInt(el.value, 10) : null;
    else body[f] = el.value.trim();
  });
  try {
    await api('/ads/admin/config', 'PUT', body);
    showMsg('Ad config saved', true);
  } catch (err) { showMsg('Save failed: ' + err.message, false); }
};

// ---------- delegated row actions (CSP-safe: no inline onclick) ----------
document.addEventListener('click', async e => {
  const el = e.target.closest('[data-action]');
  if (!el) return;
  const action = el.dataset.action;
  const id = parseInt(el.dataset.id, 10);

  if (action === 'edit-server') {
    openServerModal(servers.find(s => s.id === id));
  } else if (action === 'del-server') {
    if (!confirm('Delete server #' + id + '?')) return;
    try { await api('/servers/' + id, 'DELETE'); showMsg('Server deleted', true); loadServers(); }
    catch (err) { showMsg('Delete failed: ' + err.message, false); }
  } else if (action === 'toggle-user') {
    const cur = el.dataset.status;
    const next = cur === 'blocked' ? 'active' : 'blocked';
    if (!confirm((next === 'blocked' ? 'Block' : 'Unblock') + ' user #' + id + '?')) return;
    try { await api('/users/' + id + '/status', 'PUT', { status: next }); showMsg('User ' + next, true); loadUsers(); }
    catch (err) { showMsg('Failed: ' + err.message, false); }
  } else if (action === 'review-payment') {
    const p = payments.find(x => x.id === id);
    if (p) openPaymentModal(p);
  } else if (action === 'open-ticket') {
    const t = tickets.find(x => x.id === id);
    if (t) openTicketModal(t);
  } else if (action === 'edit-post') {
    openPostModal(posts.find(p => p.id === id));
  } else if (action === 'del-post') {
    if (!confirm('Delete post #' + id + '?')) return;
    try { await api('/blog/admin/posts/' + id, 'DELETE'); showMsg('Post deleted', true); loadPosts(); }
    catch (err) { showMsg('Delete failed: ' + err.message, false); }
  }
});

// auto-login if token saved
if (token) enterApp();
