#!/usr/bin/env bash
# Focus AI — APK yig'ish (ikki minaga qarshi himoya bilan).
# Ishlatish:  bash build-apk.sh
#
# MINA 1 (rekursiv APK): www/focusai.apk mavjud bo'lsa, `cap sync` uni
#   android/app/src/main/assets/public/ ga ko'chiradi va u YANGI APK ichiga
#   bundle bo'ladi. Natijada hajm ikki barobar oshadi (27MB -> 42MB).
# MINA 2 (keraksiz videolar): assets/video/*.mp4 faqat WEB landing uchun,
#   ilova ichida ishlatilmaydi, lekin APK ga 13MB qo'shadi.
set -u
cd "$(dirname "$0")"
ASSETS="android/app/src/main/assets/public"

echo "── 1/4  www tozalash (rekursiv APK oldini olish)"
rm -f www/focusai.apk

echo "── 2/4  cap sync"
npx cap sync android 2>&1 | tail -1

echo "── 3/4  APK ichidan keraksizlarni chiqarish"
rm -f "$ASSETS/focusai.apk"                 # rekursiv APK
rm -f "$ASSETS/assets/video/"*.mp4          # web landing videolari
echo "     assets: $(du -sh "$ASSETS" 2>/dev/null | cut -f1)"

echo "── 4/4  build"
( cd android && ANDROID_HOME="${ANDROID_HOME:-D:/Android/Sdk}" ./gradlew.bat assembleRelease -q )
RC=$?
if [ $RC -ne 0 ]; then echo "❌ BUILD XATO ($RC)"; exit 1; fi

cp android/app/build/outputs/apk/release/app-release.apk FocusAI.apk
SIZE=$(stat -c%s FocusAI.apk)
MB=$(python3 -c "print(f'{$SIZE/1048576:.1f}')" 2>/dev/null || echo "?")

# tekshiruv: rekursiv apk yoki video qolmaganmi
AAPT="D:/Android/Sdk/build-tools/34.0.0/aapt.exe"
BAD=$("$AAPT" list FocusAI.apk 2>/dev/null | grep -cE "focusai\.apk|\.mp4" | head -1)
BAD=${BAD:-0}

echo ""
echo "APK: ${MB} MB  ($SIZE bayt)"
if [ "$BAD" -gt 0 ]; then
  echo "❌ OGOHLANTIRISH: ichida $BAD ta rekursiv apk/video qoldi!"
  exit 1
else
  echo "✅ toza (rekursiv apk yo'q, video yo'q)"
fi
echo ""
echo "Endi tarqatish uchun:  bash deploy.sh"
