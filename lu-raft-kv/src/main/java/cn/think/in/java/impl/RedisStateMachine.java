package cn.think.in.java.impl;

import cn.think.in.java.StateMachine;
import cn.think.in.java.entity.LogEntry;
import cn.think.in.java.impl.DefaultStateMachine.DefaultStateMachineLazyHolder;

/**
 *
 * redis实现状态机存储
 *
 * @author rensailong
 */
public class RedisStateMachine implements StateMachine {


    public RedisStateMachine() {

    }

    public static RedisStateMachine getInstance() {
        return RedisStateMachine.RedisStateMachineLazyHolder.INSTANCE;
    }

    private static class RedisStateMachineLazyHolder {

        private static final RedisStateMachine INSTANCE = new RedisStateMachine();
    }

    @Override
    public void apply(LogEntry logEntry) {

    }

    @Override
    public LogEntry get(String key) {
        return null;
    }

    @Override
    public String getString(String key) {
        return null;
    }

    @Override
    public void setString(String key, String value) {

    }

    @Override
    public void delString(String... key) {

    }
}
