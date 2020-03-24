const { Client } = require('pg');
const client = new Client({
    host: "192.168.99.100",
    port: "5432",
    user: "postgres",
    password: "",
    database: "postgres"
});

client.connect();

client.on('end', () => {
    console.log("Disconnected");
});

client.on('error', (err) => {
    console.log(err);
});

client.on('notification', (msg) => {
    console.log(msg.processId);
    console.log(msg.channel);
    console.log(msg.payload);
});

client.query('select query from pg_stat_activity where query !=\'\'  ', (err, result) => {
    if (err) throw err;

    console.log(result.rows);

    client.query('select $1::text', ['some value'], (err, result) => {
        if (err) throw err;

        console.log(result.rows);

        client.query('notify myChannel, \'Skipped message\'', (err, result) => {
            // first notify -> skipped
            if (err) throw err;
            client.query('listen myChannel', (err, result) => {
                // start to listen myChannel
                if (err) throw err;
                client.query('notify myChannel, \'Received message\'', (err, result) => {
                    if (err) throw err;
                    client.end();
                });
            });
        });
    });
});