let amqp = require('amqplib/callback_api');

amqp.connect("amqp://192.168.99.100:5672", function (err, conn) {
    conn.createChannel(function (err, ch) {
        ch.assertExchange("exch", "fanout", {durable: false});

        ch.assertQueue("queue1", {durable:false, exclusive: true}, function (err, q) {
            ch.bindQueue(q.queue, "exch", "");

            ch.consume(q.queue, function (msg) {
                console.log("Received [1]: " + msg.content.toString());
            }, {noAck: true});
        });

        setTimeout(function () {
            for(let i = 0; i < 10; ++i) {
                ch.publish("exch", "", new Buffer("Message [" + i + "]"));
            }
        }, 1000);
    });
});

amqp.connect("amqp://192.168.99.100:5672", function (err, conn) {
    conn.createChannel(function (err, ch) {
        ch.assertExchange("exch", "fanout", {durable: false});

        ch.assertQueue("queue2", {durable:false, exclusive: true}, function (err, q) {
            ch.bindQueue(q.queue, "exch", "");

            ch.consume(q.queue, function (msg) {
                console.log("Received [2]: " + msg.content.toString());
            }, {noAck: true});
        });
    });
});

setTimeout(function () {
    process.exit();
}, 2000);