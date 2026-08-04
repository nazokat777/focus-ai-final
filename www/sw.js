/* Focus AI — Service Worker (GIBRID)
   Maqsad:
     • OFLAYN KAFOLAT — APK ichidagi (bundled) fayllar doim mavjud, internetsiz to'liq ishlaydi.
     • AVTO-YANGILANISH — internet bo'lsa saytdan (Vercel) eng oxirgi kod olinadi va keshlanadi.
   Native APK'da origin = https://localhost (bundled), web'da = vercel origin. */
var CACHE  = 'focusai-v10';  /* Web auth diagnostikasi: xato/almashinuv natijasini toast qiladi */
var REMOTE = 'https://focus-ai-final.vercel.app';
var NET_TIMEOUT = 4000;          /* sekin internet ilovani ushlab qolmasin */

var CORE = [
  './','index.html','landing.html','about.html',
  'assets/vendor/gsap.min.js','assets/vendor/ScrollTrigger.min.js',
  'assets/vendor/MotionPathPlugin.min.js','assets/vendor/lenis.min.js','assets/vendor/supabase.min.js',
  'assets/fonts/fonts.css'
];

function isNative(){ return self.location.origin !== REMOTE; }
function shellKey(path){ return self.location.origin + '/__shell__/' + path; }

/* vaqt chegarali fetch — javob kelmasa keshga tushamiz */
function fetchTimeout(req, ms){
  return new Promise(function(resolve, reject){
    var done = false;
    var t = setTimeout(function(){ if(!done){ done = true; reject(new Error('timeout')); } }, ms);
    fetch(req).then(function(r){ if(!done){ done = true; clearTimeout(t); resolve(r); } },
                    function(e){ if(!done){ done = true; clearTimeout(t); reject(e); } });
  });
}

self.addEventListener('install', function(e){
  self.skipWaiting();
  e.waitUntil(caches.open(CACHE).then(function(c){
    return Promise.all(CORE.map(function(u){ return c.add(u).catch(function(){}); }));
  }));
});

self.addEventListener('activate', function(e){
  e.waitUntil(
    caches.keys().then(function(keys){
      return Promise.all(keys.map(function(k){ if(k !== CACHE) return caches.delete(k); }));
    }).then(function(){ return self.clients.claim(); })
  );
});

self.addEventListener('fetch', function(e){
  var req = e.request;
  if(req.method !== 'GET') return;
  var url = new URL(req.url);
  if(url.origin !== self.location.origin) return;   /* faqat o'z origin */

  /* VIDEO/MEDIA — SW UMUMAN ARALASHMAYDI.
     Brauzer video uchun Range so'rovi yuboradi (bytes=0-...). Kesh esa to'liq 200
     javob qaytaradi — natijada video O'YNAMAYDI, faqat poster ko'rinadi.
     Shuning uchun media so'rovlarini tarmoqqa to'g'ridan-to'g'ri qo'yib yuboramiz. */
  if(req.headers.get('range') || /\.(mp4|webm|ogg|mov|m4v|mp3|wav)$/i.test(url.pathname)) return;

  var isHTML = req.mode === 'navigate' ||
               (req.headers.get('accept') || '').indexOf('text/html') !== -1;

  /* ---------- HTML (ilova qobig'i): SAYTDAN yangi kod -> kesh -> APK ichidagi nusxa ---------- */
  if(isHTML){
    var path = url.pathname.replace(/^\//,'') || 'index.html';
    var remoteReq = isNative()
      ? new Request(REMOTE + '/' + path, { mode:'cors', credentials:'omit', cache:'no-store' })
      : req;
    e.respondWith(
      fetchTimeout(remoteReq, NET_TIMEOUT).then(function(res){
        if(!res || !res.ok) throw new Error('bad');
        var copy = res.clone();
        caches.open(CACHE).then(function(c){ c.put(shellKey(path), copy); });
        return res;
      }).catch(function(){
        /* internet yo'q / sekin -> oxirgi keshlangan yangi kod */
        return caches.match(shellKey(path)).then(function(m){
          if(m) return m;
          /* u ham yo'q -> APK ichidagi original (OFLAYN KAFOLAT) */
          return fetch(req).catch(function(){
            return caches.match(path).then(function(k){ return k || caches.match('index.html'); });
          });
        });
      })
    );
    return;
  }

  /* ---------- Resurslar: kesh -> lokal (APK ichi) -> saytdan (yangi fayllar uchun) ---------- */
  e.respondWith(
    caches.open(CACHE).then(function(cache){
      return cache.match(req).then(function(cached){
        var net = fetch(req).then(function(res){
          if(res && res.ok){ cache.put(req, res.clone()); return res; }
          if(isNative()){                     /* APK ichida yo'q -> saytdan olib kelamiz */
            return fetch(REMOTE + url.pathname, { mode:'cors', credentials:'omit' })
              .then(function(r2){ if(r2 && r2.ok) cache.put(req, r2.clone()); return r2; });
          }
          return res;
        }).catch(function(){ return cached; });
        return cached || net;                 /* kesh bo'lsa darhol, fonda yangilanadi */
      });
    })
  );
});
