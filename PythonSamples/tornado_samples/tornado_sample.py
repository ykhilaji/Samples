import tornado.ioloop
import tornado.web
import tornado.escape
from tornado_samples import dao
import logging
import json
import decimal

logger = logging.getLogger('tornado')
handler = logging.StreamHandler()
handler.setFormatter(logging.Formatter('[%(asctime)s]%(levelname)s:%(message)s'))
logger.addHandler(handler)
logger.setLevel(logging.DEBUG)


class DecimalEncoder(json.JSONEncoder):
    def default(self, o):
        if isinstance(o, decimal.Decimal):
            return int(o)
        return super(DecimalEncoder, self).default(o)


class BooksHandler(tornado.web.RequestHandler):
    def get(self):
        book_id = self.get_argument('id', None)

        if book_id:
            book = dao.select_book(book_id=book_id)
            self.write(json.dumps(book or [], cls=DecimalEncoder))
        else:
            author_first_name = self.get_argument('name', None)
            if author_first_name:
                books = dao.select_books_by_author_first_name(first_name=author_first_name)
                self.write(json.dumps(books or [], cls=DecimalEncoder))
            else:
                books = dao.select_books()
                self.write(json.dumps(books or [], cls=DecimalEncoder))

    def post(self, *args, **kwargs):
        data = tornado.escape.json_decode(self.request.body)
        logger.debug("Data: {}".format(data))

        title = data.get('title', None)

        if title:
            result = dao.insert_book(title=title)
            self.write(json.dumps(result, cls=DecimalEncoder))
        else:
            self.write(json.dumps({'status': 'error'}))

    def delete(self, *args, **kwargs):
        data = tornado.escape.json_decode(self.request.body)
        logger.debug("Data: {}".format(data))

        book_id = data.get('book_id', None)

        if book_id:
            result = dao.delete_book(book_id=book_id)
            self.write(json.dumps(result, cls=DecimalEncoder))
        else:
            self.write(json.dumps({'status': 'error'}, cls=DecimalEncoder))

    def put(self, *args, **kwargs):
        data = tornado.escape.json_decode(self.request.body)
        logger.debug("Data: {}".format(data))

        book_id = data.get('book_id', None)
        title = data.get('title', None)

        if book_id and title:
            result = dao.update_book(book_id=book_id, title=title)
            self.write(json.dumps(result, cls=DecimalEncoder))
        else:
            self.write(json.dumps({'status': 'error'}, cls=DecimalEncoder))


class AuthorsHandler(tornado.web.RequestHandler):
    def get(self):
        author_id = self.get_argument('id', None)

        if author_id:
            author = dao.select_author(author_id=author_id)
            self.write(json.dumps(author or [], cls=DecimalEncoder))
        else:
            authors = dao.select_authors()
            self.write(json.dumps(authors or [], cls=DecimalEncoder))

    def post(self, *args, **kwargs):
        data = tornado.escape.json_decode(self.request.body)
        logger.debug("Data: {}".format(data))

        first_name = data.get('first_name', None)
        last_name = data.get('last_name', None)

        if first_name and last_name:
            result = dao.insert_author(first_name=first_name, last_name=last_name)
            self.write(json.dumps(result, cls=DecimalEncoder))
        else:
            self.write(json.dumps({'status': 'error'}, cls=DecimalEncoder))

    def delete(self, *args, **kwargs):
        data = tornado.escape.json_decode(self.request.body)
        logger.debug("Data: {}".format(data))

        author_id = data.get('author_id', None)

        if author_id:
            result = dao.delete_author(author_id=author_id)
            self.write(json.dumps(result, cls=DecimalEncoder))
        else:
            self.write(json.dumps({'status': 'error'}, cls=DecimalEncoder))

    def put(self, *args, **kwargs):
        data = tornado.escape.json_decode(self.request.body)
        logger.debug("Data: {}".format(data))

        author_id = data.get('book_id', None)
        first_name = data.get('first_name', None)
        last_name = data.get('last_name', None)

        if author_id and first_name:
            result = dao.update_author_first_name(author_id=author_id, first_name=first_name)
            self.write(json.dumps(result, cls=DecimalEncoder))

        if author_id and last_name:
            result = dao.update_author_last_name(author_id=author_id, last_name=last_name)
            self.write(json.dumps(result, cls=DecimalEncoder))

        if not author_id and (not first_name or not last_name):
            self.write(json.dumps({'status': 'error'}, cls=DecimalEncoder))


class BookAuthorHandler(tornado.web.RequestHandler):
    def post(self):
        data = tornado.escape.json_decode(self.request.body)
        logger.debug("Data: {}".format(data))

        book_id = data.get('book_id', None)
        author_id = data.get('author_id', None)

        if book_id and author_id:
            result = dao.insert_book_author(book_id=book_id, author_id=author_id)
            self.write(json.dumps(result, cls=DecimalEncoder))
        else:
            self.write(json.dumps({'status': 'error'}, cls=DecimalEncoder))


def crud_app():
    return tornado.web.Application([
        (r"/books", BooksHandler),
        (r"/authors", AuthorsHandler),
        (r"/book_author", BookAuthorHandler)
    ])


if __name__ == "__main__":
    app = crud_app()
    app.listen(8888)
    tornado.ioloop.IOLoop.current().start()
