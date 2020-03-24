let amqp = require('amqplib/callback_api');

amqp.connect("amqp://192.168.99.100:5672", function (err, conn) {
    if(err) {
        throw new Error(err);
    }

    conn.createChannel(function (err, ch) {
        if(err) {
            throw new Error(err);
        }

        ch.assertExchange("topics", "topic", {durable: false, exclusive: true});

        ch.assertQueue("queue1", {durable:false, exclusive: true}, function (err, q) {
            if(err) {
                throw new Error(err);
            }

            ch.bindQueue(q.queue, "topics", "*.a.b");

            ch.consume(q.queue, function (msg) {
                console.log("Received [*.a.b]: " + msg.content.toString());
            }, {noAck: true});
        });

        setTimeout(function () {
            let types = ["c.a.b", "q.a.b", "random", "q", "q.a", "a.b.c"];

            for(let i = 0; i < 20; ++i) {
                let type = types[i % types.length];
                ch.publish("topics", type, new Buffer("Message [" + i + "] with type: " + type));
            }
        }, 1000);
    });
});

amqp.connect("amqp://192.168.99.100:5672", function (err, conn) {
    if(err) {
        throw new Error(err);
    }

    conn.createChannel(function (err, ch) {
        if(err) {
            throw new Error(err);
        }

        ch.assertExchange("topics", "topic", {durable: false, exclusive: true});

        ch.assertQueue("queue2", {durable:false, exclusive: true}, function (err, q) {
            if(err) {
                throw new Error(err);
            }

            ch.bindQueue(q.queue, "topics", "#");

            ch.consume(q.queue, function (msg) {
                console.log("Received [#]: " + msg.content.toString());
            }, {noAck: true});
        });
    });
});


amqp.connect("amqp://192.168.99.100:5672", function (err, conn) {
    if(err) {
        throw new Error(err);
    }

    conn.createChannel(function (err, ch) {
        if(err) {
            throw new Error(err);
        }

        ch.assertExchange("topics", "topic", {durable: false, exclusive: true});

        ch.assertQueue("queue3", {durable:false, exclusive: true}, function (err, q) {
            if(err) {
                throw new Error(err);
            }

            ch.bindQueue(q.queue, "topics", "c.*.b");

            ch.consume(q.queue, function (msg) {
                console.log("Received [c.*.b]: " + msg.content.toString());
            }, {noAck: true});
        });
    });
});

setTimeout(function () {
    process.exit();
}, 3000);