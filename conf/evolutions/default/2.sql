
# --- !Ups

alter table only "check" add column secure boolean;
alter table only "check" alter column secure set default false;
alter table only "check" alter column secure set not null;

# --- !Downs

alter table if exists only "check" drop column secure;
