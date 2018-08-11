import json
from json import JSONEncoder
from app.repository import Repository
from app.model import Entity
from app.common import log, transaction, Singleton


class EntityJsonEncoder(JSONEncoder):
    def default(self, o):
        if isinstance(o, Entity):
            return {'id': o.entity_id, 'value': o.entity_value}
        return json.JSONEncoder.default(self, o)


class Service(metaclass=Singleton):
    @log
    def save(self, json_body):
        with transaction(repository=Repository) as session:
            body = json.loads(json_body)
            entity = Entity(entity_value=body['value'])
            return json.dumps(session.save(entity=entity), cls=EntityJsonEncoder)

    @log
    def update(self, json_body):
        with transaction(repository=Repository) as session:
            body = json.loads(json_body)
            return json.dumps(session.update(entity_id=body['id'], entity_value=body['value']), cls=EntityJsonEncoder)

    @log
    def remove_by_id(self, json_body):
        with transaction(repository=Repository) as session:
            body = json.loads(json_body)
            return json.dumps(session.remove_by_id(entity_id=body['id']), cls=EntityJsonEncoder)

    @log
    def get_all(self):
        with transaction(repository=Repository) as session:
            return json.dumps(session.get_all(), cls=EntityJsonEncoder)

    @log
    def get_by_id(self, json_body):
        with transaction(repository=Repository) as session:
            body = json.loads(json_body)
            return json.dumps(session.get_by_id(entity_id=body['id']), cls=EntityJsonEncoder)
