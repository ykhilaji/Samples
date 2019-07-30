CREATE SCHEMA go;

CREATE SEQUENCE go.author_seq;
CREATE SEQUENCE go.book_seq;
CREATE SEQUENCE go.library_seq;

CREATE TABLE go.author (
  id NUMERIC(38) DEFAULT nextval('go.author_seq'),
  first_name VARCHAR(255) NOT NULL ,
  last_name VARCHAR(255) NOT NULL ,
  CONSTRAINT author_pk PRIMARY KEY (id)
);

CREATE TABLE go.book (
  id NUMERIC(38) DEFAULT nextval('go.book_seq'),
  title VARCHAR(255) NOT NULL ,
  CONSTRAINT book_pk PRIMARY KEY (id)
);

CREATE TABLE go.library (
  id NUMERIC(38) DEFAULT nextval('go.library_seq'),
  city VARCHAR(255) NOT NULL ,
  CONSTRAINT library_pk PRIMARY KEY (id)
);

CREATE TABLE go.author_book (
  author_id NUMERIC(38),
  book_id NUMERIC(38),
  CONSTRAINT author_book_pk PRIMARY KEY (author_id, book_id),
  CONSTRAINT author_book_author_fk FOREIGN KEY (author_id) REFERENCES go.author(id),
  CONSTRAINT author_book_book_fk FOREIGN KEY (book_id) REFERENCES go.book(id)
);

CREATE TABLE go.library_book (
  library_id NUMERIC(38),
  book_id NUMERIC(38),
  CONSTRAINT library_book_pk PRIMARY KEY (library_id, book_id),
  CONSTRAINT library_book_library_fk FOREIGN KEY (library_id) REFERENCES go.library(id),
  CONSTRAINT library_book_book_fk FOREIGN KEY (book_id) REFERENCES go.book(id)
);

INSERT INTO go.author(first_name, last_name) VALUES ('q', 'q'), ('w', 'w'), ('e', 'e');
INSERT INTO go.book(title) VALUES ('book1'), ('book2'), ('book3');
INSERT INTO go.library(city) VALUES ('city1'), ('city2'), ('city3');
