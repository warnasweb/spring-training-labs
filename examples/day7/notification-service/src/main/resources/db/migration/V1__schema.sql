create table outbox(id uuid primary key, payload text not null, published boolean not null default false, created_at timestamptz not null default now());
create index outbox_pending on outbox(created_at) where published=false;
create table inbox(id uuid primary key, processed_at timestamptz not null default now());
create table notifications(id bigserial primary key,order_id uuid not null,event_type text not null,message text not null,created_at timestamptz not null default now(),unique(order_id,event_type));
