
# --- !Ups

alter table if not exists only "check" add column protocol boolean;

# --- !Downs

alter table if exists only "check" drop column protocol;
