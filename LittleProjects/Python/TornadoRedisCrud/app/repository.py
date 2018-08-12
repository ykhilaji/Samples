from app.common import Singleton, log
from app.model import Entity
import redis
import logging

logger = logging.getLogger('tornado')


class Repository(object, metaclass=Singleton):
    def __init__(self):
        self.connection_poll = redis.ConnectionPool(host='192.168.99.100', port=6379, db=0)
        self.client = redis.Redis(connection_pool=self.connection_poll)

    @log
    def save(self, entity):
        next_id = self.client.incr('entity_id')
        logger.info('Next id: {0}'.format(next_id))
        entity.entity_id = next_id
        logger.info('Save entity: {0} hash: {1}'.format(entity, hash(entity)))
        self.client.hmset(hash(entity), {'entity_id': entity.entity_id, 'entity_value': entity.entity_value})
        return entity

    @log
    def update(self, entity):
        with self.client.pipeline(transaction=True) as pipe:
            pipe.watch(hash(entity))
            pipe.hmset(hash(entity), {'entity_id': entity.entity_id, 'entity_value': entity.entity_value})
            return entity

    @log
    def remove(self, entity):
        with self.client.pipeline(transaction=True) as pipe:
            pipe.watch(hash(entity))
            return pipe.hdel(hash(entity), 'entity_id', 'entity_value')

    @log
    def remove_by_id(self, entity_id):
        with self.client.pipeline(transaction=True) as pipe:
            pipe.watch(hash(entity_id))  # entity class has the same hash as id
            return pipe.hdel(hash(entity_id), 'entity_id', 'entity_value')

    @log
    def get(self, entity_id):
        with self.client.pipeline(transaction=True) as pipe:
            pipe.watch(hash(entity_id))
            return Entity(**({k.decode(): v.decode() for k,v in pipe.hgetall(hash(entity_id)).items()}))
