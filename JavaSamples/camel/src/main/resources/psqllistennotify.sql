CREATE SCHEMA notify_sample;

CREATE TABLE notify_sample.entity (
  id SERIAL PRIMARY KEY,
  value VARCHAR(255)
);


CREATE OR REPLACE FUNCTION notify_event() RETURNS TRIGGER AS $notify_event$

DECLARE
  data json;
  notification json;

BEGIN
  IF (TG_OP = 'DELETE') THEN
    data = row_to_json(OLD);
  ELSE
    data = row_to_json(NEW);
  END IF;

  notification = json_build_object(
      'table',TG_TABLE_NAME,
      'action', TG_OP,
      'data', data);

  PERFORM pg_notify('events', notification::text);

  RETURN NULL;
END;
$notify_event$ LANGUAGE plpgsql;


CREATE TRIGGER entity_notify_event
AFTER INSERT OR UPDATE OR DELETE ON notify_sample.entity
FOR EACH ROW EXECUTE PROCEDURE notify_event();


INSERT INTO notify_sample.entity(value) VALUES ('1');
DELETE FROM notify_sample.entity WHERE value='1';