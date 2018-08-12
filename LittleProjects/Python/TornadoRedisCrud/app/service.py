from app.repository import Repository
from app.common import Singleton, log
from app.model import EntityEncoder, EntityDecoder
import json


class Service(object, metaclass=Singleton):
    def __init__(self):
        self.repository = Repository()

    @log
    def save(self, body):
        entity = json.loads(body, cls=EntityDecoder)
        return json.dumps(self.repository.save(entity=entity), cls=EntityEncoder)

    @log
    def update(self, body):
        entity = json.loads(body, cls=EntityDecoder)
        return json.dumps(self.repository.update(entity=entity), cls=EntityEncoder)

    @log
    def remove(self, body):
        entity = json.loads(body, cls=EntityDecoder)
        return json.dumps(self.repository.remove(entity=entity))

    @log
    def remove_by_id(self, body):
        return json.dumps(self.repository.remove_by_id(entity_id=json.loads(body)['id']))

    @log
    def get(self, entity_id):
        return json.dumps(self.repository.get(entity_id=entity_id), cls=EntityEncoder)
