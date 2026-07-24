# Focus AI — iOS build qo'llanmasi

Bu hujjat Focus AI ilovasini iOS uchun qanday yig'ishni tushuntiradi. Siz Windows'da ishlayapsiz, shuning uchun asosiy urg'u **bulutli (Codemagic)** yo'lga qaratilgan.

---

## 1. Umumiy holat

Kod allaqachon iOS'ga **tayyor**. Sabab:

- Ilova bitta statik fayldan iborat: `www/index.html` (framework yo'q, web uchun build qadami yo'q — `webDir=www` shundoqcha ishlaydi).
- Capacitor 6.1.0 sozlangan. `capacitor.config.json`:
  - `appId`: `uz.lumnaara.focusai`
  - `appName`: `Focus AI`
  - `webDir`: `www`
  - `ios.contentInset`: `always`
- iOS pluginlari `package.json`'da bor: `@capacitor/ios`, `haptics`, `local-notifications`, `share`, `splash-screen`.

**Qolgan yagona ish — iOS build.** Uni yig'ish uchun macOS + Xcode kerak.

> ⚠️ **Windows'da iOS build QILIB BO'LMAYDI.** Apple'ning Xcode'i faqat macOS'da ishlaydi. Ikki yo'l bor:
> - **YO'L A — Codemagic (bulut, Mac shart emas)** — tavsiya etiladi.
> - **YO'L B — o'zingizning yoki ijaraga olingan Mac'ingiz.**

`android/` papkasi allaqachon mavjud (release keystore bilan). `ios/` papkasi **hali yo'q** — u Mac/bulut runnerda `npx cap add ios` bilan yaratiladi.

---

## 2. YO'L A — Codemagic (bulut, tavsiya)

Mac'siz, to'g'ridan-to'g'ri Git rep'dan iOS build oling. Codemagic'da oyiga bepul minutlar bor.

### 2.1. `codemagic.yaml` allaqachon tayyor

`codemagic.yaml` repo ildizida **men tomonimdan yaratilgan** (`package.json` yonida). Uni o'zgartirish shart emas. Ichida **ikkita workflow** bor:

