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
package cn.think.in.java.raft.common.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 *
 * 附加日志 RPC 参数. handlerAppendEntries
 *
 * @author 莫那·鲁道
 */
@Getter
@Setter
@ToString
@Builder
public class AentryParam implements Serializable {

    /** 候选人的任期号  */
    private long term;

    /** 被请求者 ID(ip:selfPort) */
    private String serverId;

    /** 领导人的 Id，以便于跟随者重定向请求 */
    private String leaderId;

    /**新的日志条目紧随之前的索引值  */
    private long prevLogIndex;

    /** prevLogIndex 条目的任期号  */
    private long preLogTerm;

    /** 准备存储的日志条目（表示心跳时为空；一次性发送多个是为了提高效率） */
    private LogEntry[] entries;

    /** 领导人已经提交的日志的索引值  */
    private long leaderCommit;

}
