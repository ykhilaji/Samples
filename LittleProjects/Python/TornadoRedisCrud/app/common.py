import logging
import functools

logger = logging.getLogger('tornado')


class Singleton(type):
    _instances = {}

    def __call__(cls, *args, **kwargs):
        if cls not in cls._instances:
            cls._instances[cls] = super(Singleton, cls).__call__(*args, **kwargs)

        return cls._instances[cls]


def log(fn):
    @functools.wraps(fn)
    def wrapper(*args, **kwargs):
        logger.info('Call function: {0} with args: {1} and kwargs: {2}'.format(fn.__name__, args, kwargs))
        result = fn(*args, **kwargs)
        logger.info('Result: {0}'.format(result or 'void'))
        return result

    return wrapper
