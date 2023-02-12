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
package cn.think.in.java.impl;

import cn.think.in.java.Consensus;
import cn.think.in.java.common.NodeStatus;
import cn.think.in.java.common.Peer;
import cn.think.in.java.entity.*;
import io.netty.util.internal.StringUtil;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * 默认的一致性模块实现.
 *
 * @author 莫那·鲁道
 */
@Setter
@Getter
public class DefaultConsensus implements Consensus {


    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultConsensus.class);


    public final DefaultNode node;

    public final ReentrantLock voteLock = new ReentrantLock();
    public final ReentrantLock appendLock = new ReentrantLock();

    public DefaultConsensus(DefaultNode node) {
        this.node = node;
    }

    /**
     * 处理投票请求--RPC
     *
     * 接收者实现：
     *      如果term < currentTerm返回 false （5.2 节）
     *      如果 votedFor 为空或者就是 candidateId，并且候选人的日志至少和自己一样新，那么就投票给他（5.2 节，5.4 节）
     */
    @Override
    public RvoteResult requestVote(RvoteParam param) {
        try {
            RvoteResult.Builder builder = RvoteResult.newBuilder();
//            if (!voteLock.tryLock()) {
//                return builder.term(node.getCurrentTerm()).voteGranted(false).build();
//            }
            voteLock.lock();

            // 对方任期没有自己新
            if (param.getTerm() < node.getCurrentTerm()) {
                // 返回投票结果的同时更新对方的term
                LOGGER.info("node {} decline to vote for candidate {} because of smaller term", node.peerSet.getSelf().getAddr(), param.getCandidateId());
                return builder.term(node.getCurrentTerm()).voteGranted(false).build();
            }

            // (当前节点并没有投票 或者 已经投票过了且是对方节点) && 对方日志和自己一样新
//            LOGGER.info("node {} current vote for [{}], param candidateId : {}", node.peerSet.getSelf(), node.getVotedFor(), param.getCandidateId());
//            LOGGER.info("node {} current term {}, peer term : {}", node.peerSet.getSelf(), node.getCurrentTerm(), param.getTerm());

            if ((StringUtil.isNullOrEmpty(node.getVotedFor()) || node.getVotedFor().equals(param.getCandidateId()))) {

                if (node.getLogModule().getLast() != null) {
                    // 候选者的日志不是最新的，拒绝投票
                    if (node.getLogModule().getLast().getTerm() > param.getLastLogTerm() ||
                            node.getLogModule().getLastIndex() > param.getLastLogIndex()) {
                        LOGGER.info("node {} decline to vote for candidate {} because of older logs", node.peerSet.getSelf().getAddr(), param.getCandidateId());
                        return RvoteResult.fail();
                    }
                }

                // 切换状态
                node.status = NodeStatus.FOLLOWER;
                // 更新
                node.peerSet.setLeader(new Peer(param.getCandidateId()));
                node.setCurrentTerm(param.getTerm());
                node.setVotedFor(param.getCandidateId());
                node.preElectionTime = System.currentTimeMillis();
                LOGGER.info("node {} vote for candidate: {}", node.peerSet.getSelf(), param.getCandidateId());
                // 返回成功
                return builder.term(node.currentTerm).voteGranted(true).build();
            }

            LOGGER.info("node {} decline to vote for candidate {} because there is no vote available", node.peerSet.getSelf().getAddr(), param.getCandidateId());
            return builder.term(node.currentTerm).voteGranted(false).build();

        } finally {
            voteLock.unlock();
        }
    }


    /**
     * 附加日志(多个日志,为了提高效率) RPC
     *
     * 接收者实现：
     *    如果 term < currentTerm 就返回 false （5.1 节）
     *    如果日志在 prevLogIndex 位置处的日志条目的任期号和 prevLogTerm 不匹配，则返回 false （5.3 节）
     *    如果已经存在的日志条目和新的产生冲突（索引值相同但是任期号不同），删除这一条和之后所有的 （5.3 节）
     *    附加任何在已有的日志中不存在的条目
     *    如果 leaderCommit > commitIndex，令 commitIndex 等于 leaderCommit 和 新日志条目索引值中较小的一个
     */
    @Override
    public AentryResult appendEntries(AentryParam param) {
        AentryResult result = AentryResult.fail();
        try {
            appendLock.lock();
            result.setTerm(node.getCurrentTerm());

            // 请求方任期较低，直接拒绝
            if (param.getTerm() < node.getCurrentTerm()) {
                return result;
            }

            node.preHeartBeatTime = System.currentTimeMillis();
            node.preElectionTime = System.currentTimeMillis();
            node.peerSet.setLeader(new Peer(param.getLeaderId()));

            // 够格
            if (node.status != NodeStatus.FOLLOWER) {
                LOGGER.info("node {} become FOLLOWER, currentTerm : {}, param Term : {}",
                    node.peerSet.getSelf(), node.currentTerm, param.getTerm());
                // 收到了新领导者的append entry请求，转为跟随者
                node.status = NodeStatus.FOLLOWER;
            }
            // 更新term
            node.setCurrentTerm(param.getTerm());

            //心跳
            if (param.getEntries() == null || param.getEntries().length == 0) {
                LOGGER.info("node {} append heartbeat success , he's term : {}, my term : {}",
                    param.getLeaderId(), param.getTerm(), node.getCurrentTerm());
                // 旧日志提交
                long nextCommit = node.getCommitIndex() + 1;
                while (nextCommit <= param.getLeaderCommit()
                        && node.logModule.read(nextCommit) != null){
                    node.stateMachine.apply(node.getLogModule().read(nextCommit));
                    nextCommit++;
                }
                node.setCommitIndex(nextCommit - 1);
                return AentryResult.newBuilder().term(node.getCurrentTerm()).success(true).build();
            }

            // TODO no-op空日志

            // 1. preLog匹配判断
            if (node.getLogModule().getLastIndex() != param.getPrevLogIndex()){
                // index不匹配
                return result;
            } else if (param.getPrevLogIndex() >= 0) {
                // index匹配且前一个日志存在，比较任期号
                LogEntry preEntry = node.getLogModule().read(param.getPrevLogIndex());
                if (preEntry.getTerm() != param.getPreLogTerm()) {
                    // 任期号不匹配
                    return result;
                }
            } // else ... 前一个日志不存在时，无需查看任期号

            // 2. 清理多余的旧日志
            long curIdx = param.getPrevLogIndex() + 1;
            if (node.getLogModule().read(curIdx) != null){
                node.getLogModule().removeOnStartIndex(curIdx);
            }

            // 3. 追加日志到本地文件
            LogEntry[] entries = param.getEntries();
            for (LogEntry logEntry : entries) {
                node.getLogModule().write(logEntry);
            }

            // 4. 旧日志提交
            long nextCommit = node.getCommitIndex() + 1;
            while (nextCommit <= param.getLeaderCommit()){
                node.stateMachine.apply(node.getLogModule().read(nextCommit));
                nextCommit++;
            }
            node.setCommitIndex(nextCommit - 1);

            // 5. 同意append entry请求
            result.setSuccess(true);
            return result;

        } finally {
            appendLock.unlock();
        }
    }


}
