from contextlib import contextmanager
import functools
import logging

logger = logging.getLogger('tornado')


class Singleton(type):
    _instances = {}

    def __call__(cls, *args, **kwargs):
        if cls not in cls._instances:
            cls._instances[cls] = super(Singleton, cls).__call__(*args, **kwargs)

        return cls._instances[cls]


@contextmanager
def transaction(repository, on_success=lambda: None, on_error=lambda: None, on_finally=lambda: None):
    session = repository()

    try:
        yield session
        on_success()
        session.commit()
    except:
        on_error()
        session.rollback()
        raise
    finally:
        on_finally()
        session.close()


def log(fn):
    @functools.wraps(fn)
    def wrapper(*args, **kwargs):
        logger.info('Call method: {0} with args: {1} and kwargs: {2}'.format(fn.__name__, args, kwargs))
        result = fn(*args, **kwargs)
        logger.info('Result: {0}'.format(result or 'void'))

        return result

    return wrapper