- **`ios-simulator-unsigned`** — imzosiz iOS Simulator debug build. Apple Developer akkaunti **kerak emas**, hoziroq ishlaydi. Natija: `.app` + build loglari.
- **`ios-appstore-signed`** — imzolangan `.ipa` (TestFlight/App Store). Faqat Apple Developer + Codemagic App Store Connect integratsiyasi sozlangach ishlaydi (6-bo'limga qarang). Sozlanmaguncha 1-workflow'ga xalaqit bermaydi.

> ⚠️ **Old shart:** Codemagic Git provayder orqali ishlaydi. Repo hozircha faqat lokal — Codemagic ko'rishi uchun avval uni **GitHub** (yoki GitLab/Bitbucket) ga yuklashingiz kerak:
> ```bash
> git remote add origin https://github.com/<siz>/focus-ai-final.git
> git push -u origin main
> ```
> (`git push` ni o'zingiz bajarasiz — men push qila olmayman.)

### 2.2. Codemagic'ni sozlang

1. [codemagic.io](https://codemagic.io) ga kiring (GitHub/GitLab/Bitbucket akkaunti bilan).
2. **Add application** → Git provayderni tanlang → Focus AI rep'ini ulang.
3. Loyiha turi so'ralsa: **Capacitor** (yoki "Other/YAML") tanlang.
4. Codemagic repo ildizidagi `codemagic.yaml`'ni avtomat o'qiydi.
5. **Start new build** → workflow: `ios-imzosiz` → boshlang.

### 2.3. Natijani yuklab oling

Build tugagach (~10–20 daqiqa), **Artifacts** bo'limidan `.app` faylini yuklab olasiz. Bu **imzosiz** build — u faqat simulyator/ichki tekshiruv uchun. Haqiqiy iPhone'ga o'rnatish yoki TestFlight uchun **imzo kerak** (4-bo'limga qarang).

> **Eslatma:** Imzosiz build App Store'ga chiqmaydi va oddiy iPhone'ga o'rnatilmaydi. U — kod to'g'ri yig'ilishini isbotlash uchun. Haqiqiy tarqatish uchun Apple Developer akkaunti orqali imzolash shart.

---

## 3. YO'L B — O'z Mac'ingizda

Agar sizda macOS bo'lsa (yoki ijaraga olsangiz), Xcode orqali to'g'ridan-to'g'ri yig'asiz.

### Talablar
- macOS + **Xcode** (App Store'dan bepul).
- **CocoaPods**: `sudo gem install cocoapods`.
- **Node.js** (LTS).

### Qadamlar

```bash
# 1. Reponi klonlab, ichiga kiring, so'ng:
npm install

# 2. iOS platformasini qo'shing (ios/ papkasini yaratadi)
npx cap add ios

# 3. Web fayllarni sinxronlang
npx cap sync ios

# 4. Xcode'da oching
npx cap open ios
```

Xcode ochilgach:
1. Yuqori chapdan qurilma yoki simulyatorni tanlang.
2. **Run** (▶) tugmasini bosing.
3. Ilova simulyator/qurilmada ishga tushadi.

> Har safar `www/index.html`'ni o'zgartirsangiz, `npx cap sync ios` ni qayta ishlating.

---

## 4. Imzolash / TestFlight / App Store

Ilovani boshqalarga tarqatish (haqiqiy iPhone, TestFlight, App Store) uchun kerak:

| Nima kerak | Izoh |
|---|---|
| **Apple Developer Program** | Yiliga **$99**. [developer.apple.com](https://developer.apple.com) da ro'yxatdan o'ting. |
| **App ID** | `uz.lumnaara.focusai` — App Store Connect / Developer portalda ro'yxatga olinadi. |
| **Signing sertifikati** | Distribution certificate (Apple beradi). |
| **Provisioning profile** | App ID + sertifikatga bog'lanadi. |
| **App Store Connect yozuvi** | Ilova nomi, ikonka, skrinshotlar, tavsif, maxfiylik siyosati. |

### Tarqatish variantlari
- **TestFlight** — sinovchilarga (100 tagacha ichki, 10 000 tagacha tashqi) tarqatish. App Store tekshiruvi yengilroq.
- **App Store** — ommaviy chiqarish. To'liq App Review jarayonidan o'tadi.

### Codemagic bilan imzolash
Codemagic imzolashni ham avtomatlashtiradi: App Store Connect API kalitini (`.p8`) va sertifikatlarni Codemagic'ga qo'shib, `codemagic.yaml`'da `ios_signing` va TestFlight publishing blokini yoqasiz. Bu bosqichni imzosiz build ishlaganidan **keyin** qo'shish qulayroq.

> Android tomonда release keystore allaqachon tayyor (`KEYSTORE.md` ga qarang). iOS uchun esa yuqoridagi Apple sertifikatlari alohida kerak — ular bir-biriga bog'liq emas.

---

## 5. Sizdan qanday qaror talab qilinadi

Davom etishdan oldin bitta savolga javob bering:

**A) Faqat imzosiz sinov build kifoyami?**
→ **YO'L A (Codemagic)** yoki **YO'L B (Mac)** yetarli. Apple Developer akkaunti ($99) **shart emas**. Natija: kod iOS'da yig'ilishini ko'rasiz, lekin oddiy iPhone'ga o'rnatib bo'lmaydi.

**B) Ilovani App Store / TestFlight'ga chiqarasizmi?**
→ **Apple Developer Program ($99/yil) shart.** So'ng 4-bo'limdagi sertifikat/profil/App Store Connect qadamlari bajariladi. Codemagic bu jarayonni ham avtomatlashtira oladi.

---

### Qisqa xulosa
1. Kod tayyor — faqat build qoldi.
2. Windows'da iOS build bo'lmaydi → **Codemagic** (tavsiya) yoki **Mac**.
3. Imzosiz sinov uchun Apple akkaunti kerak emas.
4. App Store/TestFlight uchun **$99/yillik Apple Developer** + sertifikatlar kerak.

---

## 6. Imzolash — Codemagic bilan batafsil (yamldagi 2-workflow)

IMZOLANGAN IPA (App Store / TestFlight) UCHUN QADAMLAR — yamldagi 2-workflow (ios-appstore-signed) shu sozlash tugagach ishlaydi:

1) APPLE DEVELOPER PROGRAM ($99/yil, majburiy)
   - developer.apple.com/programs → ro'yxatdan o't. Individual yoki Organization.
   - App Store Connect'da yangi app yarat: Bundle ID = uz.lumnaara.focusai (capacitor.config.json'dagi appId bilan aynan bir xil). Identifiers bo'limida shu App ID'ni oldindan ro'yxatdan o'tkaz.

