# Lucky VPN — Feature Audit Report (စစ်ဆေးချက် အစီရင်ခံစာ)

**Date:** 2026-10-12
**Scope:** backend/ vs src/ (React) vs android/ — every feature, implementation, and package checked.
**Verdict up front:** Backend is real and complete (no stub endpoints). The React web frontend is **100% mock** — zero API calls anywhere. The Android app uses only ~8 of 40+ backend endpoints, and **3 of its API calls hit endpoints that don't exist (404)**.

---

## A. Backend inventory — Endpoint များ (အကုန် အစစ်, stub မရှိ)

Mount points (`backend/server.js` lines 90–111):

| Mount | Router file |
|---|---|
| `/api/v1/auth` | routes/auth.js |
| `/api/v1/users` | routes/users.js |
| `/api/v1/servers` | routes/servers.js |
| `/api/v1/subscription` ⚠️ | routes/payments.js |
| `/api/v1/rewards` | routes/rewards.js |
| `/api/v1/admin` | routes/admin.js |
| `/api/v1/oneconnect` | routes/oneconnect.js |
| `/api/v1/ads` | routes/ads.js |
| `/api/v1/notifications` | routes/notifications.js |
| `/api/v1/support` | routes/support.js |
| `/api/v1/blog` | routes/blog.js |
| `/api/v1/leaderboard` | routes/leaderboard.js |

### Endpoint list (method + path + auth)

**auth.js** → `/api/v1/auth`
- `POST /register` — public (rate-limited) — real, bcrypt + JWT
- `POST /login` — public (rate-limited) — real
- `POST /admin/login` — public (rate-limited) — real, checks Admin model

**users.js** → `/api/v1/users`
- `GET /profile` — user auth — real
- `PUT /profile` — user auth — real
- `GET /` — adminAuth — real (paginated, search)
- `PUT /:id/status` — adminAuth — real (block/unblock)

**servers.js** → `/api/v1/servers`
- `GET /` — adminOrAuth — real (admins see all incl. offline, users see online only)
- `GET /free` — public — real
- `GET /premium` — user auth — real
- `POST /` — adminAuth — real
- `PUT /:id` — adminAuth — real
- `DELETE /:id` — adminAuth — real
- `GET /:id/config` — optionalAuth — real (returns inline .ovpn)

**payments.js** → `/api/v1/subscription` ⚠️ (NOT `/api/v1/payments` — see Gap G1)
- `POST /submit` — user auth — real (manual screenshot/txn-id flow, status pending)
- `GET /history` — user auth — real
- `GET /pending` — adminAuth — real
- `POST /:id/approve` — adminAuth — real (grants premium)
- `POST /:id/reject` — adminAuth — real

**rewards.js** → `/api/v1/rewards`
- `GET /` — user auth — real (reward history)
- `POST /daily` — user auth — real (100 pts, once/day enforced)
- `POST /watch-ad` — user auth — real (points for ad watch)
- `GET /all` — adminAuth — real

**admin.js** → `/api/v1/admin`
- `GET /stats` — adminAuth — real (users, premium, servers, payments)
- `GET /activity` — adminAuth — real
- `POST /vpngate-sync` — adminAuth — real (triggers VPNGate CSV sync)

**oneconnect.js** → `/api/v1/oneconnect`
- `GET /config` — adminAuth — real
- `PUT /config` — adminAuth — real
- `POST /sync` — adminAuth — real (axios → provider API → upsert servers)

**ads.js** → `/api/v1/ads`
- `GET /config` — user auth — real (returns AdConfig)
- `GET /admin/config` — adminAuth — real
- `PUT /admin/config` — adminAuth — real

