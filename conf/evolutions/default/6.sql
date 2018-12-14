# --- !Ups

alter table "check" drop column secure;
alter table "check" drop column path;

create type kind as enum('HTTP', 'SSL', 'DNS', 'ICMP');

alter table "check" add column kind kind not null;
alter table "check" add column params json default '{}'::json;

# --- !Downs

alter table "check" drop column params;
alter table "check" drop column kind;

drop type kind;

alter table only "check" add column path character varying (2048) not null;

alter table only "check" add column secure boolean;
alter table only "check" alter column secure set default false;
alter table only "check" alter column secure set not null;
