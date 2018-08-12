from json import JSONDecoder, JSONEncoder


class Entity(object):
    def __init__(self, entity_id=0, entity_value=''):
        self.entity_id = entity_id
        self.entity_value = entity_value

    def __repr__(self) -> str:
        return 'Entity[{0}:{1}]'.format(self.entity_id, self.entity_value)

    def __hash__(self) -> int:
        return hash(self.entity_id)


class EntityDecoder(JSONDecoder):
    def __init__(self):
        JSONDecoder.__init__(self, object_hook=self.object_hook)

    def object_hook(self, o):
        if o.get('type') == 'entity':
            return Entity(entity_id=o.get('id', 0), entity_value=o['value'])

        return o


class EntityEncoder(JSONEncoder):
    def default(self, o):
        if isinstance(o, Entity):
            return {'type': 'entity', 'id': o.entity_id, 'value': o.entity_value}

        return super(EntityEncoder, self).default(o)
