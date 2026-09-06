create table outbox(id uuid primary key, payload text not null, published boolean not null default false, created_at timestamptz not null default now());
create index outbox_pending on outbox(created_at) where published=false;
create table inbox(id uuid primary key, processed_at timestamptz not null default now());
create table products(id uuid primary key,name varchar(200) not null,price numeric(12,2) not null check(price>0));
insert into products values ('11111111-1111-1111-1111-111111111111','Training Laptop',999.00);
