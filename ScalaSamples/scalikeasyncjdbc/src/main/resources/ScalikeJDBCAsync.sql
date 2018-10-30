-- docker run -e POSTGRES_PASSWORD=sa -e POSTGRES_USER=sa -p 5432:5432 -d postgres

CREATE SCHEMA async;

CREATE SEQUENCE async.book_seq;
CREATE SEQUENCE async.bookstore_seq;

CREATE TABLE async.book (
  id NUMERIC(38) DEFAULT nextval('async.book_seq') PRIMARY KEY,
  title VARCHAR(255)
);

CREATE TABLE async.bookstore (
  id NUMERIC(38) DEFAULT nextval('async.bookstore_seq') PRIMARY KEY,
  name VARCHAR(255)
);

CREATE TABLE async.book_bookstore (
  book_id NUMERIC(38),
  bookstore_id NUMERIC(38),
  quantity NUMERIC(38) CHECK (quantity >= 0),
  CONSTRAINT book_bookstore_pk PRIMARY KEY (book_id, bookstore_id)
);

