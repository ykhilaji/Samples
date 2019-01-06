create schema if not exists ${schema};

create table if not exists ${schema}.entity (
  id serial primary key,
  value text,
  create_time timestamp default now()
);