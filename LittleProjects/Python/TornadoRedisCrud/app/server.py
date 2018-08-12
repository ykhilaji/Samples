import logging
import tornado
import tornado.web
import tornado.ioloop
from app.service import Service

logger = logging.getLogger('tornado')


class EntityHandler(tornado.web.RequestHandler):
    def initialize(self):
        self.service = Service()

    async def prepare(self):
        logger.info('Call: {0}'.format(self.request))

    async def get(self):
        self.set_header('Content-type', 'application/json')
        self.write(self.service.get(entity_id=int(self.get_argument('id', 0))))

    async def post(self):
        self.set_header('Content-type', 'application/json')
        self.write(self.service.update(body=self.request.body))

    async def delete(self):
        self.set_header('Content-type', 'application/json')
        self.write(self.service.remove_by_id(body=self.request.body))

    async def put(self):
        self.set_header('Content-type', 'application/json')
        self.write(self.service.save(body=self.request.body))


if __name__ == '__main__':
    logger.info('Start app')
    app = tornado.web.Application([(r'/api', EntityHandler)])
    app.listen(8080)
    tornado.ioloop.IOLoop.current().start()
