import logging
import functools

logger = logging.getLogger('tornado')


class Singleton(type):
    instances_ = {}

    def __call__(cls, *args, **kwargs):
        if cls not in cls.instances_:
            cls.instances_[cls] = super(Singleton, cls).__call__(*args, **kwargs)

        return cls.instances_[cls]


def log(fn):
    @functools.wraps(fn)
    def wrapper(*args, **kwargs):
        logger.info('Call: {0} with args: {1} and kwargs: {2}'.format(fn.__name__, args, kwargs))
        result = fn(*args, **kwargs)
        logger.info('Result: {0}'.format(result or 'void'))

        return result

    return wrapper
