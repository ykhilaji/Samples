from app.service import Service
from app.settings import TEMPLATE_PATH
from app.common import from_json
from app.model import Entity
import tornado.websocket
import tornado.web
import tornado.ioloop
import logging
import json

logger = logging.getLogger('tornado')


class IndexHandler(tornado.web.RequestHandler):
    def prepare(self):
        pass

    def get(self, *args, **kwargs):
        self.render(template_name='index.html')


class WebSockerHandler(tornado.websocket.WebSocketHandler):
    def initialize(self):
        self.service = Service()

    def on_message(self, message):
        logger.info('Message: {0}'.format(message))
        req = json.loads(message)

        if req['action'] == 'GET':
            logger.info('GET')
            self.write_message(self.service.get(int(req['payload']['id'])))
        if req['action'] == 'DELETE':
            logger.info('DELETE')
            self.write_message(self.service.remove(int(req['payload']['id'])))
        if req['action'] == 'UPDATE':
            logger.info('UPDATE')
            self.write_message(self.service.update(Entity(**req['payload'])))
        if req['action'] == 'SAVE':
            logger.info('SAVE')
            self.write_message(self.service.save(Entity(**req['payload'])))

    def on_close(self):
        logger.info('Websocket closed')

    def open(self, *args, **kwargs):
        logger.info('Websocket opened')


if __name__ == '__main__':
    settings = {
        'template_path': TEMPLATE_PATH
    }

    app = tornado.web.Application([
        ('/', IndexHandler),
        ('/api', WebSockerHandler)
    ], **settings)

    app.listen(8080)
    tornado.ioloop.IOLoop.current().start()
