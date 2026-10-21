# TODO

Deferred work agreed during the light-mode / UX pass. `UI_UX.md` sections 1–12 remain the design reference; section 13 records the as-built state.

## Deferred settings rows

Profile intentionally has no rows for these yet; do not add placeholder screens.

- [ ] **Language** — needs an actual locale switch (Amharic + English) and `LocalStrings` provider. Amharic copy must come from a native reviewer.
- [ ] **Help & support** — needs a destination (FAQ, support ticket entry point, or `mailto:`/WhatsApp). Decide channel before building the row.
- [ ] **Notification settings** — needs backend support for per-user notification preferences and push permission handling (Android 13+ `POST_NOTIFICATIONS`, web Notification API).

## Known gaps

- [ ] Amharic localization (Ethiopic script font, pluralization, date/number formatting).
- [ ] In-app theme switch (light / dark / system). Currently follows the OS only.
- [ ] Refund and cancellation UX depends on business policy; only the cancel-while-pending flow exists.
- [ ] Ethiopian calendar display option.
- [ ] Saved teammates / "pick from past teammates" when adding names.
- [ ] Reminder notifications before a booked game.
- [ ] Coordinates for fields are not exposed by the API; Directions falls back to a maps text search.
- [ ] Desktop/web layout is capped at a 620 dp frame; no tablet-specific layout yet.

## Maintenance

- [ ] Bump `CACHE_VERSION` in `webApp/src/wasmJsMain/resources/sw.js` whenever shipped web assets change.
- [ ] Keep `MatchUpColors` token names stable; tests/docs reference them.
- [ ] Re-run `.\gradlew.bat build` after dependency bumps (AGP is pinned at the Android Studio maximum, 9.1.1).
