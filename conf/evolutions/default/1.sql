-- create extension if not exists "uuid-ossp";

# --- !Ups

create table if not exists "user" (
    user_id         uuid primary key,
    hashed_password character varying (512) not null,
    email           character varying (128) not null unique,
    created_at      timestamp without time zone default now() not null,
    updated_at      timestamp without time zone default now() not null,
    deleted_at      timestamp without time zone
);

create table if not exists "service" (
    user_id uuid not null,
    service_id uuid primary key,
    domain character varying (128) not null,
    updated_at timestamp without time zone default now() not null,
    created_at timestamp without time zone default now() not null,
    deleted_at timestamp without time zone
);

create table if not exists "check" (
    check_id       uuid primary key,
    service_id     uuid not null,
    created_at     timestamp without time zone default now() not null,
    updated_at     timestamp without time zone default now() not null,
    deleted_at     timestamp without time zone
);

alter table only "service" add constraint user_user_id_fkey foreign key (user_id) references "user"(user_id);
alter table only "check" add constraint service_service_id_fkey foreign key (service_id) references "service"(service_id);

create index if not exists user_deleted_at_idx on "user" (deleted_at);
create index if not exists check_deleted_at_idx on "check" (deleted_at);
create index if not exists service_deleted_at_idx on "service" (deleted_at);

# --- !Downs

drop index if exists check_deleted_at_idx;
drop index if exists service_deleted_at_idx;
drop index if exists user_deleted_at_idx;

drop table if exists "check";
drop table if exists "service";
drop table if exists "user";
