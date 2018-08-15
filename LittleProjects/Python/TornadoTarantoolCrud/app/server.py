import tornado
import tornado.ioloop
import tornado.web
import logging
from app.service import Service

logger = logging.getLogger('tornado')


class AppHandler(tornado.web.RequestHandler):
    def initialize(self):
        self.service = Service()

    async def prepare(self):
        logger.info('Method: {0}'.format(self.request))

    async def get(self):
        self.set_header('Content-type', 'application/json')
        self.write(self.service.get(int(self.get_argument('id'))))

    async def post(self):
        self.set_header('Content-type', 'application/json')
        self.write(self.service.update(self.request.body))

    async def delete(self):
        self.set_header('Content-type', 'application/json')
        self.write(self.service.remove(int(self.get_argument('id'))))

    async def put(self):
        self.set_header('Content-type', 'application/json')
        self.write(self.service.save(self.request.body))


if __name__ == '__main__':
    app = tornado.web.Application([
        (r'/api', AppHandler)
    ])

    app.listen(8080)
    logger.info('Server started')
    tornado.ioloop.IOLoop.current().start()
