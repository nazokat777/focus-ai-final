# Bosh-ekran widgeti (Android)

Telefon bosh ekraniga qo'yiladigan widget: **bugungi fokus vaqti**, **kunlik norma foizi** (neon progress) va **streak** (🔥). Bosilса — ilova ochiladi.

## Nima qo'shildi

**Native (Android):**
- `android/app/src/main/java/uz/lumnaara/focusai/FocusWidgetProvider.java` — `AppWidgetProvider`, SharedPreferences (`focusai_widget`) dan o'qib RemoteViews chizadi.
- `android/app/src/main/java/uz/lumnaara/focusai/FocusWidgetPlugin.java` — Capacitor plugin (`FocusWidget`). JS'dan `update()` ni qabul qiladi, SharedPreferences'ga yozadi, widgetni yangilaydi.
- `android/app/src/main/java/uz/lumnaara/focusai/MainActivity.java` — `registerPlugin(FocusWidgetPlugin.class)` (super.onCreate'dan oldin).
- `res/layout/focus_widget.xml`, `res/drawable/widget_bg.xml`, `res/drawable/widget_progress.xml`, `res/xml/focus_widget_info.xml` — ko'rinish (qora karta, neon).
- `res/values/colors.xml` — widget ranglari (neon #39FF8C).
- `AndroidManifest.xml` — `<receiver .FocusWidgetProvider>` (APPWIDGET_UPDATE).

**Web (ilova ichi, `www/index.html`):**
- `pushWidget()` — bugungi fokus/foiz/streakni native pluginga uzatadi. Faqat qiymat **o'zgarганда** yozadi (daqiqada ~1 marta). Web'da xavfsiz no-op (plugin yo'q).
- `refreshChips()` ichida + 15s intervalда + ilova ochilганда chaqiriladi.
- i18n: `wid_sub` ("Bugungi fokus" / "Сегодняшний фокус" / "Today's focus").

## Ma'lumot oqimi

```
JS: pushWidget()  ->  Capacitor.Plugins.FocusWidget.update({today,pct,streak,subtitle})
                  ->  FocusWidgetPlugin (SharedPreferences "focusai_widget" ga yozadi)
                  ->  FocusWidgetProvider.updateAll() (RemoteViews yangilaydi)
Widget bosildi    ->  ilovani ochadi (LAUNCHER intent)
```

## Qayta build qilish (widget paydo bo'lishi uchun SHART)

Widget faqat **native APK**da ko'rinadi (Vercel/brauzerда emas). Web o'zgarishlar zararsiz no-op.

```bash
npx cap sync android      # www/ ni androidga ko'chiradi + plaginlarni ro'yxatlaydi
npx cap open android      # Android Studio
# yoki Codemagic (codemagic.yaml) orqali APK yig'iladi
```

APK o'rnatilгач: bosh ekran → uzoq bosish → **Widgets** → "Focus AI" → tortib qo'ying.

## Eslatma
- Widget qiymatlari ilova kamida bir marta ochilганda to'ladi (`pushWidget()` startда ishlaydi).
- Ilova yopiq bo'lса ham oxirgi yozilган qiymat ko'rinadi; keyingi ochilишда yangilanadi.
- Streak-freeze/dam kunlari `globalStreak()` orqali hisobga olinadi (ilova mantig'i bilan bir xil).
