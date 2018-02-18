from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy import Column, String, Numeric, Text, ForeignKey, Sequence
from sqlalchemy.orm import relationship
from sqlalchemy.orm import sessionmaker


Base = declarative_base()


class User(Base):
    __tablename__ = 'user'
    __table_args__ = {'schema': 'sqlalchemy'}

    id = Column(Numeric(precision=38), primary_key=True, default=Sequence(schema='sqlalchemy', name='user_seq'))
    first_name = Column(String(length=255))
    last_name = Column(String(length=255))

    messages = relationship("Message", back_populates="user")
    groups = relationship("UserGroup", back_populates="users")

    def __repr__(self):
        return "Id: {} Name: {} Last name: {} Groups: {}".format(self.id, self.first_name, self.last_name, self.groups)


class Message(Base):
    __tablename__ = 'message'
    __table_args__ = {'schema': 'sqlalchemy'}

    id = Column(Numeric(precision=38), primary_key=True, default=Sequence(schema='sqlalchemy', name='message_seq'))
    user_id = Column(Numeric(precision=38), ForeignKey("sqlalchemy.user.id"))
    message = Column(Text)

    user = relationship("User", back_populates="messages")

    def __repr__(self):
        return "Id: {} From : {} Message: {}".format(self.id, self.user, self.message)


class Group(Base):
    __tablename__ = 'group'
    __table_args__ = {'schema': 'sqlalchemy'}

    id = Column(Numeric(precision=38), primary_key=True, default=Sequence(schema='sqlalchemy', name='group_seq'))
    name = Column(String(length=255))

    users = relationship("UserGroup", back_populates="groups")

    def __repr__(self):
        return "Id: {} Name: {} Users: {}".format(self.id, self.name, self.users)


class UserGroup(Base):
    __tablename__ = 'user_group'
    __table_args__ = {'schema': 'sqlalchemy'}

    user_id = Column(Numeric(precision=38), ForeignKey("sqlalchemy.user.id"), primary_key=True)
    group_id = Column(Numeric(precision=38), ForeignKey("sqlalchemy.group.id"), primary_key=True)

    users = relationship("User", back_populates="groups")
    groups = relationship("Group", back_populates="users")


def create_session():
    engine = create_engine("postgresql://sa:sa@192.168.99.100:5432/sa")
    Base.metadata.create_all(engine)
    return sessionmaker(bind=engine)
