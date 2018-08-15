from app.common import Singleton, log
from app.model import EntityDecoder, EntityEncoder
from app.repository import Repository
import json


class Service(metaclass=Singleton):
    def __init__(self):
        self.repository = Repository()

    @log
    def save(self, body):
        entity = json.loads(body, cls=EntityDecoder)

        return json.dumps(self.repository.create(entity), cls=EntityEncoder)

    @log
    def remove(self, id):
        return json.dumps(self.repository.remove(id), cls=EntityEncoder)

    @log
    def get(self, id):
        return json.dumps(self.repository.get(id), cls=EntityEncoder)

    @log
    def update(self, body):
        entity = json.loads(body, cls=EntityDecoder)

        return json.dumps(self.repository.update(entity), cls=EntityEncoder)
