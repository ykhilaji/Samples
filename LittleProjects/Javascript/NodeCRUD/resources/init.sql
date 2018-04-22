create schema node;

create sequence node.book_seq;

create table node.book (
  id numeric(38) default nextval('node.book_seq'),
  title varchar(255) not null,
  pages numeric(38) check (pages >= 0) default 0,
  authors varchar(255)[],
  constraint book_pk primary key (id)
);

create unique index book_title on node.book(title);
create index book_auhtors on node.book(authors);

insert into node.book(title, pages, authors) values ('book1', 100, '{"author1", "author2", "author3"}');