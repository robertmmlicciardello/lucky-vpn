# Deploying the backend (FREE)

## Option A — Render + TiDB Cloud (recommended, 100% free)

1. **Database** — sign up at [tidbcloud.com](https://tidbcloud.com) (free tier,
   no credit card). Create a Serverless cluster, then copy:
   host, port (4000), user, password. The default database is `test`.
2. **Push this repo to GitHub** (needs force-push once — history was rewritten
   to purge a leaked `.env`):
   `git push --force origin main`
3. **Web service** — sign up at [render.com](https://render.com) (free tier),
   *New → Web Service →* connect this repo. Render detects `render.yaml`
   automatically. Fill in `DB_HOST`, `DB_USER`, `DB_PASSWORD`
   (from step 1). `JWT_SECRET` is auto-generated.
4. Deploy. The API will be at `https://lucky-vpn-api.onrender.com`.
   The VPNGate free-server list syncs itself every 6 hours (`SYNC_CRON`).
   Check `/health` to confirm it's up.

Note: Render's free tier sleeps after 15 min idle and wakes on the next
request (cold start ~30-60s). Fine for testing.

## Option B — Railway ($5 free credit, simplest)

1. Sign up at [railway.app](https://railway.app), *New Project → Deploy from
   GitHub repo*, add a MySQL plugin.
2. Set the same env vars as above (`DB_*`, `JWT_SECRET`).
3. Railway gives you a public URL immediately.

## After deploy — point the app at it

1. In `android/app/src/main/java/app/lovable/luckyvpnmaster/api/APIConfig.java`
   set `API_BASE_URL = "https://<your-host>/api/v1"`.
2. Rebuild the APK and install on the phone (with **OpenVPN for Android**
   installed from the Play Store).
3. Register → pick a free server → Connect.

## Manual VPNGate sync (any host)

`node backend/jobs/sync-vpngate.js` (needs the DB_* env vars),
or `POST /api/v1/admin/vpngate-sync` with an admin token.
