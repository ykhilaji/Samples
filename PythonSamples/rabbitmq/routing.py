import pika

if __name__ == '__main__':
    connection = pika.BlockingConnection(pika.ConnectionParameters('localhost'))
    channel = connection.channel()

    channel.exchange_declare(exchange="routed",
                             exchange_type="direct",
                             auto_delete=True)

    channel.queue_declare(queue="queue_route",
                          passive=False,
                          durable=False,
                          exclusive=False,
                          auto_delete=True,
                          arguments=None)

    channel.queue_bind(queue="queue_route",
                       exchange="routed",
                       routing_key="key")

    channel.basic_publish(exchange="routed",
                          routing_key="key",
                          body="Message route")


    def consume(ch, method, properties, body):
        print("Message [queue_route]: {}".format(body))


    channel.basic_qos(prefetch_count=1)
    channel.basic_consume(consumer_callback=consume,
                          queue="queue_route",
                          no_ack=True,
                          exclusive=False)

    channel.start_consuming()

    connection.close()
