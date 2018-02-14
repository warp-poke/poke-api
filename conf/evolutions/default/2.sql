
# --- !Ups

alter table only "check" add column secure boolean;

# --- !Downs

alter table if exists only "check" drop column secure;
