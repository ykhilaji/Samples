import psycopg2
import logging
import functools

logger = logging.getLogger('tornado')


def logger_wrapper(func):
    @functools.wraps(func)
    def wrapper(*args, **kwargs):
        logger.debug('Call function: {} with Args: {} and Kwargs: {}'.format(func.__name__, args, kwargs))
        try:
            result = func(*args, **kwargs)
            logger.debug('Result: {}'.format(result or 'OK'))
            commit()

            return result
        except Exception as e:
            logger.error(e)
            rollback()

            return []

    return wrapper


connection = psycopg2.connect("host='192.168.99.100' port='5432' dbname='postgres' user='postgres' password=''")

insert_book_template = """insert into tornado.book(title) VALUES (%s)"""
insert_author_template = """insert into tornado.author(first_name, last_name) VALUES (%s, %s)"""
insert_book_author_template = """insert into tornado.book_author(book_id, author_id) VALUES (%s, %s)"""

select_book_template = """select * from tornado.book WHERE id=%s"""
select_author_template = """select * from tornado.author WHERE id=%s"""

select_books_template = """select * from tornado.book"""
select_authors_template = """select * from tornado.author"""
select_books_by_author_name_template = """select * from tornado.book b inner join tornado.book_author ba
on b.book.id = ba.book_id inner join tornado.author a on ba.author_id = a.id where a.first_name=%s"""

delete_book_template = """delete from tornado.book WHERE id=%s"""
delete_author_template = """delete from tornado.author WHERE id=%s"""

update_book_template = """update tornado.book set title=%s WHERE id=%s"""
update_author_first_name_template = """update tornado.author set first_name=%s WHERE id=%s"""
update_author_last_name_template = """update tornado.author set last_name=%s WHERE id=%s"""


def commit():
    try:
        connection.commit()
    except Exception as e:
        logger.error(e)
        rollback()


def rollback():
    try:
        connection.rollback()
    except Exception as e:
        logger.error(e)


def close():
    try:
        connection.close()
    except Exception as e:
        logger.error(e)


@logger_wrapper
def insert_book(title):
    cursor = connection.cursor()
    cursor.execute(insert_book_template, (title,))

    return cursor.rowcount


@logger_wrapper
def insert_author(first_name, last_name):
    cursor = connection.cursor()
    cursor.execute(insert_author_template, (first_name, last_name))

    return cursor.rowcount


@logger_wrapper
def insert_book_author(book_id, author_id):
    cursor = connection.cursor()
    cursor.execute(insert_book_author_template, (book_id, author_id))

    return cursor.rowcount


@logger_wrapper
def select_book(book_id):
    cursor = connection.cursor()
    cursor.execute(select_book_template, (book_id,))

    return cursor.fetchone()


@logger_wrapper
def select_author(author_id):
    cursor = connection.cursor()
    cursor.execute(select_author_template, (author_id,))

    return cursor.fetchone()


@logger_wrapper
def select_books_by_author_first_name(first_name):
    cursor = connection.cursor()
    cursor.execute(select_books_by_author_name_template, (first_name,))

    return cursor.fetchall()


@logger_wrapper
def select_books():
    cursor = connection.cursor()
    cursor.execute(select_books_template)

    return cursor.fetchall()


@logger_wrapper
def select_authors():
    cursor = connection.cursor()
    cursor.execute(select_authors_template)

    return cursor.fetchall()


@logger_wrapper
def delete_book(book_id):
    cursor = connection.cursor()
    cursor.execute(delete_book_template, (book_id,))

    return cursor.rowcount


@logger_wrapper
def delete_author(author_id):
    cursor = connection.cursor()
    cursor.execute(delete_author_template, (author_id,))

    return cursor.rowcount


@logger_wrapper
def update_book(book_id, title):
    cursor = connection.cursor()
    cursor.execute(update_book_template, (title, book_id))

    return cursor.rowcount


@logger_wrapper
def update_author_first_name(author_id, first_name):
    cursor = connection.cursor()
    cursor.execute(update_author_first_name_template, (first_name, author_id))

    return cursor.rowcount


@logger_wrapper
def update_author_last_name(author_id, last_name):
    cursor = connection.cursor()
    cursor.execute(update_author_last_name_template, (last_name, author_id))

    return cursor.rowcount
