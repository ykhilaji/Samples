from app.service import Serice
import tornado.ioloop
import tornado.web
import logging
import json

logger = logging.getLogger('tornado')


class AppHandler(tornado.web.RequestHandler):
    def initialize(self):
        self.service = Serice()

    async def get(self, *args, **kwargs):
        if self.get_argument('id', None):
            self.write(self.service.get_by_id(int(self.get_argument('id'))))
        else:
            self.write(self.service.get_all())

    async def post(self, *args, **kwargs):
        self.write(self.service.update(self.request.body))

    async def delete(self, *args, **kwargs):
        if self.get_argument('id'):
            self.write(self.service.delete_by_id(int(self.get_argument('id'))))
        else:
            self.write(json.dumps('Incorrect id'))

    async def put(self, *args, **kwargs):
        self.write(self.service.save(self.request.body))

    async def prepare(self):
        logger.info('Request: {0}'.format(self.request))
        self.set_header('Content-type', 'application/json')


if __name__ == '__main__':
    app = tornado.web.Application([(r'/api', AppHandler)])
    app.listen(8080)
    logger.info('Starting the server . . .')
    tornado.ioloop.IOLoop.current().start()
