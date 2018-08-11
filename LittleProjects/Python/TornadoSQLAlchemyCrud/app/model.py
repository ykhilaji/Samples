from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy import Column, String, Integer

Base = declarative_base()


class Entity(Base):
    __tablename__ = 'entity'
    __table_args__ = {'schema': 'public'}

    entity_id = Column(Integer, autoincrement=True, primary_key=True)
    entity_value = Column(String(length=255), nullable=False)

    def __repr__(self):
        return "Entity[{0}: {1}]".format(self.entity_id, self.entity_value)
