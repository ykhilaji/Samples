from sqlalchemysample.model import User, Group, Message, create_session


if __name__ == '__main__':
    Session = create_session()
    session = Session()

    try:
        result = session.execute("select 1").fetchall()

        user = User(id=1, first_name="name", last_name="last")
        message = Message(user_id=1, message="text")
        group = Group(name="group")

        session.add(user)
        session.add(message)
        session.add(group)

        users = session.query(User).all()
        groups = session.query(Group).all()
        messages = session.query(Message).all()

        print(result)
        print(users)
        print(groups)
        print(messages)

        session.rollback()
    finally:
        session.close()
