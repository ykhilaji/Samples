const { Pool } = require('pg');
const pool = new Pool({
    host: "192.168.99.100",
    port: "5432",
    user: "postgres",
    password: "",
    database: "postgres",
    max: 10,
    connectionTimeoutMillis: 2000,
    idleTimeoutMillis: 10000
});

pool.on('connect', (client) => {
    console.log(`Client connected - Total count: ${pool.totalCount} Idle count: ${pool.idleCount} Waiting count: ${pool.waitingCount}`)
});

pool.on('acquire', (client) => {
    console.log(`Acquiring new client connection - Total count: ${pool.totalCount} Idle count: ${pool.idleCount} Waiting count: ${pool.waitingCount}`)
});

pool.on('error', (err, client) => {
    console.log(err);
});

pool.on('remove', (client) => {
    console.log(`Remove client - Total count: ${pool.totalCount} Idle count: ${pool.idleCount} Waiting count: ${pool.waitingCount}`)
});

// also acquires first idle client
pool.query('select query from pg_stat_activity where query != \'\'')
    .then((result) => console.log(result.rows))
    .catch((err) => console.log(err));

(async function () {
    const client = await pool.connect();
    let result = await client.query('select $1::text', ['value']);
    console.log(result.rows); // text: 'value'
    console.log(result.fields); // Field {name: 'text', tableID: 0, columnID: 0, dataTypeID: 25, dataTypeSize: -1, dataTypeModifier: -1, format: 'text' }
    console.log(result.rowCount); // 1
    console.log(result.command); // select
    client.release();
})();

setTimeout(() => {
    pool.end(() => console.log("Stop pool"));
}, 3000);