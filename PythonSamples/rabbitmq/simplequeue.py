import pika

if __name__ == '__main__':
    connection = pika.BlockingConnection(pika.ConnectionParameters('localhost'))
    channel = connection.channel()

    channel.queue_declare(queue="queue",
                          passive=False,
                          durable=False,
                          exclusive=False,
                          auto_delete=True,
                          arguments=None)

    channel.basic_publish(exchange="",
                          routing_key="queue",
                          body="Message first",
                          properties=None,
                          mandatory=False,
                          immediate=False)

    channel.basic_publish(exchange="",
                          routing_key="queue",
                          body="Message second",
                          properties=None,
                          mandatory=False,
                          immediate=False)


    def consume(ch, method, properties, body):
        print("Message [1]: {}".format(body))


    channel.basic_consume(consumer_callback=consume,
                          queue="queue",
                          no_ack=True,
                          exclusive=False)

    channel.start_consuming()

    connection.close()
