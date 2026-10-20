package util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtil {
    private static final JedisPool JEDIS_POOL;

    static {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(200);    // 最大连接数
        config.setMaxIdle(50);     // 最大空闲连接数
        config.setMinIdle(5);      // 最小空闲连接数
        config.setMaxWaitMillis(2000); // 获取连接最大等待时间

        // 初始化全局唯一的连接池
        JEDIS_POOL = new JedisPool(config, "127.0.0.1", 6379, 3000);
        System.out.println("[REDIS] Connection pool initialized globally");
    }
    private RedisUtil() {
    }
//    放入
    public static void set(String key, String value) {
        try (Jedis jedis = JEDIS_POOL.getResource()) {
            jedis.set(key, value);
        } catch (Exception e) {
            System.err.println("[REDIS] Set error: " + e.getMessage());
        }
    }
    
    public static void setex(String key, int seconds, String value) {
        try (Jedis jedis = JEDIS_POOL.getResource()) {
            jedis.setex(key, seconds, value);
        } catch (Exception e) {
            System.err.println("[REDIS] Setex error: " + e.getMessage());
        }
    }
    
    public static String get(String key) {
        try (Jedis jedis = JEDIS_POOL.getResource()) {
            return jedis.get(key);
        } catch (Exception e) {
            System.err.println("[REDIS] Get error: " + e.getMessage());
            return null;
        }
    }
    
    public static void del(String key) {
        try (Jedis jedis = JEDIS_POOL.getResource()) {
            jedis.del(key);
        } catch (Exception e) {
            System.err.println("[REDIS] Del error: " + e.getMessage());
        }
    }
    
    public static boolean exists(String key) {
        try (Jedis jedis = JEDIS_POOL.getResource()) {
            return jedis.exists(key);
        } catch (Exception e) {
            System.err.println("[REDIS] Exists error: " + e.getMessage());
            return false;
        }
    }
    
    public static void expire(String key, int seconds) {
        try (Jedis jedis = JEDIS_POOL.getResource()) {
            jedis.expire(key, seconds);
        } catch (Exception e) {
            System.err.println("[REDIS] Expire error: " + e.getMessage());
        }
    }
    
    public static void shutdown() {
        if (JEDIS_POOL != null && !JEDIS_POOL.isClosed()) {
            JEDIS_POOL.destroy();
            System.out.println("[REDIS] Connection pool closed");
        }
    }
}
