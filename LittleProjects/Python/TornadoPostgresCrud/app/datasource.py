from contextlib import contextmanager
import logging
from app.common import Singleton, log
from psycopg2 import pool

logger = logging.getLogger('tornado')


@contextmanager
def transaction(session=None):
    if not session:
        session = _DataSource().next()

    try:
        yield session.cursor()
        session.commit()
    except:
        session.rollback()
        raise
    finally:
        _DataSource().close(session)


class _DataSource(object, metaclass=Singleton):
    def __init__(self):
        self.pool = pool.SimpleConnectionPool(minconn=1, maxconn=10, host='192.168.99.100', database='postgres',
                                              user='postgres',
                                              password='', port=5432)
        logger.info('Connection pool established')

    @log
    def next(self):
        return self.pool.getconn()

    @log
    def close(self, connection):
        self.pool.putconn(connection)
