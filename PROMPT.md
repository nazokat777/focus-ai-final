# Claude Code'ga tashlanadigan birinchi vazifa

Quyidagi matnni nusxa ko'chirib, Claude Code'ga tashla:

---

Bu Capacitor 6 loyihasi. `www/index.html` ichida tayyor mobil ilova bor (Focus AI).
CLAUDE.md faylini o'qi — loyiha qoidalari o'sha yerda.

Vazifa: **Android debug APK yasab ber.**

Qadamlar:
1. Kerakli narsalar bor-yo'qligini tekshir (Node, Java JDK 17, Android SDK).
   Yo'q bo'lsa — menga nima o'rnatish kerakligini aniq ayt, o'zing urinib ko'rma.
2. `npx cap add android`
3. `npx cap sync`
4. `cd android && ./gradlew assembleDebug`
5. APK yo'lini menga ayt.

Muhim: dizaynga tegma. Xato chiqsa — sababini tushuntirib, tuzatishni taklif qil.

---

## Keyingi vazifalar uchun namunalar

**Xato tuzatish:**
```
Fokus tabidagi taymer pauzadan keyin noto'g'ri vaqt ko'rsatyapti.
Sababini top, tuzat, keyin node --check bilan tekshir.
```

**Yangi funksiya:**
```
Odat kartasini chapga surganda o'chirish tugmasi chiqsin (swipe-to-delete).
Dizayn tiliga mos bo'lsin — neon-yashil, mavjud animatsiya uslubida.
```

**Skill bilan:**
```
/impeccable audit www/index.html
```

**Test:**
```
Playwright bilan ilovani ochib, quyidagilarni tekshir:
- Onboarding 3 slayd o'tadimi
- Mehmon sifatida kirish ishlaydimi
- Odat qo'shish → taymer boshlash → 5 soniyadan keyin vaqt o'zgardimi
Ekran rasmlarini olib ber.
```
