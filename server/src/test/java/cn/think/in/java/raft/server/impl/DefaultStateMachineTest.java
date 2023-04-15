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
import org.junit.Before;
import org.junit.Test;
import org.rocksdb.RocksDBException;

/**
 *
 * @author 莫那·鲁道
 */
public class DefaultStateMachineTest {
    static DefaultStateMachine machine = DefaultStateMachine.getInstance();

    static {
        System.setProperty("serverPort", "8777");
        machine.dbDir = "/Users/cxs/code/lu-raft-revert/rocksDB-raft/" + System.getProperty("serverPort");
        machine.stateMachineDir = machine.dbDir + "/stateMachine";
    }

    @Before
    public void before() {
        machine = DefaultStateMachine.getInstance();
    }

    @Test
    public void apply() {
        LogEntry logEntry = LogEntry.builder().term(1).command(Command.builder().key("hello").value("value1").build()).build();
        machine.apply(logEntry);
    }


    @Test
    public void applyRead() throws RocksDBException {

        System.out.println(machine.get("hello:7"));
    }
}
