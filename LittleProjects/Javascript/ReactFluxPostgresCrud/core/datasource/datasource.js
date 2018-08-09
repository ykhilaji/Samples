import { Pool } from 'pg'

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

export default pool;