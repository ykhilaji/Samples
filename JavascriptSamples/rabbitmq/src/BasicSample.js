let amqp = require('amqplib/callback_api');

amqp.connect("amqp://192.168.99.100:5672", function (err, conn) {
    console.log("Connected");

    conn.createChannel(function (err, ch) {
        console.log("Channel created");

        ch.assertQueue("sample", {durable: false});
        ch.sendToQueue("sample", new Buffer("First message"));

        console.log("Message was sent");

        setTimeout(function () {
            ch.assertQueue("sample",  {durable: false});
            ch.consume("sample", function (msg) {
                console.log("Received: " + msg.content.toString());
            }, {noAck: true});
        }, 1000);
    });
});

setTimeout(function () {
    process.exit();
}, 2000);

