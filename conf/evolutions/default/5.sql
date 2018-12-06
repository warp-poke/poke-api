# --- !Ups

alter table hook add column kind text not null;
alter table hook add column template text not null;

# --- !Downs

alter table hook drop column kind;
alter table hook drop column template;
