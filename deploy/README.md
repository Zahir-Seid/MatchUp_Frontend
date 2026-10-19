# Deployment notes

## PWA

1. Build the production distribution:

   ```sh
   ./gradlew :webApp:wasmJsBrowserDistribution
   ```

2. Copy `webApp/build/dist/wasmJs/productionExecutable/` to `/srv/matchup/app/`.
3. Put `deploy/Caddyfile` at `/etc/caddy/Caddyfile` and reload Caddy:

   ```sh
   sudo caddy validate --config /etc/caddy/Caddyfile
   sudo systemctl reload caddy
   ```

`app.matchup.et` must resolve to the server and ports 80/443 must be open so
Caddy can issue the certificate. Caddy proxies `/v1/*` to the backend on
`127.0.0.1:8082`, which avoids adding CORS to the public API.

## Android App Links

Google sign-in on Android uses the Credential Manager, not an app link. App
Links are used for password-reset and email-verification URLs.

1. Build a release APK/AAB and obtain the signing certificate SHA-256:

   ```sh
   keytool -list -v -keystore release.keystore -alias matchup
   ```

2. Copy `assetlinks.json.example` to the web root as
   `.well-known/assetlinks.json` and replace the fingerprint.
3. Verify with:

   ```sh
   adb shell pm get-app-links et.matchup.app
   ```

The Google OAuth **web** client must also have
`https://app.matchup.et` listed as an authorized JavaScript origin. Android
requests use the same web client ID as the server client ID.

## Cache updates

The service worker uses `CACHE_VERSION` in `sw.js`. Bump it for every
deployment so old assets are evicted. Ordinary JS/WASM requests use
stale-while-revalidate, so a client picks up a new build on the following
launch even without a version bump.
