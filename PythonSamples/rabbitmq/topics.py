import pika

if __name__ == '__main__':
    connection = pika.BlockingConnection(pika.ConnectionParameters('localhost'))
    channel = connection.channel()

    channel.exchange_declare(exchange="topics",
                             exchange_type="topic",
                             auto_delete=True)

    channel.queue_declare(queue="queue_topics",
                          passive=False,
                          durable=False,
                          exclusive=False,
                          auto_delete=True,
                          arguments=None)

    channel.queue_bind(queue="queue_topics",
                       exchange="topics",
                       routing_key="*.b.#")

    channel.basic_publish(exchange="topics",
                          routing_key="a.b.c",
                          body="Message topic a.b.c")

    channel.basic_publish(exchange="topics",
                          routing_key="a.b",
                          body="Message topic a.b")

    channel.basic_publish(exchange="topics",
                          routing_key="d.b.e",
                          body="Message topic d.b.e")

    channel.basic_publish(exchange="topics",
                          routing_key="none",
                          body="Message topic")


    def consume(ch, method, properties, body):
        print("Message [topic]: {}".format(body))


    channel.basic_qos(prefetch_count=1)
    channel.basic_consume(consumer_callback=consume,
                          queue="queue_topics",
                          no_ack=True,
                          exclusive=False)

    channel.start_consuming()

    connection.close()
