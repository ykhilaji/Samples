create table if not exists expression
(
    expression_id char(36) primary key,
    expression    text,
    creation_time timestamp default now()
);

create index if not exists expression_creation_time_idx on expression(creation_time);

create table if not exists result
(
    expression_id char(36) primary key,
    result        double precision,
    error         text,
    creation_time timestamp default now(),
    foreign key(expression_id) references expression(expression_id)
);

create index if not exists result_creation_time_idx on result(creation_time);