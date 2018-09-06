from app.common import Singleton, log
import memcache


class Repository(object, metaclass=Singleton):
    def __init__(self):
        self.client = memcache.Client(('localhost:11211',))
        self.client.set('id', 1)

    @log()
    def save(self, entity):
        next_id = self.client.get('id')
        self.client.incr('id', 1)
        entity.id = next_id
        self.client.set(str(hash(entity)), entity)
        return entity

    @log()
    def get(self, id):
        return self.client.get(str(hash(id)))

    @log()
    def remove(self, id):
        try:
            self.client.delete(str(hash(id)))
            return 'OK'
        except:
            return 'Error'

    @log()
    def update(self, entity):
        self.client.set(str(hash(entity)), entity)
        return entity
