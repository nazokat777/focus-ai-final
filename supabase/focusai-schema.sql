-- ============================================================
-- FOCUS AI — Do'stlar backend (Supabase)
-- Supabase -> SQL Editor -> New query -> BU FAYLNI to'liq joylang -> RUN
-- Idempotent: qayta ishga tushirsangiz ham xato bermaydi.
-- anon (public) kalit ochiq bo'lishi normal — ma'lumotni RLS himoya qiladi.
-- ============================================================

-- 1) Jadvallar ---------------------------------------------------------------
create table if not exists public.profiles (
  code       text primary key,
  name       text,
  created_at timestamptz default now()
);

create table if not exists public.daily_stats (
  code       text not null,
  d          date not null,
  focus_mins int,                 -- bekitilgan bo'lsa null ("—")
  habits     int,                 -- bekitilgan bo'lsa null
  report     jsonb,               -- ko'rinadigan vazifalar (per-task); ustoz/ota-ona ko'radi
  updated_at timestamptz default now(),
  primary key (code, d)
);
-- eski jadvalga report ustunini qo'shish (agar bo'lmasa)
alter table public.daily_stats add column if not exists report jsonb;

create table if not exists public.links (
  follower    text not null,      -- kim qo'shdi (mening kodim)
  target_code text not null,      -- kimni qo'shdi (do'st/farzand kodi)
  kind        text not null check (kind in ('friend','mentor')),
  created_at  timestamptz default now(),
  primary key (follower, target_code, kind)
);

create table if not exists public.messages (
  id         bigint generated always as identity primary key,
  from_code  text not null,
  to_code    text not null,
  body       text not null,
  created_at timestamptz default now()
);

-- 2) RLS (row level security) ------------------------------------------------
alter table public.profiles    enable row level security;
alter table public.daily_stats enable row level security;
alter table public.links       enable row level security;
alter table public.messages    enable row level security;

-- Reyting/do'stlar: anon o'qish+yozish (fokus statistikasi maxfiy emas)
drop policy if exists p_all on public.profiles;
drop policy if exists s_all on public.daily_stats;
drop policy if exists l_all on public.links;
create policy p_all on public.profiles    for all to public using (true) with check (true);
create policy s_all on public.daily_stats for all to public using (true) with check (true);
create policy l_all on public.links       for all to public using (true) with check (true);

-- messages (shaxsiy chat): jadval yopiq, faqat RPC orqali kirish
drop policy if exists m_all on public.messages;

create or replace function public.send_msg(p_from text, p_to text, p_body text)
returns void language sql security definer set search_path = public as $$
  insert into public.messages (from_code, to_code, body)
  values (upper(p_from), upper(p_to), left(p_body, 500));
$$;

create or replace function public.get_msgs(p_me text, p_with text)
returns setof public.messages language sql security definer set search_path = public as $$
  select * from public.messages
  where (from_code = upper(p_me)   and to_code = upper(p_with))
     or (from_code = upper(p_with) and to_code = upper(p_me))
  order by created_at;
$$;

grant execute on function public.send_msg(text, text, text) to anon, authenticated;
grant execute on function public.get_msgs(text, text)       to anon, authenticated;

-- 3) Realtime (jonli reyting) — idempotent -----------------------------------
do $$ begin
  alter publication supabase_realtime add table public.daily_stats;
exception when duplicate_object then null; end $$;

-- Tayyor. Endi ilovaga Project URL + anon public key ni ulash qoldi.
