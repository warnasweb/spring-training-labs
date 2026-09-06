create table outbox(id uuid primary key, payload text not null, published boolean not null default false, created_at timestamptz not null default now());
create index outbox_pending on outbox(created_at) where published=false;
create table inbox(id uuid primary key, processed_at timestamptz not null default now());
create table stock(product_id uuid primary key,available integer not null check(available>=0));
create table reservations(order_id uuid primary key,product_id uuid not null,quantity integer not null check(quantity>0),status text not null);
insert into stock values ('11111111-1111-1111-1111-111111111111',100);
