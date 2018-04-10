create schema akka;
create table if not exists akka.entity (
  id bigserial,
  value VARCHAR(255),
  constraint entity_pk primary key (id)
);