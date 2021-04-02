package cn.think.in.java.impl;

import java.io.File;

import com.alibaba.fastjson.JSON;

import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.think.in.java.StateMachine;
import cn.think.in.java.entity.Command;
import cn.think.in.java.entity.LogEntry;

/**
 *
 * 默认的状态机实现.
 *
 * @author 莫那·鲁道
 */
public class DefaultStateMachine implements StateMachine {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultStateMachine.class);

    /** public just for test */
    public static String dbDir;
    public static String stateMachineDir;

    public static RocksDB machineDb;

    static {
        if (dbDir == null) {
            dbDir = "./rocksDB-raft/" + System.getProperty("serverPort");
        }
        if (stateMachineDir == null) {
            stateMachineDir =dbDir + "/stateMachine";
        }
        RocksDB.loadLibrary();
    }


    private DefaultStateMachine() {
        try {
            File file = new File(stateMachineDir);
            boolean success = false;
            if (!file.exists()) {
                success = file.mkdirs();
            }
            if (success) {
                LOGGER.warn("make a new dir : " + stateMachineDir);
            }
            Options options = new Options();
            options.setCreateIfMissing(true);
            machineDb = RocksDB.open(options, stateMachineDir);

        } catch (RocksDBException e) {
            LOGGER.info(e.getMessage());
        }
    }

    public static DefaultStateMachine getInstance() {
        return DefaultStateMachineLazyHolder.INSTANCE;
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
            LOGGER.info(e.getMessage());
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
            LOGGER.info(e.getMessage());
        }
        return "";
    }

    @Override
    public void setString(String key, String value) {
        try {
            machineDb.put(key.getBytes(), value.getBytes());
        } catch (RocksDBException e) {
            LOGGER.info(e.getMessage());
        }
    }

    @Override
    public void delString(String... key) {
        try {
            for (String s : key) {
                machineDb.delete(s.getBytes());
            }
        } catch (RocksDBException e) {
            LOGGER.info(e.getMessage());
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
            LOGGER.info(e.getMessage());
        }
    }

}
