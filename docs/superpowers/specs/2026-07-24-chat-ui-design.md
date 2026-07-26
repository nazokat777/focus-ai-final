# Chat UI — Dizayn spec (2026-07-24)

## Maqsad
Do'stlar buluti backend'ida chat dvigateli (`send_msg`/`get_msgs` RPC) allaqachon
tayyor va testlangan (15/15), lekin **UI yo'q**. Shu interfeys qurilib, ijtimoiy
qatlam to'ldiriladi: do'st / ustoz / shogird bilan 1-ga-1 yozishma.

## Doira (scope)
- Kim bilan: **do'stlar (friend) + ustozlar (mentor, men qo'shgan) + shogirdlar (ward, meni qo'shgan)**.
- Jonlilik: **polling** (chat ochiq turganda ~3s da yangilash). Realtime/anon-Auth YO'Q.
- Xabar kelganda **do'stlar bo'limida o'qilmagan belgi** (yashil nuqta) + Profil "Do'stlar" tugmasida umumiy nuqta.
- Faqat **bulut rejimida** (`Social.cloud()===true`).

### Doiraga KIRMAYDI (YAGNI)
- Guruh chat, media/rasm, ovozli xabar, o'chirish/tahrirlash, "yozmoqda…", read-receipt (server tomon).
- Realtime push (kelajak bosqich — [[cloud-testing]] eslatmasidagi anon-Auth talab qiladi).

## Backend
O'zgarmaydi. Mavjud yetarli:
- `Social.sendMessage(toCode, body)` → RPC `send_msg` (500 belgi cap), Promise<bool>.
- `Social.getMessages(withCode)` → RPC `get_msgs`, `[{from_code,to_code,body,created_at}]` (created_at bo'yicha o'sish).
- `messages` jadvali RLS bilan qulf (direct SELECT bo'sh, INSERT 401) — maxfiylik saqlanadi. Chat faqat RPC orqali.

## Komponentlar

### 1. `#chat` paneli (yangi `.detail`)
- Tuzilma: `.d-head` (orqaga tugma `#chatClose` + suhbatdosh ismi `#chatName` + rol badge `#chatRole`),
  `#chatBody` (scroll xabar oqimi), pastda `.chat-input` (`#chatInput` matn maxlength=500 + `#chatSend` tugma).
- Xabar pufagi: mening (`from_code===myCode`) o'ngда neon; suhbatdoshники chapда `elev`. Matn `esc()`.
- Bo'sh holat: `chat_empty`.
- Dizayn: faqat mavjud tokenlar/uslub — **muzlatilgan dizaynga rioya, yangi rang/shrift yo'q**.
- Holat: `chatWith = { code, name, kind }` (ochiq suhbat), `chatTimer` (polling interval), `chatLastLen` (yangi xabar aniqlash).

### 2. Kirish nuqtalari
- Do'st qatori (`#frBoard .fr-row`), ustoz qatori (`#frMentorList`), **shogird qatori** (`#frWardList` — yangi seksiya, `Social.wards()`dan render).
- Qator bosilsa `openChat(code, name, kind)`. `×` (o'chirish) tugmasi chatni ochmaydi (event stopPropagation / target tekshiruvi).
- Har qatorда o'qilmagan bo'lsa `.fr-unread` (yashil nuqta).
- Shogird seksiyasi faqat `wards().length>0` bo'lsa ko'rinadi.

### 3. Ma'lumot oqimi (polling)
- `openChat`: `#chat` `.open`; `getMessages` → render → pastga scroll; `markSeen(code)` (lokal `focusai.chat.seen.<code>` = oxirgi xabar `created_at`).
- Polling: `setInterval(3000)` → `getMessages` → uzunlik o'zgargan bo'lsa qayta render + agar pastda edi — pastga scroll. `closeChat` → `clearInterval`.
- Yuborish (`#chatSend` yoki Enter): matn trim, bo'sh bo'lmasa `sendMessage` → OK: input tozalanadi, darhol `getMessages`; xato: `toast(chat_err)`, matn saqlanadi.
- O'qilmagan aniqlash: `renderFriends` ichida panel **ochilganда** har kontakt (do'st+ustoz+shogird) uchun `getMessages` → oxirgi *kiruvchi* (`from_code===kontakt`) `created_at` > lokal "seen" bo'lsa nuqta.
  - Do'st soni oz → N ta RPC maqbul. Kelajakda: bitta SECURITY DEFINER `get_inbox(p_me)` RPC bilan optimallashtirish.
- Profil "Do'stlar" nuqtasi (`#friendsRow`): panelни ochmasdan bilish uchun **alohida yengil tekshiruv** —
  Profil tabi faollashganда (va `cloud()` bo'lsa) bir marta + Profil ko'rinib turганда modest interval (~30s) bilan
  o'sha per-kontakt `getMessages` mantiqini ishlatib hisoblanadi. Biror kontakt o'qilmagan bo'lsa nuqta yonadi.
  (Aylanma bog'liqlik yo'q — renderFriends'ga bog'liq emas.)

### 4. Bulut sharti
- `Social.cloud()` false → kirish nuqtalari chatni ochmaydi (yoki `toast(chat_cloud_only)`), o'qilmagan nuqtalar hisoblanmaydi.

## i18n (UZ/RU/EN — `I18N` obyektiga uchchalasiga)
Yangi kalitlar: `chat_ph` (input placeholder), `chat_send`, `chat_empty`, `chat_cloud_only`, `chat_err`, `fr_wards` (shogirdlar sarlavhasi).

## Xavfsizlik / chekka holatlar
- Barcha xabar matni `esc()` — XSS (KRITIK, chat foydalanuvchi kiritган matn).
- 500 belgi cap (klient + server `left(...,500)`).
- Tarmoq xatosi: yuborish muvaffaqiyatsiz → toast, matn yo'qolmaydi.
- Panel yopilganда interval to'xtaydi (resurs oqmasin).
- Kontakt kodlari allaqachon tasdiqlangan (`/^[A-Z]{3}[0-9]{3}$/`).

## Test
- Backend RPC: allaqachon 15/15 ([[cloud-testing]]).
- Yangi lokal UI test (`_*.cjs`, gitignored — [[testing-setup]] naqshi): chat ochish, xabar yuborish, `getMessages` qaytishi, o'qilmagan nuqta mantiqi, `esc()` XSS (masalan `<img onerror>` matn sifatida chiqishi).
- `node --check www/index.html` (skript sintaksisi) — aslida HTML, shuning uchun skript qismini ajratib yoki mavjud test naqshi bilan.
- Regressiya `_test_focus.cjs` 27/27 buzilmasligi.

## Ochiq savol yo'q. Keyingi qadam: writing-plans (amalga oshirish rejasi).
