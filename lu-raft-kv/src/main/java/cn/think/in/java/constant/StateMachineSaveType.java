package cn.think.in.java.constant;

import cn.think.in.java.StateMachine;
import cn.think.in.java.impl.DefaultStateMachine;
import cn.think.in.java.impl.RedisStateMachine;

/**
 *
 * 快照存储类型
 *
 * @author rensailong
 */
public enum StateMachineSaveType {
    REDIS("redis", "redis存储", RedisStateMachine.getInstance()),
    ROCKS_DB("RocksDB", "RocksDB本地存储", DefaultStateMachine.getInstance())
    ;

    public StateMachine getStateMachine() {
        return this.stateMachine;
    }

    private String typeName;

    private String desc;

    private StateMachine stateMachine;

    StateMachineSaveType(String typeName, String desc, StateMachine stateMachine) {
        this.typeName = typeName;
        this.desc = desc;
        this.stateMachine = stateMachine;
    }

}
