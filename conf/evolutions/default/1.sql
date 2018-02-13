-- create extension if not exists "uuid-ossp";

create table if not exists user (
    user_id         uuid primary key,
    hashed_password character varying (512) not null,
    email           character varying (128) not null unique,
    created_at      timestamp without timezone default now() not null
    updated_at      timestamp without timezone default now() not null,
    deleted_at      timestamp without timezone,
);

create table if not exists check (
    check_id       uuid primary key,
    service_id     uuid not null,
    created_at     timestamp without timezone default now() not null
    updated_at     timestamp without timezone default now() not null,
    deleted_at     timestamp without timezone,
);

create table if not exists service (
    service_id uuid primary key,
    deleted_at timestamp without timezone,
    domain character varying (128) not null,
    updated_at timestamp without timezone default now() not null,
    created_at timestamp without timezone default now() not null,
    user_id uuid not null
);

alter table only service add constraint user_user_id_fkey foreign key (user_id) references user(user_id);
alter table only check add constraint service_service_id_fkey foreign key (service_id) references service(service_id);

create index if not exists user_deleted_at_idx on user (deleted_at);
create index if not exists check_deleted_at_idx on check (deleted_at);
create index if not exists service_deleted_at_idx on service (deleted_at);
