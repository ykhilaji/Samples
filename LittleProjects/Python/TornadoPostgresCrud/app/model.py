from json import JSONDecoder, JSONEncoder


class Entity(object):
    def __init__(self, id=0, value=''):
        self.id = id
        self.value = value

    def __repr__(self):
        return 'Entity {0}: {1}'.format(self.id, self.value)

    def __cmp__(self, other):
        if isinstance(other, Entity):
            return self.id == other.id and self.value == other.value
        return False

    def __hash__(self):
        return hash(self.id)


class EntityJsonDecoder(JSONDecoder):
    def __init__(self):
        JSONDecoder.__init__(self, object_hook=self.object_hook)

    def object_hook(self, o):
        if o.get('type') == 'entity':
            return Entity(id=o.get('id'), value=o.get('value'))

        return o


class EntityJsonEncoder(JSONEncoder):
    def default(self, o):
        if isinstance(o, Entity):
            return {'type': 'entity', 'id': o.id, 'value': o.value}
        return super().default(o)
