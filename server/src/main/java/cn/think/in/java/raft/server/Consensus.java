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
package cn.think.in.java.raft.server;


import cn.think.in.java.raft.common.entity.AentryParam;
import cn.think.in.java.raft.common.entity.AentryResult;
import cn.think.in.java.raft.common.entity.RvoteParam;
import cn.think.in.java.raft.common.entity.RvoteResult;

/**
 *
 * Raft 一致性模块.
 * @author 莫那·鲁道
 */
public interface Consensus {

    /**
     * 请求投票 RPC
     *
     * 接收者实现：
     *
     *      如果term < currentTerm返回 false （5.2 节）
     *      如果 votedFor 为空或者就是 candidateId，并且候选人的日志至少和自己一样新，那么就投票给他（5.2 节，5.4 节）
     * @return
     */
    RvoteResult requestVote(RvoteParam param);

    /**
     * 附加日志(多个日志,为了提高效率) RPC
     *
     * 接收者实现：
     *
     *    如果 term < currentTerm 就返回 false （5.1 节）
     *    如果日志在 prevLogIndex 位置处的日志条目的任期号和 prevLogTerm 不匹配，则返回 false （5.3 节）
     *    如果已经存在的日志条目和新的产生冲突（索引值相同但是任期号不同），删除这一条和之后所有的 （5.3 节）
     *    附加任何在已有的日志中不存在的条目
     *    如果 leaderCommit > commitIndex，令 commitIndex 等于 leaderCommit 和 新日志条目索引值中较小的一个
     * @return
     */
    AentryResult appendEntries(AentryParam param);


}
