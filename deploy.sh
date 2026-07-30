#!/usr/bin/env bash
# Focus AI deploy — APK 404 minasidan himoya bilan.
# Ishlatish:  bash deploy.sh
set -u
cd "$(dirname "$0")"

# 1) APK www ichida bo'lishi SHART (aks holda sayt /focusai.apk da 404 beradi)
if [ -f FocusAI.apk ]; then
  cp -f FocusAI.apk www/focusai.apk
  echo "APK nusxalandi: $(stat -c%s www/focusai.apk) bayt"
else
  echo "OGOHLANTIRISH: FocusAI.apk topilmadi — sayt eski APK bilan qoladi"
fi

# 2) Deploy
vercel --prod --yes 2>&1 | grep -iE "Aliased|Error" | head -1

# 3) Tekshiruv — sayt haqiqatan yangi APK'ni beryaptimi
sleep 5
LOCAL=$(stat -c%s www/focusai.apk 2>/dev/null || echo 0)
REMOTE=$(curl -sIL "https://focus-ai-final.vercel.app/focusai.apk" | grep -i content-length | tail -1 | tr -d '\r' | awk '{print $2}')
echo "lokal=$LOCAL  sayt=$REMOTE"
if [ "$LOCAL" = "$REMOTE" ] && [ "$LOCAL" != "0" ]; then
  echo "✅ SAYT YANGI APK'NI BERYAPTI"
else
  echo "❌ MOS EMAS — qayta deploy kerak (vercel --prod --yes)"
  exit 1
fi
