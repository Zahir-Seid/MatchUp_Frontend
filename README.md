# MatchUp Mobile

MatchUp is a Kotlin Multiplatform client for the existing MatchUp backend. One
Compose Multiplatform UI runs on Android and as a WASM PWA for iPhone/iPad
(Safari → Add to Home Screen). It uses the real `/v1` API; there is no mock
data layer.

## What is implemented

- Player login, signup, Google sign-in, logout, logout-everywhere,
  forgot/reset password, change password.
- Game catalog with server data, free-text search and date/format/field/
  availability filters (applied on-device to the cached catalog).
- Game detail, quote, participant slots, slot count, saved teammates, reserve.
- Booking list and detail with hold countdown, payment instructions, payment
  reference submission, payment history, QR check-in pass and cancellation.
- Notification feed with 30-second unread polling, mark-read and mark-all-read.
- Profile editing, email-verification UI, saved-teammates manager.
- Offline read-only snapshots of catalog, bookings, notifications, profile,
  payment instructions and saved teammates. Booking, payment, cancellation and
  profile writes are never queued while offline.
- Dark lime MatchUp theme, edge-to-edge dark system bars with light icons, a
  620 dp centered frame on desktop-width browsers and safe-area aware mobile
  layouts.

The UI follows the backend's current constraints: max 14 spots per booking,
`cbe` / `telebirr` payment methods, participant names, and booking statuses
`pending_payment`, `verifying`, `confirmed`, `pending_review`, `expired`,
`cancelled`.

## Project layout

```
shared/       Compose UI, screens, state, repositories, API, caching, platform contracts
androidApp/   Android entry point, manifest, app links, icons, debug cleartext config
webApp/       WASM entry point, index.html, manifest, service worker, webpack proxy
deploy/       Caddy config and Android App Links assetlinks example
```

## Requirements

- JDK 17 or newer. Android Studio's bundled JDK 21 also works.
- Android Studio Panda 3 (2025.3.3 Patch 1) or newer. The project uses
  AGP 9.1.1, and `compileSdk 37` needs AGP 9.1.1 or newer.
- Android SDK Platform 37 (`platforms;android-37.0`) and Build Tools 36.0.0.
  Configure them through `ANDROID_HOME` or `sdk.dir` in `local.properties`.
- Node/Yarn do not need to be installed; the Kotlin Gradle plugin downloads
  pinned copies the first time a JS/WASM task runs.
- Backend running locally on port 8082 for development.

## Open in Android Studio

The repository is a standard Gradle project with a committed wrapper, so
Android Studio imports it without conversion:

1. **File → Open** and select the repository root, not `androidApp/`.
2. Set **Gradle JDK** to 17 or newer (Settings → Build, Execution, Deployment →
   Build Tools → Gradle → Gradle JDK).
3. Wait for Gradle sync. `:androidApp` is the Android application module;
   `:shared` and `:webApp` are Kotlin Multiplatform modules.
4. Install SDK Platform 37 and Build Tools 36.0.0 when prompted, or run
   `sdkmanager "platforms;android-37.0" "build-tools;36.0.0"`.
5. Select the `androidApp` run configuration and run it on an emulator or
   device. The configuration is checked in as `.run/androidApp.run.xml`; any
   AVD from the Device Manager works (for example `Pixel_9_Pro`).

`local.properties` (SDK path and optional MatchUp config keys) and `.idea/` are
local files and stay out of version control. To verify the project from a
terminal, run `./gradlew build`; it builds the Android variants, runs lint and
produces the WASM distribution.

## Configuration

Copy `.env.example` to `.env` and fill in the values:

| Key | Meaning |
| --- | --- |
| `API_BASE_URL_ANDROID` | Absolute API URL for Android, including `/v1` |
| `API_BASE_URL_WEB` | `/v1` in production; relative paths are resolved against the page origin |
| `GOOGLE_WEB_CLIENT_ID` | Google OAuth **web** client ID, shared by Credential Manager and GIS |
| `WEB_PUSH_VAPID_PUBLIC_KEY` | Reserved for Web Push; not used yet |
| `ENABLE_FCM` | Reserved; Android push is not enabled yet |
| `APP_VERSION` | Informational version string |

Gradle also reads each key from `local.properties` (highest priority) or the
process environment. It generates
`shared/build/generated/matchupConfig/commonMain/kotlin/et/matchup/app/config/GeneratedConfig.kt`.
`.env` is gitignored; `.env.example` is committed.

Google setup:

1. Create a Google OAuth **Web application** client.
2. Add `https://app.matchup.et` as an authorized JavaScript origin.
3. Set the client ID in `GOOGLE_WEB_CLIENT_ID`.
4. Use the same client ID in the backend's `GOOGLE_OAUTH_CLIENT_ID`.
5. For Android, add the debug/release signing SHA-1 in the Google Cloud
   console. Credential Manager uses the web client ID as the server client ID.

## Run the PWA locally

