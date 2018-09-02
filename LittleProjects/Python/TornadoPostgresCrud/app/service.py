from app.repository import Repository
from app.common import Singleton, log
from app.model import EntityJsonEncoder, EntityJsonDecoder, Entity
import json


class Serice(object, metaclass=Singleton):
    def __init__(self):
        self.repository = Repository()

    @log
    def get_all(self):
        entities = [Entity(int(x[0]), x[1]) for x in self.repository.get_all()]
        return json.dumps(entities, cls=EntityJsonEncoder)

    @log
    def get_by_id(self, id):
        r = self.repository.get_by_id(id)
        entity = r and Entity(int(r[0]), r[1]) or Entity()
        return json.dumps(entity, cls=EntityJsonEncoder)

    @log
    def save(self, entity):
        return json.dumps(self.repository.save(json.loads(entity, cls=EntityJsonDecoder)), cls=EntityJsonEncoder)

    @log
    def delete_by_id(self, id):
        return json.dumps(self.repository.delete_by_id(id))

    @log
    def update(self, entity):
        return json.dumps(self.repository.update(json.loads(entity, cls=EntityJsonDecoder)), cls=EntityJsonEncoder)
