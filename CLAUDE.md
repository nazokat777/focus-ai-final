# Focus AI — loyiha qoidalari

## Loyiha haqida
Vaqtga asoslangan odatlar kuzatuvchisi. Konkurs ishi (buyurtmachi: Shamsuddeen).
Asosiy farq: boshqa ilovalar "qildingmi?" deb so'raydi — **Focus AI haqiqiy sarflangan vaqtni hisoblaydi**.

## Stack
- Bitta HTML fayl: `www/index.html` (framework yo'q, build yo'q)
- Capacitor 6 → Android + iOS
- Barcha ma'lumot lokal: `localStorage`. Offline ishlaydi.
- Ovozlar WebAudio'da **sintezlanadi** — audio fayl ishlatma.

## ⛔ QAT'IY QOIDALAR

1. **DIZAYNGA TEGMA.** Rang, tipografika, layout, animatsiya — hammasi tasdiqlangan va muzlatilgan.
   Neon-yashil (`#39FF8C`) qora fonda. Space Grotesk + JetBrains Mono.
   Faqat funksiya va mantiq ustida ishla. Dizayn o'zgarishi kerak bo'lsa — avval **so'ra**.

2. **Taymer formulasiga tegma.** TZ talabi:
   `elapsed = accMs + (now − runningSince)`
   setInterval sanog'i EMAS. Manfiy vaqt yo'q, maqsaddan oshmaydi, parallel sessiyalar mustaqil.
   O'zgartirsang — avval test yoz.

3. **Halol bo'l.** Soxta statistika yo'q, dark pattern yo'q. Faqat bugungi kun belgilanadi, o'tmish soxtalanmaydi.

4. **Uch til.** UZ (asosiy) / RU / EN. Yangi matn qo'shsang — uchalasiga ham qo'sh (`I18N` obyekti).

## Kod uslubi
- ES5-uslub (`var`, `function`) — eski WebView'lar uchun
- Foydalanuvchi kiritgan matn → `esc()` dan o'tkaz (XSS)
- Yangi kod yozgach: `node --check` bilan sintaksisni tekshir

## Til
Men bilan **o'zbek tilida** gaplash. Qisqa va aniq. Xato qilsang — to'g'ridan-to'g'ri ayt, uzr so'rab o'tirma.
