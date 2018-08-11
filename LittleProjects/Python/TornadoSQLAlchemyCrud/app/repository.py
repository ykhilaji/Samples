from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from app.common import Singleton
from app.model import Base, Entity


class EngineCreator(object, metaclass=Singleton):
    def __init__(self):
        self.engine = create_engine('postgresql://postgres:@192.168.99.100:5432/postgres',
                                    pool_size=5, max_overflow=0)
        Base.metadata.create_all(self.engine)
        self.Session = sessionmaker(self.engine)

    def new_session(self):
        return self.Session()


class Repository(object):
    def __init__(self):
        self.session = EngineCreator().new_session()

    def save(self, entity):
        return self.session.add(entity)

    def update(self, entity_id, entity_value):
        return self.session.query(Entity).filter_by(entity_id=entity_id).update({'entity_value': entity_value})

    def remove(self, entity):
        return self.session.delete(entity)

    def remove_by_id(self, entity_id):
        return self.session.query(Entity).filter_by(entity_id=entity_id).delete()

    def get_by_id(self, entity_id):
        return self.session.query(Entity).filter_by(entity_id=entity_id).one_or_none()

    def get_all(self):
        return self.session.query(Entity).all()

    def commit(self):
        self.session.commit()

    def rollback(self):
        self.session.rollback()

    def close(self):
        self.session.close()
