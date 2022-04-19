package cn.think.in.java.impl;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import cn.think.in.java.StateMachine;
import cn.think.in.java.entity.Command;
import cn.think.in.java.entity.LogEntry;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * redis实现状态机存储
 *
 * @author rensailong
 */
public class RedisStateMachine implements StateMachine {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisStateMachine.class);

    private static JedisPool jedisPool;

    static {
        GenericObjectPoolConfig redisConfig = new GenericObjectPoolConfig();
        redisConfig.setMaxTotal(100);
        redisConfig.setMaxWaitMillis(10 * 1000);
        redisConfig.setMaxIdle(1000);
        redisConfig.setTestOnBorrow(true);
        jedisPool = new JedisPool(redisConfig, "127.0.0.1", 6379);
    }

    public static RedisStateMachine getInstance() {
        return RedisStateMachine.RedisStateMachineLazyHolder.INSTANCE;
    }

    private static class RedisStateMachineLazyHolder {

        private static final RedisStateMachine INSTANCE = new RedisStateMachine();
    }

    @Override
    public void apply(LogEntry logEntry) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            Command command = logEntry.getCommand();
            if (command == null) {
                throw new IllegalArgumentException("command can not be null, logEntry : " + logEntry.toString());
            }
            String key = command.getKey();
            jedis.set(key.getBytes(), JSON.toJSONBytes(logEntry));
        } catch (Exception e) {
            LOGGER.info(e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public LogEntry get(String key) {
        LogEntry result = null;
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            result = JSON.parseObject(jedis.get(key), LogEntry.class);
        } catch (Exception e) {
            LOGGER.error("redis error ", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return result;
    }

    @Override
    public String getString(String key) {
        String result = null;
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            result = jedis.get(key);
        } catch (Exception e) {
            LOGGER.error("redis error ", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return result;
    }

    @Override
    public void setString(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.set(key, value);
        } catch (Exception e) {
            LOGGER.error("redis error ", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public void delString(String... keys) {
        Jedis jedis = null;
        try {
            jedis.del(keys);
        } catch (Exception e) {
            LOGGER.error("redis error ", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }
}
