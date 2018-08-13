from json import JSONDecoder, JSONEncoder


class Entity(object):
    def __init__(self, entity_id=0, entity_value=''):
        self.entity_id = entity_id
        self.entity_value = entity_value

    def __hash__(self):
        return hash(int(self.entity_id))


class EntityEncoder(JSONEncoder):
    def default(self, o):
        if isinstance(o, Entity):
            return {'type': 'entity', 'id': o.entity_id, 'value': o.entity_value}

        return super().default(o)


class EntityDecoder(JSONDecoder):
    def __init__(self):
        JSONDecoder.__init__(self, object_hook=self.object_hook)

    def object_hook(self, o):
        if o.get('type') == 'entity':
            return Entity(o.get('id', 0), o['value'])

        return o
