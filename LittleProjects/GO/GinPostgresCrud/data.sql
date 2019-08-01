create sequence entity_id_seq;

create table entity (
    id bigint default nextval('entity_id_seq'),
    value varchar(255) not null ,
    constraint entity_pk primary key (id)
)