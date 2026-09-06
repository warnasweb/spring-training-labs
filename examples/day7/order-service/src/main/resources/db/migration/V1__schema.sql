create table outbox(id uuid primary key, payload text not null, published boolean not null default false, created_at timestamptz not null default now());
create index outbox_pending on outbox(created_at) where published=false;
create table inbox(id uuid primary key, processed_at timestamptz not null default now());
create table orders(id uuid primary key,product_id uuid not null,quantity integer not null check(quantity>0),fail_payment boolean not null,status text not null,correlation_id varchar(100) not null,updated_at timestamptz not null default now());
