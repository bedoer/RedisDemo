import redis.clients.jedis.Jedis;

public class JedisDemo {
    public static void main(String[] args) {
        Jedis jedis = new Jedis("127.0.0.1", 6379);
        String msg = jedis.ping();
        System.out.println(msg);
        jedis.close();
    }
}
