create schema spring;

create sequence spring.author_seq;
create sequence spring.book_seq;
create sequence spring.publisher_seq;

ALTER SEQUENCE spring.author_seq RESTART 1;
ALTER SEQUENCE spring.book_seq RESTART 1;
ALTER SEQUENCE spring.publisher_seq RESTART 1;

create table spring.publisher (
  id numeric(38) default nextval('spring.publisher_seq'),
  name varchar(255) not null,
  constraint publisher_pk primary key (id),
  constraint publisher_unique unique (name)
);

create table spring.book (
  id numeric(38) default nextval('spring.book_seq'),
  title varchar(255) not null,
  pages numeric(38) not null,
  publisher_id numeric(38),
  constraint book_pk primary key (id),
  constraint book_unique unique (title),
  constraint book_pages check (pages > 0),
  constraint book_fk foreign key (publisher_id) references spring.publisher(id) on delete set null
);

create index book_publisher on spring.book (publisher_id);

create table spring.author (
  id numeric(38) default nextval('spring.author_seq'),
  first_name varchar(255) not null,
  last_name varchar(255),
  constraint author_pk primary key (id)
);

create index author_name on spring.author (first_name, last_name);

create table spring.author_books (
  author_id numeric(38),
  book_id numeric(38),
  constraint author_books_pk primary key (author_id, book_id),
  constraint author_books_a_fk foreign key (author_id) references spring.author(id) on delete cascade,
  constraint author_books_b_fk foreign key (book_id) references spring.book(id) on delete cascade
);

insert into spring.publisher(name) values ('Pearson'), ('ThomsonReuters'), ('Scholastic'), ('Wiley'), ('Informa'), ('Shueisha'), ('Shogakukan'), ('Bonnier'), ('Cornelsen');
insert into spring.author(first_name, last_name) values ('A', 'K'), ('B', 'L'), ('C', 'M'), ('D', 'N'), ('E', null), ('F', 'O'), ('G', null), ('H', 'P'), ('I', 'Q'), ('J', 'R');
insert into spring.book(title, pages, publisher_id) values ('A', 10, 1), ('B', 20, 2), ('C', 15, 3), ('D', 153, 4), ('E', 641, 5), ('F', 843, 6), ('G', 1000, 7), ('H', 24, 8), ('I', 525, 9), ('J', 357, 1), ('K', 731, 2), ('L', 826, 3), ('M', 267, 4), ('N', 825, 5), ('O', 518, 6), ('P', 931, 7), ('Q', 136, 8), ('R', 25, 9), ('S', 673, 1), ('T', 846, 2), ('U', 156, 3), ('V', 817, 4), ('W', 528, 5), ('X', 810, 6), ('Y', 416, 7), ('Z', 58, 8);
insert into spring.author_books(author_id, book_id) values (1, 1), (2, 2), (3, 3), (4, 4), (5, 5), (6, 6), (7, 7), (8, 8), (9, 9), (10, 10), (1, 11), (2, 12), (3, 13), (4, 14), (5, 15), (6, 16), (7, 17), (8, 18), (9, 19), (10, 20), (1, 21), (2, 22), (3, 23), (4, 24), (5, 25), (6, 26), (7, 1), (8, 2), (9, 3), (10, 4), (1, 5), (3, 7), (4, 8), (5, 9), (6, 10), (7, 11), (8, 12);
