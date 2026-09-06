create table outbox(id uuid primary key, payload text not null, published boolean not null default false, created_at timestamptz not null default now());
create index outbox_pending on outbox(created_at) where published=false;
create table inbox(id uuid primary key, processed_at timestamptz not null default now());
create table payments(order_id uuid primary key,status text not null,created_at timestamptz not null default now());
