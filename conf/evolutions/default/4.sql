# --- !Ups

alter table "check" add column kind text not null default 'http';

# --- !Downs

alter table "check" drop column kind;
