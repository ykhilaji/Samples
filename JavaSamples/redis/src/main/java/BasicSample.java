import redis.clients.jedis.BinaryClient;
import redis.clients.jedis.GeoCoordinate;
import redis.clients.jedis.GeoUnit;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisDataException;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class BasicSample {
    private static Jedis jedis = new Jedis("localhost");

    public static void main(String[] args) throws InterruptedException {
        try {
            System.out.println(jedis.auth("password"));
        } catch (JedisDataException e) {
            System.out.println(e);
        }

        System.out.println(String.format("Select another DB: %s", jedis.select(1)));
        System.out.println(String.format("PING - %s", jedis.ping())); // PONG

        geoOperations();
        jedis.flushAll();
        hyperLogLogOperations();
        jedis.flushAll();
        listOperations();
        jedis.flushAll();
        hashOperations();
        jedis.flushAll();
        setOperations();
        jedis.flushAll();
        keyOperations();
        jedis.flushAll();
        databaseOperations();

        jedis.close();
    }

    public static void hyperLogLogOperations() {
        System.out.println("HyperLogLog OPERATIONS");
        System.out.println(String.format("PFADD: %s", jedis.pfadd("pf", "1", "2", "3")));
        System.out.println(String.format("PFCOUNT: %s", jedis.pfcount("pf")));
        jedis.pfadd("pf2", "4", "5", "6");
        System.out.println(String.format("PFMERGE: %s", jedis.pfmerge("pf3", "pf", "pf2")));
        System.out.println(String.format("PFCOUNT: %s", jedis.pfcount("pf3")));
    }

    public static void listOperations() {
        System.out.println("LIST OPERATIONS");
        System.out.println(String.format("LPUSH values: %s", jedis.lpush("list1", "value1", "value2", "value3", "value4", "value5")));
        System.out.println(String.format("LPUSH values: %s", jedis.lpush("list2", "value1", "value2", "value3", "value4", "value5")));
        System.out.println(String.format("LRANGE: %s", jedis.lrange("list1", 0, 3)));

//        System.out.println(String.format("BLPOP: %s", jedis.blpop("list1", "list2"))); // blocking lpop version
        System.out.println(String.format("LPOP: %s", jedis.lpop("list1")));
//        System.out.println(String.format("BRPOP: %s", jedis.brpop("list1", "list2")));// blocking rpop version
//        System.out.println(String.format("BRPOPLPUSH: %s", jedis.brpoplpush("list1", "list2", 0)));
        System.out.println(String.format("LINDEX: %s", jedis.lindex("list1", 0)));
        System.out.println(String.format("LINSERT: %s", jedis.linsert("list1", BinaryClient.LIST_POSITION.BEFORE, "value3", "insertedBeforeValue3")));
        System.out.println(String.format("LLEN: %s", jedis.llen("list1")));
        System.out.println(String.format("LPUSHX: %s", jedis.lpushx("list1", "a"))); // insert only if key existst
        System.out.println(String.format("LREM: %s", jedis.lrem("list1", 1, "a"))); // remove 1 element equal to a moving from head to tail.
//        count > 0: Remove elements equal to value moving from head to tail.
//        count < 0: Remove elements equal to value moving from tail to head.
//        count = 0: Remove all elements equal to value.
        System.out.println(String.format("LSET: %s", jedis.lset("list1", 1, "newValue")));
        System.out.println(String.format("LTRIM: %s", jedis.ltrim("list1", 0, -2))); // ~ slice[0:-2]
        System.out.println(String.format("RPOP: %s", jedis.rpop("list2")));
//        System.out.println(String.format("RPOPLPUSH: %s", jedis.rpoplpush("list1", "list3")));
        System.out.println(String.format("RPUSH: %s", jedis.rpush("list1", "tail")));
        System.out.println(String.format("RPUSHX: %s", jedis.rpushx("list1", "onlyIfKeyExists")));
//        System.out.println(String.format("SORT: %s", jedis.sort("list1")));
    }

    public static void setOperations() {
        System.out.println("SET OPERATIONS");
    }

    public static void keyOperations() throws InterruptedException {
        System.out.println("KEY OPERATIONS");
        System.out.println(String.format("SET: %s", jedis.set("key1", "value1")));
        jedis.set("key2", "value2");
        jedis.set("key3", "value3");
        jedis.set("key4", "value4");
        jedis.set("key5", "value5");
        jedis.set("forRename", "val");
        jedis.set("key", "val");
        System.out.println(String.format("DEL: %s", jedis.del("key1"))); // blocking operation
        byte[] dump = jedis.dump("key2");
        System.out.println(String.format("DUMP: %s", Arrays.toString(dump)));
        System.out.println(String.format("EXISTS: %s", jedis.exists("key3")));
        System.out.println(String.format("EXPIRE: %s", jedis.expire("key2", 1)));
        Thread.sleep(2000);
        System.out.println(String.format("EXISTS: %s", jedis.exists("key2")));
        System.out.println(String.format("EXPIREAT: %s", jedis.expireAt("key3", Calendar.getInstance().getTimeInMillis() + 10)));
        System.out.println(String.format("KEYS: %s", jedis.keys("key[0-9]")));
        try {
            System.out.println(String.format("MIGRATE: %s", jedis.migrate("localhost", 6379, "key4", 0, 5))); // migrate key to another redis instance
        } catch (JedisDataException e) {
            System.out.println(e);
        }
        System.out.println(String.format("EXISTS key5: %s", jedis.exists("key5")));
        System.out.println(String.format("MOVE key5 to 0 db: %s", jedis.move("key5", 0))); // migrate key to another database in current instance
        System.out.println(String.format("EXISTS key5: %s", jedis.exists("key5")));
        System.out.println(String.format("OBJECT: %s", jedis.objectEncoding("key4")));
        System.out.println(String.format("PERSIST: %s", jedis.persist("key4"))); // remove TTL -> key will not expire
        System.out.println(String.format("PEXPIRE: %s", jedis.pexpire("key4", 5000))); // milliseconds
        System.out.println(String.format("PEXPIREAT: %s", jedis.pexpireAt("key3", Calendar.getInstance().getTimeInMillis() + 10)));
        System.out.println(String.format("PTTL: %s", jedis.pttl("key4")));
        System.out.println(String.format("RANDOMKEY: %s", jedis.randomKey()));
        System.out.println(String.format("RENAME: %s", jedis.rename("forRename", "renamed")));
        System.out.println(String.format("RENAMENX: %s", jedis.renamenx("renamed", "renamedTwice"))); // rename key only if key exists
        System.out.println(String.format("RESTORE: %s", jedis.restore("key2", 0, dump)));
        System.out.println(String.format("SCAN: %s", jedis.scan("0")));
        System.out.println(String.format("TTL: %s", jedis.ttl("key4")));
        System.out.println(String.format("TYPE: %s", jedis.type("key4")));
        System.out.println(String.format("WAIT: %s", jedis.watch("key4")));
    }

    public static void hashOperations() {
        System.out.println("HASH OPERATIONS");
        System.out.println(String.format("HSET: %s", jedis.hset("hash", "field1", "value1"))); // 1 if field is a new field in the hash and value was set. 0 if field already exists in the hash and the value was updated
        System.out.println(String.format("HSETNX: %s", jedis.hset("hash", "field1", "value2"))); // 1 if field is a new field in the hash and value was set. 0 if field already exists in the hash and no operation was performed
        Map<String, String> map = new HashMap<String, String>();
        map.put("field1", "value1");
        map.put("field2", "value2");
        map.put("field3", "value3");
        System.out.println(String.format("HMSET: %s", jedis.hmset("hash2", map)));
        System.out.println(String.format("HVALS: %s", jedis.hvals("hash2")));
        System.out.println(String.format("HSCAN: %s", jedis.hscan("hash2", "0").getResult().toString())); // [field3=value3, field1=value1, field2=value2]
        System.out.println(String.format("HMGET: %s", jedis.hmget("hash2", "field1", "field2", "field3", "not_existing_field_4")));
        System.out.println(String.format("HKEYS: %s", jedis.hkeys("hash2")));
        System.out.println(String.format("HLEN: %s", jedis.hlen("hash2")));
        System.out.println(String.format("HGETALL: %s", jedis.hgetAll("hash2")));
        System.out.println(String.format("HGET: %s", jedis.hget("hash", "field1")));
        System.out.println(String.format("HEXISTS: %s", jedis.hexists("hash", "unknown")));
        System.out.println(String.format("HDEL: %s", jedis.hdel("hash", "unknown"))); // 0
        System.out.println(String.format("HDEL: %s", jedis.hdel("hash", "field1"))); // 1

        jedis.hset("incr", "field1", "1");

        System.out.println(String.format("HINCRBY: %s", jedis.hincrBy("incr", "field1", 1)));
        System.out.println(String.format("HINCRBYFLOAT: %s", jedis.hincrByFloat("incr", "field1", 1.5)));
        System.out.println(String.format("HINCRBYFLOAT: %s", jedis.hincrByFloat("incr", "field1", -2.5)));
    }

    public static void geoOperations() {
        System.out.println("GEO OPERATIONS");
        Map<String, GeoCoordinate> map = new HashMap<String, GeoCoordinate>();
        map.put("1", new GeoCoordinate(10, 10));
        map.put("2", new GeoCoordinate(30, 30));
        map.put("3", new GeoCoordinate(50, 50));
        map.put("4", new GeoCoordinate(70, 70));
        map.put("5", new GeoCoordinate(85, 85));

        System.out.println(String.format("Geoadd: %s", jedis.geoadd("geo", map))); // 5
        System.out.println(String.format("Geohash: %s", jedis.geohash("geo", "1", "2"))); // [s1z0gs3y0z0, stm6dtm6dt0]
        System.out.println(String.format("Geopos: %s", jedis.geopos("geo", "1", "2", "10"))); // [(10.000002086162567,10.00000092823273), (30.00000089406967,30.000000249977013), null]
        System.out.println(String.format("Georadiusbymember: %s", jedis.georadiusByMember("geo", "3", 20, GeoUnit.KM)));
        System.out.println(String.format("Georadius: %s", jedis.georadius("geo", 25, 25, 40, GeoUnit.KM)));
        System.out.println(String.format("Geodist: %s", jedis.geodist("geo", "1", "2")));
    }

    public static void databaseOperations() {
        System.out.println("DAATABASE OPERATIONS");
        System.out.println(jedis.flushDB()); // OK - removes all keys from current DB
        System.out.println(jedis.flushAll()); // OK - removes all keys from whole server
    }
}
