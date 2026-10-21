/* MatchUp PWA service worker.
 *
 * The app shell is cached for offline launch. API traffic under /v1 is never
 * cached here; the Compose client keeps account-scoped JSON snapshots in
 * localStorage and decides when cached data is shown.
 *
 * Bump CACHE_VERSION on every deployment so old assets are evicted.
 */
const CACHE_VERSION = 'matchup-v2';
const SHELL_CACHE = `${CACHE_VERSION}-shell`;
const ASSET_CACHE = `${CACHE_VERSION}-assets`;
const SHELL = [
  '/',
  '/index.html',
  '/styles.css',
  '/manifest.webmanifest',
  '/icons/favicon-32.png',
  '/icons/apple-touch-icon.png',
  '/icons/icon-192.png',
  '/icons/icon-512.png',
  '/icons/icon-maskable-512.png'
];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(SHELL_CACHE).then((cache) =>
      Promise.allSettled(SHELL.map((url) => cache.add(url)))
    ).then(() => self.skipWaiting())
  );
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((keys) =>
      Promise.all(
        keys
          .filter((key) => key !== SHELL_CACHE && key !== ASSET_CACHE)
          .map((key) => caches.delete(key))
      )
    ).then(() => self.clients.claim())
  );
});

function isApi(url) {
  return url.pathname === '/v1' || url.pathname.startsWith('/v1/');
}

function isNavigation(request) {
  return request.mode === 'navigate' ||
    (request.method === 'GET' && request.headers.get('accept')?.includes('text/html'));
}

async function networkFirstNavigation(request) {
  try {
    const response = await fetch(request);
    if (response && response.ok) {
      const cache = await caches.open(SHELL_CACHE);
      cache.put('/index.html', response.clone());
    }
    return response;
  } catch (_) {
    return (await caches.match('/index.html')) || (await caches.match('/')) ||
      new Response('MatchUp is offline. Reconnect once to finish installing the app.', {
        status: 503,
        headers: { 'Content-Type': 'text/plain; charset=utf-8' }
      });
  }
}

async function staleWhileRevalidate(event, request) {
  const cache = await caches.open(ASSET_CACHE);
  const cached = await cache.match(request);
  const network = fetch(request).then((response) => {
    if (response && response.ok && response.type !== 'opaque') {
      cache.put(request, response.clone());
    }
    return response;
  }).catch(() => null);
  event.waitUntil(network);
  return cached || (await network) || caches.match(request);
}

async function cacheFirst(request) {
  const cached = await caches.match(request);
  if (cached) return cached;
  const response = await fetch(request);
  if (response && response.ok) {
    const cache = await caches.open(ASSET_CACHE);
    cache.put(request, response.clone());
  }
  return response;
}

self.addEventListener('message', (event) => {
  const data = event.data;
  if (!data || data.type !== 'CACHE_URLS' || !Array.isArray(data.urls)) return;

  const urls = data.urls.filter((value) => {
    if (typeof value !== 'string') return false;
    try {
      return new URL(value, self.location.origin).origin === self.location.origin;
    } catch (_) {
      return false;
    }
  });
  if (urls.length === 0) return;

  event.waitUntil(
    caches.open(ASSET_CACHE).then((cache) =>
      Promise.allSettled(urls.map((url) => cache.add(url)))
    )
  );
});

self.addEventListener('fetch', (event) => {
  const request = event.request;
  if (request.method !== 'GET') return;

  const url = new URL(request.url);
  if (url.origin !== self.location.origin || isApi(url)) return;
  if (url.pathname === '/sw.js') return;

  if (isNavigation(request)) {
    event.respondWith(networkFirstNavigation(request));
    return;
  }

  if (/\.(?:png|jpg|jpeg|svg|webp|ico|woff2?|ttf)$/i.test(url.pathname)) {
    event.respondWith(cacheFirst(request));
    return;
  }

  if (/\.(?:js|mjs|wasm|css)$/i.test(url.pathname)) {
    event.respondWith(staleWhileRevalidate(event, request));
    return;
  }

  event.respondWith(staleWhileRevalidate(event, request));
});
