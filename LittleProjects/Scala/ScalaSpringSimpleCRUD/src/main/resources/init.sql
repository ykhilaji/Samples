create schema crud;

create sequence crud.book_seq;
create sequence crud.author_seq;
create sequence crud.publisher_seq;

create table crud.publisher (
  id numeric(38) default nextval('crud.publisher_seq'),
  name VARCHAR(255) not null,
  constraint publisher_pk primary key (id)
);

create table crud.book (
  id numeric(38) default nextval('crud.book_seq'),
  title varchar(255) not null,
  publisher_id numeric(38),
  constraint book_pk primary key (id),
  constraint book_fk foreign key (publisher_id) references crud.publisher(id) on delete set null
);

create table crud.author (
  id numeric(38) default nextval('crud.author_seq'),
  first_name VARCHAR(255) not null,
  last_name VARCHAR(255),
  constraint author_pk primary key (id)
);

create table crud.author_book (
  author_id numeric(38),
  book_id numeric(38),
  constraint author_book_pk primary key (author_id, book_id),
  constraint a_b_a_fk foreign key (author_id) references crud.author(id),
  constraint a_b_b_fk foreign key (book_id) references crud.book(id)
);

create unique index book_title on crud.book (title);
create unique index publisher_name on crud.publisher (name);
create index author_f_l_name on crud.author (first_name, last_name);
create index book_publisher on crud.book (publisher_id);