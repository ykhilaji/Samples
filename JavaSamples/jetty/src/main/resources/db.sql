create schema jetty;

create table jetty.user (
  id SERIAL PRIMARY KEY ,
  first_name VARCHAR(255),
  last_name VARCHAR(255),
  email VARCHAR(255) NOT NULL
);

CREATE INDEX user_name on jetty.user(first_name, last_name);
CREATE UNIQUE INDEX user_email on jetty.user(email);

INSERT INTO jetty.user(first_name, last_name, email) VALUES ('user', 'user', 'user@user.com');
INSERT INTO jetty.user(first_name, last_name, email) VALUES ('user2', 'user2', 'user2@user2.com');

SELECT * from jetty.user;