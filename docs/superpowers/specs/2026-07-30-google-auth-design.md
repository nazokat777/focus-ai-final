# Focus AI — Google orqali kirish + bulut sinxron (dizayn)

Sana: 2026-07-30
Yondashuv: **A — Supabase Auth (Google provider) + tizim brauzeri + deep-link**

## Maqsad (foydalanuvchi so'rovi)
1. Google orqali ro'yxatdan o'tish — **musobaqa sharti**
2. Ma'lumot bulutda saqlansin — parol/qurilma yo'qolsa tiklanadi
3. Do'stlar bo'limi uchun haqiqiy kimlik (ism + avatar)

## Asosiy prinsip: ADDITIVE
Mavjud `code` (ABC123) asosidagi kimlik va `profiles/daily_stats/links/messages`
tizimi **buzilmaydi**. Google login ustiga qo'shiladi. Login **ixtiyoriy** —
kirmasa ilova hozirgidek oflayn ishlaydi (offline-first saqlanadi).

## Arxitektura
```
Focus AI (web/APK) --Google tugmasi--> tizim brauzeri (Google consent)
       ^                                        |
       |------------- deep-link -----------------
       |         (uz.lumnaara.focusai://auth yoki web redirect)
       v
Supabase Auth (Google) -> user.id, email, name, avatar_url
       |
       v
user_data jadvali (uid -> store JSON) <-> localStorage (last-write-wins)
```

## Komponentlar
1. **Auth modul** (`www/index.html` ichida IIFE, mavjud Supabase bloki yonida)
   - `signInGoogle()`: web -> `supabase.auth.signInWithOAuth({provider:'google', options:{redirectTo}})`;
     native (Capacitor) -> Browser plugin bilan OAuth URL ochib, `appUrlOpen` (deep-link) da
     `exchangeCodeForSession` / `setSession`.
   - `signOut()`, `currentUser()`, `onAuthChange()`.
   - Web redirectTo = joriy origin; native redirectTo = `uz.lumnaara.focusai://auth`.
2. **Bulut sinxron modul**
   - Jadval `user_data`: `{ user_id uuid PK (auth.uid), data jsonb, updated_at timestamptz }`, RLS: faqat egasi.
   - `pushStore()` (debounce ~2s, save() ichidan): agar kirgan bo'lsa `upsert`.
   - `pullStore()`: kirganda o'qib, birlashtirish qoidasi bilan qo'llaydi.
   - Birlashtirish: 1-login — bulut bo'sh -> local push; bulut bor -> pull (localga `store_backup_preauth` zaxira).
3. **Kimlik ulash**
   - Kirganda `localStorage['focusai.userName']` = Google display name (agar bo'sh bo'lsa),
     `profiles` upsert `{code, name, avatar_url}`. Avatar do'stlar ro'yxatida ko'rsatiladi.
   - Ulashadigan `code` o'zgarmaydi (do'stlar aloqasi saqlanadi).
4. **UI**
   - Sozlamalar tepasida: kirmagan -> "Google bilan kirish" tugmasi; kirgan -> avatar + ism + email + "Chiqish".
   - Minimalist (yangi ekran yo'q, mavjud sozlamalar varag'iga qo'shiladi).
   - i18n uz/ru/en.

## Deep-link (native)
`AndroidManifest.xml` MainActivity'ga intent-filter:
`<data android:scheme="uz.lumnaara.focusai" android:host="auth"/>` (BROWSABLE).
Capacitor `App.addListener('appUrlOpen')` -> URL'dan kod/tokenni olib sessiya o'rnatadi.

## Xatolik boshqaruvi
- Internet yo'q / Supabase yo'q -> login tugmasi ishlamaydi (toast), ilova oflayn davom etadi.
- OAuth bekor qilinsa -> jim qaytadi.
- Sync xatosi -> local saqlanadi, keyingi saqlashda qayta urinadi (soxta holat yo'q).

## Foydalanuvchi tomonidan sozlash (men qila olmayman — akkaunt kirishi)
1. **Google Cloud Console**: OAuth consent (External, Testing), OAuth client (Web),
   redirect URI = `https://ursuqwotkpczjrqnvoad.supabase.co/auth/v1/callback`.
2. **Supabase**: Authentication -> Providers -> Google -> Enable, Client ID/Secret joylash.
   URL Configuration -> Redirect URLs: sayt URL + `uz.lumnaara.focusai://auth`.
3. **SQL** (Supabase SQL editor): `user_data` jadvali + RLS (kod berilади).

## YAGNI (qilinmaydi)
- Har bir odatni alohida birlashtirish (real-time merge) — kerak emas, LWW yetarli.
- Majburiy login / mehmon rejimini o'chirish — foydalanuvchi so'ramadi.
- Email/parol, boshqa providerlar — faqat Google.
```
