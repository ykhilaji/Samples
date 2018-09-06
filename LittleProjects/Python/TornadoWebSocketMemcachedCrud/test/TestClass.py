from app.common import JsonSerializer


class TestClass(object, metaclass=JsonSerializer):
    def __init__(self, a, b):
        self.a = a
        self.b = b

    @classmethod
    def serialize(cls, o):
        return {'a': o.a, 'b': o.b}

    @classmethod
    def deserialize(cls, o):
        return TestClass(o['a'], o['b'])

    def __eq__(self, other):
        if isinstance(other, TestClass):
            return self.a == other.a and self.b == other.b

        return False

    def __repr__(self):
        return 'A: {0}, B: {1}'.format(self.a, self.b)
