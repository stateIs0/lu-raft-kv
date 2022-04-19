package cn.think.in.java.impl;

import org.junit.Before;
import org.junit.Test;
import org.rocksdb.RocksDBException;

import cn.think.in.java.entity.Command;
import cn.think.in.java.entity.LogEntry;

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
        LogEntry logEntry = LogEntry.newBuilder().term(1).command(Command.newBuilder().key("hello").value("value1").build()).build();
        machine.apply(logEntry);
    }


    @Test
    public void applyRead() throws RocksDBException {

        System.out.println(machine.get("hello:7"));
    }
}