2) APP STORE CONNECT API KEY (Codemagic imzolash uchun eng oson yo'l)
   - App Store Connect → Users and Access → Integrations (Keys) → App Store Connect API → yangi kalit yarat (Access: App Manager yoki Admin).
   - Yuklab ol: Issuer ID, Key ID, va .p8 private key fayli (faqat bir marta yuklanadi — saqla).

3) CODEMAGIC'DA INTEGRATSIYA (yamldagi `focusai-asc` nomi shu yerdan)
   - Codemagic → Teams/Personal → Settings → Integrations → App Store Connect → "Add key".
   - Integration name'ni AYNAN `focusai-asc` qilib qo'y (yamlda shu nom yozilgan; boshqa nom bersang yamldagi `integrations.app_store_connect: focusai-asc` va publishing `auth: integration` shu nomga mos bo'lishi kerak).
   - Issuer ID, Key ID, .p8 faylni kirit.
   Bu integratsiya build vaqtida signing sertifikat va provisioning profillarni AVTOMATIK yaratadi/oladi — `xcode-project use-profiles` shundan foydalanadi. Qo'lda .p12/.mobileprovision yuklash shart emas (automatic signing).

4) YAML'DAGI TAYYOR BLOKLAR (allaqachon yozilgan, o'zgartirish shart emas):
   - integrations: { app_store_connect: focusai-asc }
   - environment.ios_signing: { distribution_type: app_store, bundle_identifier: uz.lumnaara.focusai }
   - "xcode-project use-profiles" qadami (imzo sozlamalarini Xcode loyihasiga qo'llaydi)
   - "xcode-project build-ipa --workspace App.xcworkspace --scheme App" (imzolangan .ipa quradi → ios/App/build/ios/ipa/*.ipa)
   - publishing.app_store_connect: { auth: integration, submit_to_testflight: true, submit_to_app_store: false }
     TestFlight'ga avtomatik yuklaydi. App Store'ga to'liq chiqarish uchun submit_to_app_store: true qil.

5) QO'SHIMCHA (ixtiyoriy, lekin tavsiya):
   - beta_groups qo'shish: publishing.app_store_connect ostiga
       beta_groups:
         - "Internal Testers"
     (App Store Connect'da shu nomli tester guruh oldindan yaratilgan bo'lishi kerak.)
   - Aniq build raqami App Store'dan olinishi uchun `agvtool new-version` o'rniga:
       LATEST=$(app-store-connect get-latest-app-store-build-number "$APP_STORE_APP_ID")
       agvtool new-version -all $(($LATEST + 1))
     va environment.vars ga APP_STORE_APP_ID (App Store Connect > App > General > App Information dagi raqam) qo'sh.
   - distribution_type: app_store o'rniga ad_hoc / development ham mumkin (ichki qurilma testlari uchun).
   - xcode versiyasini `latest` dan aniq versiyaga (masalan 16.2) pin qilib, kutilmagan yangilanishlardan saqlan.

MUHIM: Apple Developer akkaunti hozircha yo'q ekan, faqat 1-workflow (ios-simulator-unsigned) ishlaydi va hech qanday to'lov/imzo talab qilmaydi. 2-workflow yuqoridagi 1-4 qadam bajarilgach ishga tushadi; ikkalasi bitta codemagic.yaml'da birga tursa ham bir-biriga xalaqit bermaydi.
