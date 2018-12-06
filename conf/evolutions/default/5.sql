# --- !Ups

alter table hook add column kind text not null;

# --- !Downs

alter table hook drop column kind;
