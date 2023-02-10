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
package cn.think.in.java.impl;

import cn.think.in.java.StateMachine;
import cn.think.in.java.entity.Command;
import cn.think.in.java.entity.LogEntry;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.File;

/**
 * 默认的状态机实现.
 *
 * @author 莫那·鲁道
 */
@Slf4j
public class DefaultStateMachine implements StateMachine {

    /** public just for test */
    public String dbDir;
    public String stateMachineDir;

    public RocksDB machineDb;


    private DefaultStateMachine() {
        dbDir = "./rocksDB-raft/" + System.getProperty("server.port");
        stateMachineDir = dbDir + "/stateMachine";
        RocksDB.loadLibrary();

        File file = new File(stateMachineDir);
        boolean success = false;

        if (!file.exists()) {
            success = file.mkdirs();
        }
        if (success) {
            log.warn("make a new dir : " + stateMachineDir);
        }
        Options options = new Options();
        options.setCreateIfMissing(true);
        try {
            machineDb = RocksDB.open(options, stateMachineDir);
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
    }

    public static DefaultStateMachine getInstance() {
        return DefaultStateMachineLazyHolder.INSTANCE;
    }

    @Override
    public void init() throws Throwable {

    }

    @Override
    public void destroy() throws Throwable {
        machineDb.close();
        log.info("destroy success");
    }

    private static class DefaultStateMachineLazyHolder {
        private static final DefaultStateMachine INSTANCE = new DefaultStateMachine();
    }

    @Override
    public LogEntry get(String key) {
        try {
            byte[] result = machineDb.get(key.getBytes());
            if (result == null) {
                return null;
            }
            return JSON.parseObject(result, LogEntry.class);
        } catch (RocksDBException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    @Override
    public String getString(String key) {
        try {
            byte[] bytes = machineDb.get(key.getBytes());
            if (bytes != null) {
                return new String(bytes);
            }
        } catch (RocksDBException e) {
            log.error(e.getMessage());
        }
        return "";
    }

    @Override
    public void setString(String key, String value) {
        try {
            machineDb.put(key.getBytes(), value.getBytes());
        } catch (RocksDBException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public void delString(String... key) {
        try {
            for (String s : key) {
                machineDb.delete(s.getBytes());
            }
        } catch (RocksDBException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public synchronized void apply(LogEntry logEntry) {

        try {
            Command command = logEntry.getCommand();

            if (command == null) {
                throw new IllegalArgumentException("command can not be null, logEntry : " + logEntry.toString());
            }
            String key = command.getKey();
            machineDb.put(key.getBytes(), JSON.toJSONBytes(logEntry));
        } catch (RocksDBException e) {
            log.error(e.getMessage());
        }
    }

}
