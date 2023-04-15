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
package cn.think.in.java.raft.server.constant;

import cn.think.in.java.raft.server.StateMachine;
import cn.think.in.java.raft.server.impl.DefaultStateMachine;
import cn.think.in.java.raft.server.impl.RedisStateMachine;
import lombok.Getter;

/**
 *
 * 快照存储类型
 *
 * @author rensailong
 */
@Getter
public enum StateMachineSaveType {
    /** sy */
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

    public static StateMachineSaveType getForType(String typeName) {
        for (StateMachineSaveType value : values()) {
            if (value.getTypeName().equals(typeName)) {
                return value;
            }
        }

        return null;
    }

}
