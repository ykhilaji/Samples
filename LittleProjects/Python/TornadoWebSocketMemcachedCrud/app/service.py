from app.common import Singleton, log, to_json
from app.repository import Repository
from app.model import Entity


class Service(object, metaclass=Singleton):
    def __init__(self):
        self.repository = Repository()

    @log()
    @to_json(clazz=Entity)
    def save(self, body):
        return self.repository.save(body)

    @log()
    @to_json(clazz=Entity)
    def get(self, id):
        return self.repository.get(id)

    @log()
    def remove(self, id):
        return self.repository.remove(id)

    @log()
    @to_json(clazz=Entity)
    def update(self, body):
        return self.repository.update(body)
