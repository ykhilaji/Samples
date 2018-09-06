from app.common import JsonSerializer


class Entity(object, metaclass=JsonSerializer):
    def __init__(self, id=0, value=''):
        self.id = int(id)
        self.value = value

    def __cmp__(self, other):
        if isinstance(other, Entity):
            return self.id == other.id and self.value == other.value
        return False

    def __hash__(self):
        return hash(self.id)

    @classmethod
    def serialize(cls, o):
        return {'id': o.id, 'value': o.value}

    @classmethod
    def deserialize(cls, o):
        return Entity(o['id'], o['value'])
