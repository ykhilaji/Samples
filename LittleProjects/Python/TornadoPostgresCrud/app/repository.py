from app.common import Singleton, log
from app.datasource import transaction


class Repository(object, metaclass=Singleton):
    def __init__(self):
        #  bad practice, but in this case - ok
        with transaction() as session:
            session.execute('create schema if not exists crud')
            session.execute('create sequence if not exists crud.entity_seq')
            session.execute("create table if not exists crud.entity(id numeric(38) primary key default nextval('crud.entity_seq'), value varchar(255))")

    @log
    def get_all(self):
        with transaction() as session:
            session.execute('select id, value from crud.entity')
            return session.fetchall()

    @log
    def get_by_id(self, id):
        with transaction() as session:
            session.execute('select id, value from crud.entity where id = %s', (id,))
            return session.fetchone()

    @log
    def save(self, entity):
        with transaction() as session:
            session.execute('insert into crud.entity(value) values (%s) returning id', (entity.value,))
            next_id = session.fetchone()
            entity.id = int(next_id)
            return entity

    @log
    def delete_by_id(self, id):
        with transaction() as session:
            session.execute('delete from crud.entity where id = %s', (id, ))
            return 'OK'

    @log
    def update(self, entity):
        with transaction() as session:
            session.execute('update crud.entity set value = %s where id = %s', (entity.value, entity.id))
            return entity
