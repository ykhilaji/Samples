from app.repository import Repository
from app.model import EntityEncoder, EntityDecoder
import json
import tornado
import tornado.web
import tornado.ioloop
import logging

logger = logging.getLogger('tornado')


class AppHandler(tornado.web.RequestHandler):
    def initialize(self):
        self.repository = Repository()

    def get(self):
        self.write(json.dumps(self.repository.get(self.get_argument('id')), cls=EntityEncoder))

    def post(self):
        self.write(
            json.dumps(self.repository.update(json.loads(self.request.body, cls=EntityDecoder)), cls=EntityEncoder))

    def put(self):
        self.write(
            json.dumps(self.repository.create(json.loads(self.request.body, cls=EntityDecoder)), cls=EntityEncoder))

    def delete(self):
        self.write(json.dumps(self.repository.remove(self.get_argument('id'))))


if __name__ == '__main__':
    app = tornado.web.Application([
        (r'/api', AppHandler),
    ])
    app.listen(8080)
    logger.info('Server started')
    tornado.ioloop.IOLoop.current().start()
