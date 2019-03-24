create database if not exists test;

create table if not exists test.merge_tree
(
  id    Int64,
  value String,
  time  Date materialized toDate(now())
) engine = MergeTree
  partition by toYYYYMM(time) primary key id
    order by (id, value);

create table if not exists test.replacing_merge_tree
(
  id    Int64,
  value String,
  time  Date materialized toDate(now())
) engine = ReplacingMergeTree
  partition by toYYYYMM(time) primary key id
    order by (id, value);

create table if not exists test.summing_merge_tree
(
  id    Int64,
  digit Int64 materialized rand64(),
  value String,
  time  Date materialized toDate(now())
) engine = SummingMergeTree
  partition by toYYYYMM(time) primary key id
    order by (id);


insert into test.merge_tree(id, value) values (1, '1'), (2, '2'), (1, '1');
select *, time from test.merge_tree order by id; -- 3 rows

insert into test.replacing_merge_tree(id, value) values (1, '1'), (2, '2'), (1, '1');
select *, time from test.replacing_merge_tree order by id; -- 3 rows
optimize table test.replacing_merge_tree; -- force cleaning duplicates. Usually, this is a background process.
select *, time from test.replacing_merge_tree order by id; -- 2 rows

insert into test.summing_merge_tree(id, value) values (1, '1'), (2, '2');
select *, digit, time from test.summing_merge_tree order by id;
insert into test.summing_merge_tree(id, value) values (1, '1'), (2, '2');
optimize table test.summing_merge_tree; -- force
select *, digit, time from test.summing_merge_tree order by id;


create materialized view if not exists test.aggregate_merge_tree
engine = AggregatingMergeTree partition by toYYYYMM(time) order by id as select
id, time, sumState(toInt64(value)) as sum from test.merge_tree group by id, time;

select id, sumMerge(sum) from test.aggregate_merge_tree group by id;

