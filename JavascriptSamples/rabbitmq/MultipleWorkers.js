let amqp = require('amqplib/callback_api');

amqp.connect("amqp://localhost", function (err, conn) {
    console.log("Connected [1]");

    conn.createChannel(function (err, ch) {
        console.log("Channel [1] created");

        ch.assertQueue("sample", {durable: false});
        setTimeout(function () {
            for(let i = 0; i < 20; ++i) {
                ch.sendToQueue("sample", new Buffer("Message[" + i + "]"));
            }
        }, 1000);

        setTimeout(function () {
            ch.assertQueue("sample",  {durable: false});
            console.log("Consumer 1 is ready");
            ch.consume("sample", function (msg) {
                console.log("Worker[1]: " + msg.content.toString());
            }, {noAck: true});
        }, 10);
    });
});


amqp.connect("amqp://localhost", function (err, conn) {
    console.log("Connected [2]");

    conn.createChannel(function (err, ch) {
        console.log("Channel [2] created");

        setTimeout(function () {
            ch.assertQueue("sample",  {durable: false});
            console.log("Consumer 2 is ready");
            ch.consume("sample", function (msg) {
                console.log("Worker[2]: " + msg.content.toString());
            }, {noAck: true});
        }, 10);
    });
});

setTimeout(function () {
    process.exit();
}, 3000);

