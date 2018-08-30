import redis from 'redis'
import bluebird from 'bluebird'

bluebird.promisifyAll(redis);

class Repository {
    constructor() {
        this.client = redis.createClient({host: '192.168.99.100', port: 6379});

        this.client.on('ready', err => {
            if (err) {
                console.log(err)
            } else {
                console.log('Client is ready');
            }
        });

        this.client.on('reconnecting', err => {
            if (err) {
                console.log(err)
            } else {
                console.log('Client is trying to reconnect');
            }
        });

        this.client.on('connect', err => {
            if (err) {
                console.log(err)
            } else {
                console.log('Client is connected');
            }
        });
    }

    getById(id) {
        return this.client.hgetallAsync(id);
    }

    insert(entity) {
        return this.client.multi()
            .incr('ids')
            .get('ids', (err, nextId) => {
                if (err) {
                    console.log(err);
                } else {
                    this.client.hmsetAsync(nextId, {...entity, id: nextId})
                }
            }).execAsync();
    }

    update(entity) {
        return this.client.watchAsync(entity.id).then(() => {
            return this.client.multi()
                .hmset(entity.id, {...entity})
                .execAsync();
        });
    };

    deleteById(id) {
        return this.client.watchAsync(id).then(() => {
            return this.client.multi()
                .del(id)
                .execAsync();
        });
    }
}

export default Repository;