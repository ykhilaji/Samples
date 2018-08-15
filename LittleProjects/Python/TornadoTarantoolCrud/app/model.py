from json import JSONEncoder, JSONDecoder


class Entity(object):
    def __init__(self, e_id=0, e_val=''):
        self.e_id = e_id
        self.e_val = e_val

    def __hash__(self):
        return hash(int(self.e_id))

    def to_tuple(self):
        return self.e_id or None, self.e_val


class EntityEncoder(JSONEncoder):
    def default(self, o):
        if isinstance(o, Entity):
            return {'type': 'entity', 'id': o.e_id, 'value': o.e_val}
        return super(EntityEncoder, self).default(o)


class EntityDecoder(JSONDecoder):
    def __init__(self):
        JSONDecoder.__init__(self, object_hook=self.object_hook)

    def object_hook(self, o):
        if o.get('type') == 'entity':
            return Entity(o['id'], o['value'])
        return o
