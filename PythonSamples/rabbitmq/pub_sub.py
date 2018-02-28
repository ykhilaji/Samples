import pika

if __name__ == '__main__':
    connection = pika.BlockingConnection(pika.ConnectionParameters('localhost'))
    channel = connection.channel()

    channel.exchange_declare(exchange="all",
                             exchange_type="fanout",
                             auto_delete=True)

    channel.queue_declare(queue="queue_all",
                          passive=False,
                          durable=False,
                          exclusive=False,
                          auto_delete=True,
                          arguments=None)

    channel.queue_bind(queue="queue_all",
                       exchange="all",
                       routing_key="")

    channel.basic_publish(exchange="all",
                          routing_key="",
                          body="Message first")


    def consume(ch, method, properties, body):
        print("Message [fanout]: {}".format(body))


    channel.basic_qos(prefetch_count=1)
    channel.basic_consume(consumer_callback=consume,
                          queue="queue_all",
                          no_ack=True,
                          exclusive=False)

    channel.start_consuming()

    connection.close()
