let amqp = require('amqplib/callback_api');

amqp.connect("amqp://192.168.99.100:5672", function (err, conn) {
    if(err) {
        throw new Error(err);
    }

    conn.createChannel(function (err, ch) {
        if(err) {
            throw new Error(err);
        }

        ch.assertExchange("exch_routing", "direct", {durable: false});

        ch.assertQueue("queue1", {durable:false, exclusive: true}, function (err, q) {
            if(err) {
                throw new Error(err);
            }

            ch.bindQueue(q.queue, "exch_routing", "first");

            ch.consume(q.queue, function (msg) {
                console.log("Received [first]: " + msg.content.toString());
            }, {noAck: true});
        });

        setTimeout(function () {
            let types = ["first", "second", "third"];

            for(let i = 0; i < 20; ++i) {
                let type = types[i % types.length];
                ch.publish("exch_routing", type, new Buffer("Message [" + i + "] with type: " + type));
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

        ch.assertExchange("exch_routing", "direct", {durable: false});

        ch.assertQueue("queue2", {durable:false, exclusive: true}, function (err, q) {
            if(err) {
                throw new Error(err);
            }

            ch.bindQueue(q.queue, "exch_routing", "second");

            ch.consume(q.queue, function (msg) {
                console.log("Received [ssecond]: " + msg.content.toString());
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

        ch.assertExchange("exch_routing", "direct", {durable: false});

        ch.assertQueue("queue3", {durable:false, exclusive: true}, function (err, q) {
            if(err) {
                throw new Error(err);
            }

            ch.bindQueue(q.queue, "exch_routing", "third");

            ch.consume(q.queue, function (msg) {
                console.log("Received [third]: " + msg.content.toString());
            }, {noAck: true});
        });
    });
});

setTimeout(function () {
    process.exit();
}, 3000);