create schema sqlalchemy;

create sequence sqlalchemy.user_seq;
create sequence sqlalchemy.message_seq;
create sequence sqlalchemy.group_seq;

create table sqlalchemy.user (
  id numeric(38) default nextval('sqlalchemy.user_seq'),
  first_name varchar(255),
  last_name varchar(255),
  constraint user_pk primary key (id)
);

create table sqlalchemy.message (
  id numeric(38) default nextval('sqlalchemy.message_seq'),
  user_id numeric(38),
  message text,
  constraint message_pk primary key (id),
  constraint message_user_fk foreign key (user_id) references sqlalchemy.user(id)
);

create table sqlalchemy.group (
  id numeric(38) default nextval('sqlalchemy.group_seq'),
  name varchar(255),
  constraint group_pk primary key (id)
);

create table sqlalchemy.user_group (
  user_id numeric(38),
  group_id numeric(38),
  constraint user_group_pk primary key (user_id, group_id),
  constraint user_group_user_fk foreign key (user_id) references sqlalchemy.user(id),
  constraint user_group_group_fk foreign key (group_id) references sqlalchemy.group(id)
);