**notifications.js** → `/api/v1/notifications`
- `GET /` — user auth — real (user's inbox)
- `PUT /:id/read` — user auth — real
- `POST /send` — adminAuth — real (broadcast to all/premium/free)
- `GET /admin/all` — adminAuth — real

**support.js** → `/api/v1/support`
- `POST /tickets` — user auth — real
- `GET /tickets` — user auth — real
- `GET /admin/tickets` — adminAuth — real
- `PUT /admin/tickets/:id` — adminAuth — real

**blog.js** → `/api/v1/blog`
- `GET /posts` — user auth — real
- `GET /posts/:id` — user auth — real
- `GET /admin/posts` — adminAuth — real
- `POST /admin/posts` — adminAuth — real
- `PUT /admin/posts/:id` — adminAuth — real
- `DELETE /admin/posts/:id` — adminAuth — real

**leaderboard.js** → `/api/v1/leaderboard`
- `GET /points` — user auth — real
- `GET /referrals` — user auth — real
- `GET /my-rank` — user auth — real

**No stubs found.** Grep for TODO/FIXME/mock/placeholder across backend/routes, models, middleware, jobs, utils, config returned zero hits. Every handler does real DB work.

---

## B. Backend missing pieces — ပြဿနာများ

### B1. Dead npm dependencies (package.json မှာ ပါပြီး code မှာ မသုံးတာ)
| Package | Status |
|---|---|
| `multer@1.4.5-lts.1` | ❌ never required anywhere |
| `socket.io@4.7.4` | ❌ never required anywhere |
| `moment@2.29.4` | ❌ never required anywhere |

→ Safe to remove. (swagger-jsdoc/swagger-ui-express ARE used — `/api-docs` live.)

### B2. Dead middleware
- `backend/middleware/adminSecurity.js` — **never imported by server.js or any route**. Dead file.

### B3. Dead model/table
- `UserConnection` model (`backend/models/UserConnection.js`) is defined, associated in `models/index.js` (lines 70–74), and synced — but **no route or job ever creates/reads UserConnection rows**. Connection tracking table is empty forever.

### B4. VPNGate sync — OK
- `backend/jobs/sync-vpngate.js` IS scheduled: `server.js` lines 216–231, `cron.schedule('0 */6 * * *', runSync)` (every 6h) + manual trigger via `POST /api/v1/admin/vpngate-sync`. Working.

### B5. .env.example gaps
- `.env.example` documents `ONECONNECT_API_URL` / `ONECONNECT_API_KEY`, but `routes/oneconnect.js` reads config from the **DB** (`OneConnectConfig` model), not env — the env vars are misleading leftovers.
- `KPAY_MERCHANT_ID`, `WAVE_MERCHANT_ID`, `AYA_MERCHANT_ID`, `PAYMENT_SECRET` in `.env.example` — **no backend code reads these**. Payment is manual screenshot-review flow; the "gateway" vars are aspirational.
- `UPLOAD_MAX_SIZE` — no upload handling exists (multer unused).

---

## C. Android gaps — အင်ဒရိုက်မှာ မပါတာ / ပျက်နေတာ

### C1. 🔴 BROKEN — Android calls endpoints that DO NOT EXIST (404)

| Android file | Calls | Backend reality |
|---|---|---|
| `PaymentManager.java:34` | `POST /api/v1/payments/submit` | ❌ payments mounted at `/api/v1/subscription` → **404. Payment submission completely broken.** |
| `APIConfig.java` (`PAYMENT_ACCOUNTS_ENDPOINT`) | `GET /api/v1/payments/accounts` | ❌ no `/accounts` endpoint exists anywhere → **404** |
| `PointsManager.java:27` | `POST /api/v1/rewards/add-points` | ❌ no such endpoint (real one is `POST /rewards/watch-ad`) → **404** |

→ Premium payment flow cannot work end-to-end on Android today.

### C2. Android API usage map (what it DOES call)

| Backend endpoint | Android caller | Status |
|---|---|---|
| `POST /auth/login` | AuthManager.java:43 | ✅ |
| `POST /auth/register` | AuthManager.java:97 | ✅ |
| `GET /servers/free` | ServerManager.java:42 | ✅ |
| `GET /servers/premium` | ServerManager.java:46 | ✅ (needs login) |
| `GET /servers/:id/config` | ServerManager.java:137 | ✅ |
| `POST /rewards/daily` | RewardsManager.java:31 | ✅ |
| `POST /rewards/watch-ad` | RewardsManager.java:64,99 | ✅ |

### C3. Backend endpoints Android NEVER calls (feature gaps)

- `GET /users/profile`, `PUT /users/profile` — no profile data from server; ProfileFragment shows local/auth data only
- `GET /rewards/` — reward history never shown
- `GET /subscription/history` — user can't see own payment submissions
- `GET /notifications/`, `PUT /notifications/:id/read` — no notification inbox; no FCM wired either
- `POST /support/tickets`, `GET /support/tickets` — Profile → Help/Support rows show "coming soon" toast (ProfileFragment.java:67–78)
- `GET /blog/posts` — no news/blog screen
- `GET /leaderboard/*` — no leaderboard screen
- `GET /ads/config` — ad config fetched by nobody (RewardsFragment has AdView code but check C4)

### C4. Android placeholders / half-done
- `PaymentActivity.java:151–158` — KPay/Wave QR codes are **drawable placeholders** (`kpay_qr_placeholder`, `wave_qr_placeholder`), not real merchant QRs
- `ProfileFragment.java:67–78` — Help, Privacy Policy, About rows → "coming soon" toast
- `RewardsFragment.java` — no AdMob code present (earlier grep matched method name `watchRewardedAd` only); `play-services-ads:22.6.0` is a dependency in build.gradle but unused → either implement rewarded ads or drop the dep to save size

### C5. VPN packages — present and correct
- `io.github.tim06:openvpn:1.1.3` + `basevpnprotocols:1.1.1` (embedded OpenVPN3, Apache-2.0) — in `app/build.gradle:50–51`
- `libovpn3.so` confirmed in APK (all 4 ABIs)
- WireGuard (`com.wireguard.android:tunnel`) commented out (build.gradle:56) — D8 dexing issue, deferred
- No references to `de.blinkt.openvpn` (external app) — fully embedded ✅

---

## D. React gaps — React (src/) က အတုချည်း

**Every single React file has ZERO real API calls.** `src/api/client.ts` exists but is imported by nothing.

| File | Real API calls | Notes |
|---|---|---|
| `pages/AdminLogin.tsx` | 0 | Hardcoded `admin@monetizevpn.com` / `admin123`, `setTimeout` fake login |
| `pages/AdminPanel.tsx` | 0 | Tab shell only |
| `pages/Index.tsx`, `Landing.tsx`, `NotFound.tsx` | 0 | Static |
| `components/admin/ServerManagement.tsx` | 0 | `setTimeout` fake data |
| `components/admin/OneConnectSettings.tsx` | 0 | `setTimeout` fake data |
| Other 12 admin components | 0 | Static/hardcoded JSX |

→ The React frontend is a **UI mockup only**. It is NOT a working admin panel and cannot be "fixed" — the real admin panel is `backend/public/admin.html` (+ `admin.js`), which is fully wired and deployed.

---

## E. Prioritized implementation list — လုပ်ရမယ့်စာရင်း

### 🔴 P0 — ပျက်နေတာ (user-facing broken)
1. **Android payment path 404** — `PaymentManager.java:34` + `APIConfig.java`: change `/payments/submit` → `/subscription/submit` (backend mount, server.js:93). Also fix or remove `PAYMENT_ACCOUNTS_ENDPOINT` (`/payments/accounts` doesn't exist — either add backend `GET /subscription/accounts` returning merchant account info, or drop the call).
2. **Android PointsManager 404** — `PointsManager.java:27`: `/rewards/add-points` doesn't exist → point it at `POST /rewards/watch-ad` or remove the class if unused.
3. **Real payment QR codes** — `PaymentActivity.java:151–158`: replace `kpay_qr_placeholder` / `wave_qr_placeholder` drawables with the admin's real KPay/Wave merchant QRs (user must supply images).

### 🟡 P1 — Feature gaps (backend ready, Android missing)
4. **Notifications screen** — backend `GET /notifications` + `PUT /:id/read` exist; Android has nothing. Add inbox fragment + badge. (FCM push itself is a bigger project — inbox first.)
5. **Support tickets** — backend `POST/GET /support/tickets` exist; Profile → Help currently "coming soon". Add ticket list + create form.
6. **Payment history** — backend `GET /subscription/history` exists; add "My payments" row in Profile showing pending/approved status.
7. **Reward history** — backend `GET /rewards/` exists; RewardsFragment shows actions but no history list.
8. **Profile from server** — `GET /users/profile` unused; ProfileFragment could show real points/plan/expiry.
9. **Blog/news screen** — `GET /blog/posts` exists; optional announcement feed.
10. **Leaderboard** — `GET /leaderboard/*` exist; optional gamification screen.

### 🟢 P2 — Cleanup / tech debt
11. Remove dead npm deps: `multer`, `socket.io`, `moment` (backend/package.json).
12. Delete or wire `middleware/adminSecurity.js` (currently dead).
13. Decide on `UserConnection` model: either record connections (needs an endpoint + Android call on connect/disconnect) or drop the table.
14. Fix `.env.example`: remove `ONECONNECT_API_*` (DB-driven now), `KPAY/WAVE/AYA_MERCHANT_ID`, `PAYMENT_SECRET`, `UPLOAD_MAX_SIZE` — or implement what they promise.
15. React `src/` decision: it's 100% mock — either delete it to avoid confusion, or mark clearly as design-only. (The real admin panel is backend/public/admin.html.)

### ⏸️ Deferred (known)
- WireGuard in Android — blocked on D8 dexing of `com.wireguard.android:tunnel:1.0.20230706` ("Record desugaring without global-synthetics consumer"). Needs newer AGP or alternative integration.
- FCM push notifications — no Firebase wiring in backend or Android yet.

---

## Appendix — file reference

- Backend routes: `backend/routes/*.js` (12 files, 40 endpoints, 0 stubs)
- Backend server: `backend/server.js` (mounts, CSP, cron, static admin)
- Real admin panel: `backend/public/admin.html` + `backend/public/admin.js` (8 tabs, deployed)
- React mock frontend: `src/` (0 API calls — design reference only)
- Android API layer: `android/app/src/main/java/app/lovable/luckyvpnmaster/{api,auth,rewards}/`
- Android VPN: `android/app/src/main/java/app/lovable/luckyvpnmaster/vpn/`
- Android entry: `MainActivity.java`, fragments in `.../fragments/`
