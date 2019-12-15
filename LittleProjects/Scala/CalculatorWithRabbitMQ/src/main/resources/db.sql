create sequence if not exists expression_seq;
create table if not exists expression
(
    expression_id bigint primary key default nextval('expression_seq'),
    expression    text
);

create table if not exists result
(
    expression_id bigint primary key,
    result        double precision,
    error         text,
    foreign key(expression_id) references expression(expression_id)
);