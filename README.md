# Focus AI 🌱

Vaqtga asoslangan odatlar kuzatuvchisi. Boshqa ilovalar "qildingmi?" deb so'raydi — Focus AI **haqiqiy sarflangan vaqtni** hisoblaydi.

**Stack:** HTML/CSS/JS (framework'siz, bitta fayl) + Capacitor 6 (Android + iOS). Barcha ma'lumotlar lokal (localStorage), offline ishlaydi. Ovozlar WebAudio'da sintezlanadi — bitta ham audio fayl yo'q.

## Funksiyalar

- ⏱ **Timestamp-asosli taymer** — `elapsed = accMs + (now − runningSince)`. Ilova yopilsa/telefon uxlasa ham aniq. Bir nechta odat parallel ishlaydi.
- ✓ **Ikki tur odat** — vaqtli (maqsad soat) va belgilash (kunlik check).
- 🌳 **O'sish daraxti** — barcha odatlar progressidan jonli o'sadi (canvas, generativ).
- 🏆 **Natija kartasi** — sessiya yakunida: vaqt, streak, jami soat, daraja + **brendlangan share-rasm** (Telegram/Instagram).
- 🎖 **Mastery** — 6 daraja, hikmat mozaikasi, generativ "fokus izi" san'ati.
- 🏅 **12 milestone nishon** — streak 3/7/14/30/100/365 + soat 1/10/50/100/500/1000, glow-reveal.
- 🌧 **8 ambient ovoz** — yomg'ir, gulxan, okean, qushlar, soy, shamol, tun, oq shovqin.
- ⏰ **Matematikali budilnik** — to'xtatish uchun misol yechasiz; 2 daqiqada favqulodda chiqish.
- 🔒 **PIN qulf** — SHA-256 hash.
- 🤖 **AI murabbiy** — statistikangizga qarab motivatsiya (offline fallback bilan).
- 🔔 Kunlik eslatma, 100% da bildirishnoma.
- 🌐 UZ / RU / EN · ☀️ Yorug'/qorong'i mavzu · 📴 Telefonsiz rejim (yuztuban → +10% bonus).

## Ishga tushirish (dev)

```bash
npm install
npx cap add android
npx cap add ios        # faqat macOS'da
npx cap sync
```

## Android APK

```bash
npx cap open android   # Android Studio ochiladi
# Build → Build Bundle(s)/APK(s) → Build APK(s)
# APK: android/app/build/outputs/apk/debug/app-debug.apk
```

Terminaldan: `cd android && ./gradlew assembleDebug`

## iOS

```bash
npx cap open ios       # Xcode ochiladi (macOS kerak)
# Signing → jamoangizni tanlang → Run yoki Archive (TestFlight)
```

## Eslatmalar

- Kod o'zgartirsangiz: `www/index.html` ni yangilab, `npx cap sync` bajaring.
- iOS'da `navigator.vibrate` yo'q — haptics allaqachon graceful fallback qiladi; xohlasangiz `@capacitor/haptics` bilan kuchaytirish mumkin (dependency qo'shilgan).
- AI murabbiy: mobil buildda o'z API kalitingiz bilan (Gemini/Groq bepul tier) proxy qo'shing yoki offline hikmatlar rejimida qoldiring.

— Lumnaara³ Studio · Improve Yourself Every Day
