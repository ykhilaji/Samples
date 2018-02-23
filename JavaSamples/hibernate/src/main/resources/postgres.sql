create schema jdbc;

create table jdbc.entity (
  id int,
  value varchar(255),
  constraint entity_pk primary key(id)
);

insert into jdbc.entity(id, value) VALUES (1, '1'), (2, '2'), (3, '3'), (4, '4'), (5, '5');