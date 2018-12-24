let mysql = require('mysql');

const pool = mysql.createPool({
    connectionLimit : 10,
    host     : '192.168.99.100',
    user     : 'root',
    port     : 3306,
    password : 'root',
    database : 'crud',
    insecureAuth : true
});


pool.on('acquire', function (connection) {
    console.log('Connection %d acquired', connection.threadId);
});

pool.on('enqueue', function () {
    console.log('Waiting for available connection slot');
});

pool.on('release', function (connection) {
    console.log('Connection %d released', connection.threadId);
});

export default pool;