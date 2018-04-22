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

module.exports = pool;