-- ═══════════════════════════════════════════════════════════════════
--  FOCUS AI — XAVFSIZLIK TUZATISHI (Supabase SQL Editor'da RUN qiling)
--  Sana: 2026-07-31
--
--  MUAMMO: profiles / daily_stats / links jadvallarida RLS siyosati
--    `for all to public using (true) with check (true)`
--  ya'ni RLS YOQILGAN, lekin hech narsani cheklamaydi. Ustiga anon rolga
--  DELETE grant ham berilgan. Anon (publishable) kalit esa ilova kodida
--  ochiq va repo PUBLIC — demak istalgan odam:
--    • hamma foydalanuvchining ismi va statistikasini O'QIY oladi
--    • istalgan yozuvni O'ZGARTIRA oladi (reytingni soxtalashtirish)
--    • BUTUN jadvalni O'CHIRA oladi
--
--  TASDIQLANGAN (2026-07-31, faqat o'qish bilan):
--    curl '.../profiles?select=code,name' -H 'apikey: <anon>'
--    -> begona foydalanuvchilar ismlari qaytdi.
--
--  QUYIDAGI SQL: o'qish ochiq qoladi (reyting/do'stlar ishlashi uchun),
--  lekin O'CHIRISH butunlay taqiqlanadi va yozish cheklanadi.
-- ═══════════════════════════════════════════════════════════════════

-- ─── 1) DELETE huquqini olib tashlaymiz (eng muhim qadam) ───────────
revoke delete on public.profiles    from anon, authenticated;
revoke delete on public.daily_stats from anon, authenticated;
revoke delete on public.links       from anon, authenticated;

-- ─── 2) Eski "hamma narsaga ruxsat" siyosatlarini o'chiramiz ────────
drop policy if exists p_all on public.profiles;
drop policy if exists s_all on public.daily_stats;
drop policy if exists l_all on public.links;

-- ─── 3) O'qish: ochiq (reyting va do'stlar shunga tayanadi) ─────────
create policy p_read on public.profiles    for select using (true);
create policy s_read on public.daily_stats for select using (true);
create policy l_read on public.links       for select using (true);

-- ─── 4) Yozish: ruxsat, lekin O'CHIRISH yo'q ───────────────────────
--  Izoh: anon kalit bilan "faqat o'z qatorini yozsin" deb cheklab
--  bo'lmaydi, chunki foydalanuvchi kodi (ABC123) mijozda hosil bo'ladi
--  va serverga isbot qilinmaydi. Shuning uchun:
--    - insert/update ruxsat (ilova ishlashi uchun shart)
--    - delete YO'Q (eng katta zarar shu edi)
create policy p_write on public.profiles    for insert with check (true);
create policy p_upd   on public.profiles    for update using (true) with check (true);
create policy s_write on public.daily_stats for insert with check (true);
create policy s_upd   on public.daily_stats for update using (true) with check (true);
create policy l_write on public.links       for insert with check (true);
create policy l_upd   on public.links       for update using (true) with check (true);

-- ─── 5) Ishlatilmaydigan ochiq jadval bo'lsa — o'chiramiz ──────────
--  `backups` jadvali supabase/backups.sql da to'liq ochiq siyosat bilan
--  ta'riflangan. 2026-07-31 tekshiruvida u PRODDA MAVJUD EMAS (yaxshi).
--  Agar keyin yaratilgan bo'lsa — quyidagini oching:
-- drop table if exists public.backups cascade;

-- ─── TEKSHIRUV ─────────────────────────────────────────────────────
--  RUN qilgandan keyin quyidagi buyruq XATO qaytarishi kerak (403):
--    curl -X DELETE '.../rest/v1/profiles?code=eq.XXX' -H 'apikey: <anon>'
--  O'qish esa ishlashda davom etadi (Liga bo'sh qolmasin).
