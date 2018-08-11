from app.service import Service
import tornado.ioloop
import tornado.web
import tornado.escape
import logging

logger = logging.getLogger('tornado')
service = Service()


class EntityHandler(tornado.web.RequestHandler):
    def get(self):
        entity_id = self.get_argument('id', None)

        if entity_id:
            self.write(service.get_by_id(entity_id=entity_id))
        else:
            self.write(service.get_all())

    def post(self, *args, **kwargs):
        self.write(service.update(self.request.body))

    def delete(self, *args, **kwargs):
        self.write(service.remove_by_id(self.request.body))

    def put(self, *args, **kwargs):
        self.write(service.save(self.request.body))


if __name__ == '__main__':
    logger.info('Start tornado crud')

    app = tornado.web.Application([
        (r"/api", EntityHandler),
    ])
    app.listen(8080)
    tornado.ioloop.IOLoop.current().start()
