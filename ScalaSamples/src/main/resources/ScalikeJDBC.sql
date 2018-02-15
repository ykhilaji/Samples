-- docker run -e POSTGRES_PASSWORD=sa -e POSTGRES_USER=sa -p 5432:5432 -d postgres

CREATE SCHEMA sync;

CREATE SEQUENCE sync.entity_seq;

CREATE TABLE sync.entity (
  id NUMERIC(38) DEFAULT nextval('sync.entity_seq') PRIMARY KEY ,
  value VARCHAR(255)
);

INSERT INTO sync.entity(value) VALUES ('val1'), ('val2'), ('val3')