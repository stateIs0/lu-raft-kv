package cn.think.in.java.impl;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import cn.think.in.java.entity.Command;
import cn.think.in.java.entity.LogEntry;


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
        LogEntry entry = LogEntry.newBuilder().
                term(1).
                command(Command.newBuilder().key("hello").value("world").build()).
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
