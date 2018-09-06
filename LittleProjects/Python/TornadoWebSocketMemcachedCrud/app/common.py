import logging
import functools

logger = logging.getLogger('tornado')


class Singleton(type):
    instances_ = {}

    def __call__(cls, *args, **kwargs):
        if cls not in cls.instances_:
            cls.instances_[cls] = super(Singleton, cls).__call__(*args, **kwargs)

        return cls.instances_[cls]


def log(level=logging.INFO):
    def wrapper(fn):
        @functools.wraps(fn)
        def inner(*args, **kwargs):
            logger.log(level=level, msg='Call: {0} with {1} and {2}'.format(fn.__name__, args, kwargs))
            result = fn(*args, **kwargs)
            logger.log(level=level, msg='Result: {0}'.format(result or 'ok'))
            return result

        return inner

    return wrapper


def try_catch_finally(on_error_callback=lambda: None, on_finally_callback=lambda: None):
    def wrapper(fn):
        @functools.wraps(fn)
        def inner(*args, **kwargs):
            try:
                return fn(*args, **kwargs)
            except:
                if callable(on_error_callback):
                    on_error_callback()
                raise
            finally:
                if callable(on_finally_callback):
                    on_finally_callback()

        return inner

    return wrapper


import json
from json import JSONDecoder, JSONEncoder


class JsonSerializer(type):
    def __new__(mcs, *args, **kwargs):
        clazz = super(JsonSerializer, mcs).__new__(mcs, *args, **kwargs)

        if not hasattr(clazz, 'serialize') or not hasattr(clazz, 'deserialize'):
            raise RuntimeError('Class should implement both static methods: serialize and deserialize')

        serialize = getattr(clazz, 'serialize')
        deserialize = getattr(clazz, 'deserialize')

        class Encoder(JSONEncoder):
            def default(self, o):
                if isinstance(o, clazz):
                    d = serialize(o)
                    if isinstance(d, dict):
                        d.update({'__meta': clazz.__name__})
                        return d
                    else:
                        raise RuntimeError('Serialize method should return a dictionary')
                return super().default(o)

        class Decoder(JSONDecoder):
            def __init__(self):
                JSONDecoder.__init__(self, object_hook=self.object_hook)

            def object_hook(self, o):
                if o.get('__meta') == clazz.__name__:
                    return deserialize(o)
                return o

        clazz.json_encoder = Encoder
        clazz.json_decoder = Decoder

        return clazz


def to_json(clazz=None):
    def wrapper(fn):
        @functools.wraps(fn)
        def inner(*args, **kwargs):
            return json.dumps(fn(*args, **kwargs), cls=clazz and clazz.json_encoder or None)

        return inner

    return wrapper


def from_json(clazz=None):
    def wrapper(fn):
        @functools.wraps(fn)
        def inner(self, body, *args, **kwargs):
            body = json.loads(body, cls=clazz and clazz.json_decoder or None)
            return fn(self, body, *args, **kwargs)

        return inner

    return wrapper
