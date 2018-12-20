create schema if not exists ${schema};

create sequence ${schema}.event_seq;

create table ${schema}.entity (
 id numeric(38),
 value varchar(255) not null,
 constraint entity_pk primary key (id)
);

create table ${schema}.event (
 id numeric(38) default nextval('${schema}.event_seq'),
 event_time timestamp,
 entity_id numeric(38) not null,
 constraint event_pk primary key (id),
 constraint event_entity_fk foreign key (entity_id) references ${schema}.entity(id)
);

create index event_index on ${schema}.event(entity_id);

create table ${schema}.unknown_event (
 id numeric(38) default nextval('${schema}.event_seq'),
 event_time timestamp,
 entity_id numeric(38) not null,
 constraint event_unknown_pk primary key (id)
);

create index unknown_event_index on ${schema}.unknown_event(entity_id);