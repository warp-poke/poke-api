# --- !Ups

alter table service add column name text;
alter table "check" add column name text;

# --- !Downs

alter table "check" drop column name;
alter table service drop column name;
