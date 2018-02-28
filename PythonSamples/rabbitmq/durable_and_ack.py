import pika

if __name__ == '__main__':
    connection = pika.BlockingConnection(pika.ConnectionParameters('localhost'))
    channel = connection.channel()

    channel.queue_declare(queue="queue_durable",
                          passive=False,
                          durable=True, # durable queue
                          exclusive=False,
                          auto_delete=True,
                          arguments=None)

    channel.basic_publish(exchange="",
                          routing_key="queue_durable",
                          body="Message first",
                          properties=pika.BasicProperties(delivery_mode=2)) # durable message


    def consume(ch, method, properties, body):
        print("Message: {}".format(body))
        ch.basic_ack(delivery_tag=method.delivery_tag)


    channel.basic_qos(prefetch_count=1)
    channel.basic_consume(consumer_callback=consume,
                          queue="queue_durable",
                          no_ack=False,
                          exclusive=False)

    channel.start_consuming()

    connection.close()