```sh
cp .env.example .env
./gradlew :webApp:wasmJsBrowserDevelopmentRun
```

Open the printed URL (normally `http://localhost:9173`). The Kotlin webpack dev
server proxies `/v1/*` to `http://127.0.0.1:8082`, so the browser stays
same-origin and the backend does not need CORS.

Production build:

```sh
./gradlew :webApp:wasmJsBrowserDistribution
# output: webApp/build/dist/wasmJs/productionExecutable/
```

Serve the output over HTTPS. The service worker and PWA installation require a
secure origin (localhost is allowed for development). See `deploy/`.

## Build Android

```sh
./gradlew :androidApp:assembleDebug
./gradlew :androidApp:installDebug
```

`API_BASE_URL_ANDROID` defaults to `http://10.0.2.2:8082/v1` for the emulator.
For a physical phone use the computer's LAN address, for example
`http://192.168.1.20:8082/v1`, and keep both devices on the same network.
Cleartext HTTP is enabled only in the debug manifest. Release builds must use
HTTPS.

The Android modules compile against API 37 (`compileSdk = 37`) with
`targetSdk = 36` and `minSdk = 26`. Android Studio uses the same Gradle build,
so the IDE build and the CLI build produce identical output.

## Offline behavior

The PWA caches the app shell in a service worker and, after the first
launch, also caches the JS/WASM bundle so the app can relaunch offline. Data
snapshots are stored as JSON in localStorage; Android stores the same snapshots
in private SharedPreferences. Account-scoped keys include the user ID so one
account's bookings and notifications are never shown to another.

When offline:

- Cached games, bookings, booking details, notifications, profile, saved
  teammates and payment instructions remain readable.
- Search and catalog filters work against the cached catalog.
- Refresh attempts short-circuit to the cache instead of waiting for a network
  timeout.
- Reserve, payment, cancellation, profile and other write actions are disabled
  or fail immediately. Nothing is queued for later replay.
- Signing out while offline clears local tokens and caches. Logout-everywhere
  only reaches the server when online.

## Backend gaps in the current API

The client calls these account routes but the backend OpenAPI contract does not
yet expose them. Until they exist, the UI shows the server's error and keeps the
rest of the app usable.

| Client call | Proposed route | Status |
| --- | --- | --- |
| Profile update | `PATCH /me` | Not in API |
| Send email verification | `POST /auth/email/verify/request` | Not in API |
| Confirm email verification | `POST /auth/email/verify/confirm` | Not in API |
| Player booking cancellation | `POST /bookings/{id}/cancel` | Not in API |
| QR check-in verification | — | No endpoint; QR pass is display-only |

Payment submissions are real and use the existing
`POST /bookings/{id}/payment` route. Cancellation is intentionally limited to
`pending_payment` in the UI; paid/confirmed cancellation needs a refund policy
before it can be exposed.

## Distribution notes

- **PWA:** `app.matchup.et` with HTTPS. On iPhone/iPad, open in Safari, Share →
  Add to Home Screen. The manifest requests standalone display.
- **Browser support:** the Compose WASM bundle needs WebAssembly GC. Use Safari
  18.2+ (iOS 18.2+) or a current Chrome/Edge/Firefox; older iOS versions cannot
  run this PWA target.
- **Android:** build the release bundle, sign it, upload to the Play Console or
  distribute directly. Add the release SHA-256 to
  `.well-known/assetlinks.json` for App Links (see `deploy/README.md`).
- **Push:** notification polling is implemented now. FCM for Android and Web
  Push/VAPID for iOS 16.4+ installed PWAs are reserved but not wired yet.
- **QR:** the pass encodes `MATCHUP:<referenceCode>` and is for display until
  the backend exposes a check-in endpoint.

## Verification status

- The Gradle wrapper is committed; `./gradlew` runs Gradle 9.7.1.
- `:shared:compileKotlinWasmJs` succeeds with no warnings.
- The WASM production distribution builds with
  `:webApp:wasmJsBrowserDistribution` (about 16 MB of JS/WASM output).
- `:androidApp:assembleDebug` and `:androidApp:assembleRelease` succeed with
  `compileSdk = 37` and Build Tools 36.0.0 against the local Android SDK.
- `./gradlew build` passes: Android debug/release packaging, lint and the WASM
  distribution.
- The PWA dev server on port 9173 serves the app shell and proxies `/v1` to
  `127.0.0.1:8082`; the proxy was verified with a local stub, not the real
  backend.

Before shipping, run through this smoke test online and offline:

1. Sign up/login and confirm the profile appears.
2. Load games, search, filter, open a game and reserve.
3. Submit a payment reference and verify the booking reaches `verifying`.
4. View the QR pass and copy the reference.
5. Reload the PWA in airplane mode and confirm cached games/bookings open.
6. Trigger a notification, check the badge, mark it read.
7. Edit profile / change password (profile edit requires `PATCH /me`).
8. Trigger and complete a password reset link.
9. Install the PWA on iOS and verify safe areas and standalone launch.
