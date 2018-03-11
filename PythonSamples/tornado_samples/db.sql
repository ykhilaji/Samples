create schema tornado;

create sequence tornado.book_seq;
create sequence tornado.author_seq;

create table tornado.book (
  id numeric(38) default nextval('tornado.book_seq'),
  title varchar(255) not null unique,
  constraint book_pk primary key(id)
);

create table tornado.author (
  id numeric(38) default nextval('tornado.author_seq'),
  first_name varchar(255) not null,
  last_name varchar(255) not null,
  constraint author_pk primary key(id)
);

create table tornado.book_author (
  book_id numeric(38),
  author_id numeric(38),
  constraint book_author_pk primary key(book_id, author_id),
  constraint book_author_book_fk foreign key(book_id) references tornado.book(id) on delete cascade,
  constraint book_author_author_fk foreign key(author_id) references tornado.author(id) on delete cascade
);


create index author_first_name on tornado.author(first_name);