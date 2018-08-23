# --- !Ups

create table hook(
  id uuid primary key not null,
  user uuid not null,
  label text not null,
  webhook text not null
);

# --- !Downs

drop table if exists hook;
