import tarantool
from app.common import Singleton, log
from app.model import Entity
import logging

logger = logging.getLogger('tornado')


class Repository(metaclass=Singleton):
    def __init__(self):
        self.client = tarantool.connect(host='localhost', port=3301)
        self.space_id = self.initialize()
        logger.info('Space id: {0}'.format(self.space_id))

    def initialize(self):
        self.client.eval("box.schema.space.create('entities', {if_not_exists=true})")
        self.client.eval("box.schema.sequence.create('ids', {if_not_exists=true})")
        self.client.eval(
            "box.space.entities:create_index('primary', {unique=true, sequence='ids', if_not_exists=true, type='HASH', parts={1, 'unsigned'}})")
        self.client.eval(
            "box.space.entities:create_index('secondary', {unique=false, if_not_exists=true, type='TREE', parts={2, 'string', is_nullable=true}})")

        return self.client.eval('return box.space.entities.id')[0]

    @log
    def create(self, entity):
        return Entity(*self.client.insert(space_name=self.space_id, values=entity.to_tuple())[0])

    @log
    def remove(self, id):
        response = self.client.delete(space_name=self.space_id, key=id, index=0)
        return Entity(*response[0]) if response else Entity()

    @log
    def update(self, entity):
        response = self.client.update(space_name=self.space_id, key=entity.e_id, op_list=[('=', 1, entity.e_val)])
        return Entity(*response[0]) if response else Entity()

    @log
    def get(self, id):
        response = self.client.select(space_name=self.space_id, key=id)
        return Entity(*response[0]) if response else Entity()
