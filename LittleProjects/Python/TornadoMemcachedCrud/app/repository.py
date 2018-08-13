from app.common import Singleton, log
import memcache


class Repository(object, metaclass=Singleton):
    def __init__(self):
        self.client = memcache.Client(('192.168.99.100:11211',))
        self.client.set('id', 0)

    @log
    def create(self, entity):
        next_id = self.client.incr('id')
        entity.entity_id = next_id
        self.client.set(str(hash(entity)), entity)
        return entity

    @log
    def update(self, entity):
        self.client.replace(str(hash(entity)), entity)
        return entity

    @log
    def remove(self, entity_id):
        return self.client.delete(str(hash(int(entity_id))))

    @log
    def get(self, entity_id):
        return self.client.get(str(hash(int(entity_id))))
