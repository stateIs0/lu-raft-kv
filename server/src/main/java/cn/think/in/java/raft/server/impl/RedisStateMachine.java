/*
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package cn.think.in.java.raft.server.impl;

import cn.think.in.java.raft.server.StateMachine;
import cn.think.in.java.raft.common.entity.Command;
import cn.think.in.java.raft.common.entity.LogEntry;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * redis实现状态机存储
 *
 * @author rensailong
 */
@Slf4j
public class RedisStateMachine implements StateMachine {

    private JedisPool jedisPool;

    private RedisStateMachine() {
        init();
    }

    public static RedisStateMachine getInstance() {
        return RedisStateMachineLazyHolder.INSTANCE;
    }

    private static class RedisStateMachineLazyHolder {

        private static final RedisStateMachine INSTANCE = new RedisStateMachine();
    }

    @Override
    public void init() {
        GenericObjectPoolConfig redisConfig = new GenericObjectPoolConfig();
        redisConfig.setMaxTotal(100);
        redisConfig.setMaxWaitMillis(10 * 1000);
        redisConfig.setMaxIdle(100);
        redisConfig.setTestOnBorrow(true);
        // todo config
        jedisPool = new JedisPool(redisConfig, System.getProperty("redis.host", "127.0.0.1"), 6379);
    }

    @Override
    public void destroy() throws Throwable {
        jedisPool.close();
        log.info("destroy success");
    }

    @Override
    public void apply(LogEntry logEntry) {
        try (Jedis jedis = jedisPool.getResource()) {
            Command command = logEntry.getCommand();
            if (command == null) {
                // 忽略空日志
                log.warn("insert no-op log, logEntry={}", logEntry);
                return;
            }
            String key = command.getKey();
            jedis.set(key.getBytes(), JSON.toJSONBytes(logEntry));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public LogEntry get(String key) {
        LogEntry result = null;
        try (Jedis jedis = jedisPool.getResource()) {
            result = JSON.parseObject(jedis.get(key), LogEntry.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public String getString(String key) {
        String result = null;
        try (Jedis jedis = jedisPool.getResource()) {
            result = jedis.get(key);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public void setString(String key, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set(key, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delString(String... keys) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(keys);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
