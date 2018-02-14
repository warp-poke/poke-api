-- create extension if not exists "uuid-ossp";

# --- !Ups

create table if not exists "user" (
    user_id         uuid primary key,
    hashed_password character varying (512) not null,
    email           character varying (128) not null unique
);

create table if not exists "service" (
    user_id uuid not null,
    service_id uuid primary key,
    domain character varying (128) not null
);

create table if not exists "check" (
    check_id       uuid primary key,
    service_id     uuid not null,
    path           character varying (2048) not null
);

alter table only "service" add constraint user_user_id_fkey foreign key (user_id) references "user"(user_id);
alter table only "check" add constraint service_service_id_fkey foreign key (service_id) references "service"(service_id);

# --- !Downs

drop table if exists "check";
drop table if exists "service";
drop table if exists "user";
