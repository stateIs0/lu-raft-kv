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

import java.io.File;

import com.alibaba.fastjson.JSON;

import org.junit.Before;
import org.junit.Test;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author 莫那·鲁道
 */
public class RocksDBTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultStateMachine.class);

    private String dbDir = "./rocksDB-raft/" + System.getProperty("serverPort");
    private String stateMachineDir = dbDir + "/test";

    public RocksDB machineDb;

    static {
        RocksDB.loadLibrary();
    }

    public byte[] lastIndexKey = "LAST_INDEX_KEY".getBytes();

    public RocksDBTest() {
        try {
            System.setProperty("serverPort", "8078");
            File file = new File(stateMachineDir);
            if (!file.exists()) {
                file.mkdirs();
            }
            Options options = new Options();
            options.setCreateIfMissing(true);
            machineDb = RocksDB.open(options, stateMachineDir);

        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }


    public static RocksDBTest getInstance() {
        return RocksDBTestLazyHolder.INSTANCE;
    }

    private static class RocksDBTestLazyHolder {

        private static final RocksDBTest INSTANCE = new RocksDBTest();
    }

    RocksDBTest instance;

    @Before
    public void before() {
        instance = getInstance();
    }

    @Test
    public void test() throws RocksDBException {
        System.out.println(getLastIndex());
        System.out.println(get(getLastIndex()));

        write(new Cmd("hello", "value"));

        System.out.println(getLastIndex());

        System.out.println(get(getLastIndex()));

        deleteOnStartIndex(getLastIndex());

        write(new Cmd("hello", "value"));

        deleteOnStartIndex(1L);

        System.out.println(getLastIndex());

        System.out.println(get(getLastIndex()));


    }

    public synchronized void write(Cmd cmd) {
        try {
            cmd.setIndex(getLastIndex() + 1);
            machineDb.put(cmd.getIndex().toString().getBytes(), JSON.toJSONBytes(cmd));
        } catch (RocksDBException e) {
            e.printStackTrace();
        } finally {
            updateLastIndex(cmd.getIndex());
        }
    }

    public synchronized void deleteOnStartIndex(Long index) {
        try {
            for (long i = index; i <= getLastIndex(); i++) {
                try {
                    machineDb.delete((i + "").getBytes());
                } catch (RocksDBException e) {
                    e.printStackTrace();
                }
            }

        } finally {
            updateLastIndex(index - 1);
        }
    }

    public Cmd get(Long index) {
        try {
            if (index == null) {
                throw new IllegalArgumentException();
            }
            byte[] cmd = machineDb.get(index.toString().getBytes());
            if (cmd != null) {
                return JSON.parseObject(machineDb.get(index.toString().getBytes()), Cmd.class);
            }
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void updateLastIndex(Long index) {
        try {
            // overWrite
            machineDb.put(this.lastIndexKey, index.toString().getBytes());
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    public Long getLastIndex() {
        byte[] lastIndex = new byte[0];
        try {
            lastIndex = machineDb.get(this.lastIndexKey);
            if (lastIndex == null) {
                lastIndex = "0".getBytes();
            }
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
        return Long.valueOf(new String(lastIndex));
    }

    @Setter
    @Getter
    @ToString
    static class Cmd {

        Long index;
        String key;
        String value;

        public Cmd() {
        }

        public Cmd(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }
}
