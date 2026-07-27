-- Focus AI — to'liq tarix zaxirasi (bulut)
-- Supabase -> SQL Editor -> New query -> pastdagini joylab RUN bosing.
-- Bir marta bajariladi. Shundan keyin ilova butun tarixni avtomatik saqlaydi.

create table if not exists public.backups (
  code       text primary key,
  data       text not null,
  updated_at timestamptz not null default now()
);

alter table public.backups enable row level security;

-- Ilovada haqiqiy auth yo'q — foydalanuvchi kodi (Do'stlar bo'limидаги kod) kalit vazifasini bajaradi.
-- Shu bois siyosatlar ochiq (anon key bilan ishlaydi). Kod maxfiy emas (do'stlarga ulashiladi),
-- ma'lumot esa faqat odat tarixi — shuning uchun bu konkurs ilovasi uchun maqbul.
drop policy if exists "backups_read"   on public.backups;
drop policy if exists "backups_insert" on public.backups;
drop policy if exists "backups_update" on public.backups;

create policy "backups_read"   on public.backups for select using (true);
create policy "backups_insert" on public.backups for insert with check (true);
create policy "backups_update" on public.backups for update using (true) with check (true);
