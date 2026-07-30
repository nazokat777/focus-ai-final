/* Focus AI — Service Worker
   Maqsad: internet BO'LSA eng oxirgi versiyani ko'rsatish (auto-yangilanish),
   internet BO'LMASA keshdan ishlash (oflayn). Bridge server.url origin'da ishlaydi. */
var CACHE = 'focusai-v2';
var CORE = [
  './',
  'index.html',
  'landing.html',
  'assets/vendor/gsap.min.js',
  'assets/vendor/ScrollTrigger.min.js',
  'assets/vendor/MotionPathPlugin.min.js',
  'assets/vendor/lenis.min.js',
  'assets/fonts/fonts.css'
];

self.addEventListener('install', function(e){
  self.skipWaiting();
  e.waitUntil(
    caches.open(CACHE).then(function(c){
      /* yadroni oldindan keshlaymiz; xato bo'lsa ham install to'xtamasin */
      return Promise.all(CORE.map(function(u){
        return c.add(u).catch(function(){});
      }));
    })
  );
});

self.addEventListener('activate', function(e){
  e.waitUntil(
    caches.keys().then(function(keys){
      return Promise.all(keys.map(function(k){
        if(k !== CACHE) return caches.delete(k); /* eski keshlarni tozalash */
      }));
    }).then(function(){ return self.clients.claim(); })
  );
});

self.addEventListener('fetch', function(e){
  var req = e.request;
  if(req.method !== 'GET') return;
  var url = new URL(req.url);
  if(url.origin !== self.location.origin) return; /* faqat o'z origin */

  var isHTML = req.mode === 'navigate' ||
               (req.headers.get('accept') || '').indexOf('text/html') !== -1;

  if(isHTML){
    /* HTML: avval tarmoq (yangilanish uchun), bo'lmasa kesh (oflayn) */
    e.respondWith(
      fetch(req).then(function(res){
        var copy = res.clone();
        caches.open(CACHE).then(function(c){ c.put(req, copy); });
        return res;
      }).catch(function(){
        return caches.match(req).then(function(m){
          return m || caches.match('index.html');
        });
      })
    );
    return;
  }

  /* Boshqa resurslar — STALE-WHILE-REVALIDATE: keshdan darhol ber (tez),
     fonда tarmoqdan yangilab keshni yangila (keyingi safar yangi). Oflaynда kesh. */
  e.respondWith(
    caches.open(CACHE).then(function(cache){
      return cache.match(req).then(function(cached){
        var net = fetch(req).then(function(res){
          if(res && res.status === 200 && (res.type === 'basic' || res.type === 'default')){
            cache.put(req, res.clone());
          }
          return res;
        }).catch(function(){ return cached; });
        return cached || net;   /* kesh bo'lsa darhol, yo'q bo'lsa tarmoqni kut */
      });
    })
  );
});
