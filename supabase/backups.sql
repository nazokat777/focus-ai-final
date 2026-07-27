-- Focus AI — bulut zaxirasi
-- Supabase -> SQL Editor -> New query -> pastdagini joylab RUN bosing (bir marta).

-- ============ 1) SHAXSIY zaxira (Google login bilan — TAVSIYA ETILADI) ============
-- Har foydalanuvchi FAQAT o'z ma'lumotini ko'radi/yozadi (RLS auth.uid()).
create table if not exists public.cloud_backups (
  user_id    uuid primary key references auth.users(id) on delete cascade,
  data       text not null,
  updated_at timestamptz not null default now()
);
alter table public.cloud_backups enable row level security;

drop policy if exists "cb_select" on public.cloud_backups;
drop policy if exists "cb_insert" on public.cloud_backups;
drop policy if exists "cb_update" on public.cloud_backups;

create policy "cb_select" on public.cloud_backups for select using (auth.uid() = user_id);
create policy "cb_insert" on public.cloud_backups for insert with check (auth.uid() = user_id);
create policy "cb_update" on public.cloud_backups for update using (auth.uid() = user_id) with check (auth.uid() = user_id);

-- ============ 2) Google provayderini yoqish ============
-- Supabase panel -> Authentication -> Providers -> Google -> Enable.
--   * Google Cloud Console'da OAuth Client (Web) yarating, Client ID + Secret ni Supabase'ga joylang.
--   * "Authorized redirect URI" ga Supabase bergan callback URL'ni qo'shing
--     (https://<loyiha>.supabase.co/auth/v1/callback).
--   * Supabase -> Authentication -> URL Configuration -> Site URL / Redirect URLs ga
--     ilova manzilini qo'shing: https://focus-ai-final.vercel.app
-- Web (Vercel) shu bilan ishlaydi. NATIVE APK uchun alohida native Google plagin +
-- Android SHA-1 (Google Cloud'da) kerak — buni native build paytida ulaymiz.

-- ============ (eski) kod bilan ochiq zaxira — ENDI ISHLATILMAYDI ============
-- Login qo'shilгач shaxsiy zaxira (yuqoridagi) ishlatiladi. Bu jadval majburiy emas.
create table if not exists public.backups (
  code       text primary key,
  data       text not null,
  updated_at timestamptz not null default now()
);
alter table public.backups enable row level security;
drop policy if exists "backups_read"   on public.backups;
drop policy if exists "backups_insert" on public.backups;
drop policy if exists "backups_update" on public.backups;
create policy "backups_read"   on public.backups for select using (true);
create policy "backups_insert" on public.backups for insert with check (true);
create policy "backups_update" on public.backups for update using (true) with check (true);
