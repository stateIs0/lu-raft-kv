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

import cn.think.in.java.raft.common.entity.Command;
import cn.think.in.java.raft.common.entity.LogEntry;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;



/**
 * @author 莫那·鲁道
 */
public class DefaultLogModuleTest {

    static DefaultLogModule defaultLogs = DefaultLogModule.getInstance();

    static {
        System.setProperty("serverPort", "8779");
        defaultLogs.dbDir = "/Users/cxs/code/lu-raft-revert/rocksDB-raft/" + System.getProperty("serverPort");
        defaultLogs.logsDir = defaultLogs.dbDir + "/logModule";
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("serverPort", "8777");
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void write() {
        LogEntry entry = LogEntry.builder().
                term(1).
                command(Command.builder().key("hello").value("world").build()).
                build();
        defaultLogs.write(entry);

        Assert.assertEquals(entry, defaultLogs.read(entry.getIndex()));
    }

    @Test
    public void read() {
        System.out.println(defaultLogs.getLastIndex());
    }

    @Test
    public void remove() {
        defaultLogs.removeOnStartIndex(3L);
    }

    @Test
    public void getLast() {

    }

    @Test
    public void getLastIndex() {
    }

    @Test
    public void getDbDir() {
    }

    @Test
    public void getLogsDir() {
    }

    @Test
    public void setDbDir() {
    }
}
