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
import cn.think.in.java.LogModule;
import cn.think.in.java.Node;
import cn.think.in.java.StateMachine;
import cn.think.in.java.common.NodeConfig;
import cn.think.in.java.common.NodeStatus;
import cn.think.in.java.common.Peer;
import cn.think.in.java.common.PeerSet;
import cn.think.in.java.current.RaftThreadPool;
import cn.think.in.java.entity.*;
import cn.think.in.java.exception.RaftRemotingException;
import cn.think.in.java.membership.changes.ClusterMembershipChanges;
import cn.think.in.java.membership.changes.Result;
import cn.think.in.java.rpc.*;
import cn.think.in.java.util.LongConvert;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import raft.client.ClientKVAck;
import raft.client.ClientKVReq;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static cn.think.in.java.common.NodeStatus.LEADER;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * 抽象机器节点, 初始为 follower, 角色随时变化.
 *
 * @author 莫那·鲁道
 */
@Getter
@Setter
@Slf4j
public class DefaultNode implements Node, ClusterMembershipChanges {

    /**
     * 选举超时时间
     * 如果一个跟随者在该时间内没有收到来自领导者/候选者的rpc请求，则参与竞选
     * 单位: ms
     */
    public volatile long electionTimeout = 1000 * 3;
    /**
     * 上一次选举时间；收到领导者rpc调用时该值会更新
     */
    public volatile long preElectionTime = System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(10) * 100;

    /** 上次一心跳时间戳 */
    public volatile long preHeartBeatTime = 0;
    /** 心跳间隔基数 */
    public final long heartBeatTick = 300;

    /** 一致性信号 */
    public final Integer consistencySignal = 1;

    /** 发送心跳信号 */
    private HeartBeatTask heartBeatTask = new HeartBeatTask();

    private ElectionTask electionTask = new ElectionTask();

    private ReplicationFailQueueConsumer replicationFailQueueConsumer = new ReplicationFailQueueConsumer();

    private LinkedBlockingQueue<ReplicationFailModel> replicationFailQueue = new LinkedBlockingQueue<>(2048);


    /**
     * 节点当前状态
     *
     * @see NodeStatus
     */
    public volatile int status = NodeStatus.FOLLOWER;

    public PeerSet peerSet;

    volatile boolean running = false;

    /* ============ 所有服务器上持久存在的 ============= */

    /** 日志条目集；每一个条目包含一个用户状态机执行的指令，和收到时的任期号 */
    LogModule logModule;
    /** 服务器最后一次知道的任期号（初始化为 0，持续递增） */
    volatile long currentTerm;
    /** 在当前获得选票的候选人的 Id */
    volatile String votedFor;

    /* ========== 在领导人里经常改变的(选举后重新初始化) ================== */

    /** 对于每一个服务器，需要发送给他的下一个日志条目的索引值（初始化为领导人最后索引值加一） */
    Map<Peer, Long> nextIndexs;

    /* ============================== */

    public NodeConfig config;

    public RpcService rpcServer;

    private RpcClient rpcClient = new DefaultRpcClient();

    public StateMachine stateMachine;

    /* ============================== */

    /** 一致性模块实现 */
    Consensus consensus;

    ClusterMembershipChanges delegate;

    /* ============================== */

    private DefaultNode() {
    }

    public static DefaultNode getInstance() {
        return DefaultNodeLazyHolder.INSTANCE;
    }


    private static class DefaultNodeLazyHolder {
        private static final DefaultNode INSTANCE = new DefaultNode();
    }

    @Override
    public void init() throws Throwable {
        running = true;
        rpcServer.init();
        rpcClient.init();

        consensus = new DefaultConsensus(this);
        delegate = new ClusterMembershipChangesImpl(this);

        LogEntry lastEntry = logModule.getLast();
        if (lastEntry != null)
            currentTerm = lastEntry.getTerm();

        /**
         * 创建重复延迟任务；任务完成后重新投入队列
         */
        RaftThreadPool.scheduleWithFixedDelay(heartBeatTask, heartBeatTick);
        /**
         * 创建重复定期任务；任务在固定的时间点执行（不会并发）
         */
        RaftThreadPool.scheduleAtFixedRate(electionTask, 6000, 100);
        /**
         * 创建常规任务，只执行一次
         */
        RaftThreadPool.execute(replicationFailQueueConsumer);

        LogEntry logEntry = logModule.getLast();
        if (logEntry != null) {
            currentTerm = logEntry.getTerm();
        }

        log.info("start success, selfId : {} ", peerSet.getSelf().getAddr());
    }

    @Override
    public void setConfig(NodeConfig config) {
        this.config = config;
        stateMachine = config.getStateMachineSaveType().getStateMachine();
        logModule = DefaultLogModule.getInstance();

        peerSet = PeerSet.getInstance();
        for (String s : config.getPeerAddrs()) {
            Peer peer = new Peer(s);
            peerSet.addPeer(peer);
            if (s.equals("localhost:" + config.getSelfPort())) {
                peerSet.setSelf(peer);
            }
        }

        rpcServer = new DefaultRpcServiceImpl(config.selfPort, this);
    }


    public void setCommitIndex(long index){
        stateMachine.setCommit(index);
    }

    public long getCommitIndex(){
        return stateMachine.getCommit();
    }

    @Override
    public RvoteResult handlerRequestVote(RvoteParam param) {
        log.info("vote process for {}, its term {} ", param.getCandidateId(), param.getTerm());
        return consensus.requestVote(param);
    }

    @Override
    public AentryResult handlerAppendEntries(AentryParam param) {
        if (param.getEntries() != null) {
            log.info("node receive node {} append entry, entry content = {}", param.getLeaderId(), param.getEntries());
        }
        return consensus.appendEntries(param);
    }

    /**
     * 重定向
     * @param request 客户端请求
     * @return 请求结果
     */
    @Override
    public ClientKVAck redirect(ClientKVReq request) {
        Request r = Request.builder()
                .obj(request)
                .url(peerSet.getLeader().getAddr())
                .cmd(Request.CLIENT_REQ).build();
        return rpcClient.send(r);
    }

    /**
     * 客户端的每一个请求都包含一条被复制状态机执行的指令。
     * 领导人把这条指令作为一条新的日志条目附加到日志中去，然后并行的发起附加条目 RPCs 给其他的服务器，让他们复制这条日志条目。
     * 当这条日志条目被安全的复制（下面会介绍），领导人会应用这条日志条目到它的状态机中然后把执行的结果返回给客户端。
     * 如果跟随者崩溃或者运行缓慢，再或者网络丢包，
     * 领导人会不断的重复尝试附加日志条目 RPCs （尽管已经回复了客户端）直到所有的跟随者都最终存储了所有的日志条目。
     *
     * @param request
     * @return
     */
    @Override
    public synchronized ClientKVAck handlerClientRequest(ClientKVReq request) {

        log.info("handlerClientRequest handler {} operation, key: [{}], value: [{}]",
                ClientKVReq.Type.value(request.getType()), request.getKey(), request.getValue());

        if (status != LEADER) {
            log.warn("I not am leader, only invoke redirect method, leader addr : {}, my addr : {}",
                    peerSet.getLeader(), peerSet.getSelf().getAddr());
            return redirect(request);
        }

        // 读操作
        if (request.getType() == ClientKVReq.GET) {
            synchronized (consistencySignal){
                try {
                    // 等待一个心跳周期，以保证当前领导者有效
                    consistencySignal.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return ClientKVAck.fail();
                }
                String value = stateMachine.getString(request.getKey());
                if (value != null) {
                    return new ClientKVAck(value);
                }
                return new ClientKVAck(null);
            }
        }

        // 幂等性判断
        if (stateMachine.getString(request.getRequestId()) != null){
            log.warn("request have been ack");
            return ClientKVAck.ok();
        }

        // 写操作
        LogEntry logEntry = LogEntry.builder()
                .command(Command.builder().
                        key(request.getKey()).
                        value(request.getValue()).
                        build())
                .term(currentTerm)
                .requestId(request.getRequestId())
                .build();

        // 写入本地日志并更新logEntry的index
        logModule.write(logEntry);
        log.info("write logModule success, logEntry info : {}, log index : {}", logEntry, logEntry.getIndex());

        final AtomicInteger success = new AtomicInteger(0);

        List<Future<Boolean>> futureList = new ArrayList<>();

        //  复制到其他机器
        for (Peer peer : peerSet.getPeersWithOutSelf()) {
            // TODO check self and RaftThreadPool
            // 并行发起 RPC 复制并获取响应
            futureList.add(replication(peer, logEntry));
        }
        int count = futureList.size();

        CountDownLatch latch = new CountDownLatch(futureList.size());
        List<Boolean> resultList = new CopyOnWriteArrayList<>();

        getRPCAppendResult(futureList, latch, resultList);

        try {
            // 等待getRPCAppendResult中的线程执行完毕
            latch.await(10000, MILLISECONDS);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }

        for (Boolean aBoolean : resultList) {
            if (aBoolean) {
                success.incrementAndGet();
            }
        }

        //  响应客户端(成功一半及以上)
        if (success.get() * 2 >= count) {
            // 更新
            setCommitIndex(logEntry.getIndex());
            //  应用到状态机
            getStateMachine().apply(logEntry);
            log.info("successfully commit, logEntry info: {}", logEntry);
            // TODO 记录已提交日志对应的request id
            // 返回成功.
            return ClientKVAck.ok();
        } else {
            // 提交失败，删除日志
            logModule.removeOnStartIndex(logEntry.getIndex());
            log.warn("commit fail, logEntry info : {}", logEntry);
            // TODO 不应用到状态机,但已经记录到日志中.由定时任务从重试队列取出,然后重复尝试,当达到条件时,应用到状态机.
            // 这里应该返回错误, 因为没有成功复制过半机器.
            return ClientKVAck.fail();
        }
    }

    /**
     * 开启多个线程读取futureList，将结果写入支持并发的resultList
     * @param futureList
     * @param latch
     * @param resultList
     */
    private void getRPCAppendResult(List<Future<Boolean>> futureList, CountDownLatch latch, List<Boolean> resultList) {
        for (Future<Boolean> future : futureList) {
            RaftThreadPool.execute(() -> {
                try {
                    resultList.add(future.get(3000, MILLISECONDS));
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    resultList.add(false);
                } finally {
                    latch.countDown();
                }
            });
        }
    }


    /**
     * 复制日志到跟随者节点
     * @param peer 跟随者节点
     * @param entry 日志条目
     * @return 线程执行结果
     */
    public Future<Boolean> replication(Peer peer, LogEntry entry) {

        return RaftThreadPool.submit(() -> {

            long start = System.currentTimeMillis(), end = start;

            // 5秒重试时间
            while (end - start < 5 * 1000L) {

                // 1. 封装append entry请求基本参数
                AentryParam aentryParam = AentryParam.builder().build();
                aentryParam.setTerm(currentTerm);
                aentryParam.setServerId(peer.getAddr());
                aentryParam.setLeaderId(peerSet.getSelf().getAddr());
                aentryParam.setLeaderCommit(getCommitIndex());

                // 2. 生成日志数组
                Long nextIndex = nextIndexs.get(peer);
                LinkedList<LogEntry> logEntries = new LinkedList<>();
                if (entry.getIndex() >= nextIndex) {
                    // 把 nextIndex ~ entry.index 之间的日志都加入list
                    for (long i = nextIndex; i <= entry.getIndex(); i++) {
                        LogEntry l = logModule.read(i);
                        if (l != null) {
                            logEntries.add(l);
                        }
                    }
                } else {
                    logEntries.add(entry);
                }
                aentryParam.setEntries(logEntries.toArray(new LogEntry[0]));

                // 3. 设置preLog相关参数，用于日志匹配
                LogEntry preLog = getPreLog(logEntries.getFirst());
                // preLog不存在时，下述参数会被设为-1
                aentryParam.setPreLogTerm(preLog.getTerm());
                aentryParam.setPrevLogIndex(preLog.getIndex());

                // 4. 封装RPC请求
                Request request = Request.builder()
                        .cmd(Request.A_ENTRIES)
                        .obj(aentryParam)
                        .url(peer.getAddr())
                        .build();

                try {
                    // 5. 发送RPC请求；同步调用，阻塞直到获取返回值
                    AentryResult result = getRpcClient().<AentryResult>send(request);
                    if (result == null) {
                        // timeout
                        return false;
                    }
                    if (result.isSuccess()) {
                        log.info("append follower entry success, follower=[{}], entry=[{}]", peer.getAddr(), aentryParam.getEntries());
                        // update 这两个追踪值
                        nextIndexs.put(peer, entry.getIndex() + 1);
                        return true;
                    } else  {
                        // 对方比我大
                        if (result.getTerm() > currentTerm) {
                            log.warn("follower [{}] term [{}], my term = [{}], so I will become follower",
                                    peer.getAddr(), result.getTerm(), currentTerm);
                            currentTerm = result.getTerm();
                            status = NodeStatus.FOLLOWER;
                            return false;
                        }
                        else {
                            // 任期相同却失败了，说明preLog匹配不上，nextIndex递减
                            nextIndexs.put(peer, Math.max(nextIndex - 1, 0));
                            log.warn("follower {} nextIndex not match, will reduce nextIndex and retry append, nextIndex : [{}]", peer.getAddr(),
                                    nextIndex);
                            // 重来, 直到成功.
                        }
                    }

                    end = System.currentTimeMillis();

                } catch (Exception e) {
                    log.warn(e.getMessage(), e);
                    // TODO 到底要不要放队列重试?
//                        ReplicationFailModel model =  ReplicationFailModel.newBuilder()
//                            .callable(this)
//                            .logEntry(entry)
//                            .peer(peer)
//                            .offerTime(System.currentTimeMillis())
//                            .build();
//                        replicationFailQueue.offer(model);
                    return false;
                }
            }
            // 超时了,没办法了
            return false;
        });

    }

    /**
     * 获取logEntry的前一个日志
     * 没有前一个日志时返回一个index和term为0的空日志
     * @param logEntry
     * @return
     */
    private LogEntry getPreLog(LogEntry logEntry) {
        LogEntry entry = logModule.read(logEntry.getIndex() - 1);

        if (entry == null) {
            log.warn("perLog is null, parameter logEntry : {}", logEntry);
            entry = LogEntry.builder().index(-1L).term(-1).command(null).build();
        }
        return entry;
    }


    class ReplicationFailQueueConsumer implements Runnable {

        /** 一分钟 */
        long intervalTime = 1000 * 60;

        @Override
        public void run() {
            while (running) {
                try {
                    ReplicationFailModel model = replicationFailQueue.poll(1000, MILLISECONDS);
                    if (model == null) {
                        continue;
                    }
                    if (status != LEADER) {
                        // 应该清空?
                        replicationFailQueue.clear();
                        continue;
                    }
                    log.warn("replication Fail Queue Consumer take a task, will be retry replication, content detail : [{}]", model.logEntry);
                    long offerTime = model.offerTime;
                    if (System.currentTimeMillis() - offerTime > intervalTime) {
                        log.warn("replication Fail event Queue maybe full or handler slow");
                    }

                    Callable callable = model.callable;
                    Future<Boolean> future = RaftThreadPool.submit(callable);
                    Boolean r = future.get(3000, MILLISECONDS);
                    // 重试成功.
                    if (r) {
                        // 可能有资格应用到状态机.
                        tryApplyStateMachine(model);
                    }

                } catch (InterruptedException e) {
                    // ignore
                } catch (ExecutionException | TimeoutException e) {
                    log.warn(e.getMessage());
                }
            }
        }
    }

    private void tryApplyStateMachine(ReplicationFailModel model) {

        String success = stateMachine.getString(model.successKey);
        stateMachine.setString(model.successKey, String.valueOf(Integer.parseInt(success) + 1));

        String count = stateMachine.getString(model.countKey);

        if (Integer.parseInt(success) >= Integer.parseInt(count) / 2) {
            stateMachine.apply(model.logEntry);
            stateMachine.delString(model.countKey, model.successKey);
        }
    }


    @Override
    public void destroy() throws Throwable {
        rpcServer.destroy();
        stateMachine.destroy();
        rpcClient.destroy();
        running = false;
        log.info("destroy success");
    }


    /**
     * 1. 在转变成候选人后就立即开始选举过程
     * 自增当前的任期号（currentTerm）
     * 给自己投票
     * 重置选举超时计时器
     * 发送请求投票的 RPC 给其他所有服务器
     * 2. 如果接收到大多数服务器的选票，那么就变成领导人
     * 3. 如果接收到来自新的领导人的附加日志 RPC，转变成跟随者
     * 4. 如果选举过程超时，再次发起一轮选举
     */
    class ElectionTask implements Runnable {

        @Override
        public void run() {

            if (status == LEADER) {
                return;
            }

            long current = System.currentTimeMillis();
            // 基于 RAFT 的随机时间,解决冲突.
            if (current - preElectionTime <
                    electionTimeout + ThreadLocalRandom.current().nextInt(10) * 100) {
                return;
            }

            status = NodeStatus.CANDIDATE;
            currentTerm = currentTerm + 1;
            // 推荐自己.
            votedFor = peerSet.getSelf().getAddr();
            log.info("node {} become CANDIDATE and start election, its term : [{}], LastEntry : [{}]",
                    peerSet.getSelf().getAddr(), currentTerm, logModule.getLast());


            List<Peer> peers = peerSet.getPeersWithOutSelf();

            ArrayList<Future<RvoteResult>> futureArrayList = new ArrayList<>();

            //log.info("peerList size : {}, peer list content : {}", peers.size(), peers);

            // 发送请求
            for (Peer peer : peers) {
                // 执行rpc调用并加入list；添加的是submit的返回值
                futureArrayList.add(RaftThreadPool.submit(() -> {
                    long lastTerm = 0L;
                    LogEntry last = logModule.getLast();
                    if (last != null) {
                        lastTerm = last.getTerm();
                    }

                    RvoteParam param = RvoteParam.builder().
                            term(currentTerm).
                            candidateId(peerSet.getSelf().getAddr()).
                            lastLogIndex(LongConvert.convert(logModule.getLastIndex())).
                            lastLogTerm(lastTerm).
                            build();

                    Request request = Request.builder()
                            .cmd(Request.R_VOTE)
                            .obj(param)
                            .url(peer.getAddr())
                            .build();

                    try {
                        return rpcClient.<RvoteResult>send(request);
                    } catch (RaftRemotingException e) {
                        log.error("ElectionTask RPC Fail , URL : " + request.getUrl());
                        return null;
                    }
                }));
            }
            log.info("futureArrayList.size() : {}", futureArrayList.size());

            /** 统计赞同票的数量 */
            AtomicInteger success2 = new AtomicInteger(0);

            /** 计数器 */
            CountDownLatch latch = new CountDownLatch(futureArrayList.size());

            // 获取结果.
            for (Future<RvoteResult> future : futureArrayList) {
                RaftThreadPool.submit(() -> {
                    try {
                        RvoteResult result = future.get(1000, MILLISECONDS);
                        if (result == null) {
                            // rpc调用失败或任务超时
                            return -1;
                        }
                        boolean isVoteGranted = result.isVoteGranted();

                        if (isVoteGranted) {
                            success2.incrementAndGet();
                        } else {
                            // 更新自己的任期.
                            long resTerm =result.getTerm();
                            if (resTerm >= currentTerm) {
                                currentTerm = resTerm;
                            }
                        }
                        return 0;
                    } catch (Exception e) {
                        log.error("future.get exception");
                        return -1;
                    } finally {
                        latch.countDown();
                    }
                });
            }

            try {
                // 等待子线程完成选票统计
                latch.await(3500, MILLISECONDS);
            } catch (InterruptedException e) {
                log.warn("InterruptedException By Master election Task");
            }

            // 总票数
            int success = success2.get();

            //log.info("node {} maybe become leader , success count = {} , status : {}", peerSet.getSelf(), success, NodeStatus.Enum.value(status));

            // 如果投票期间,有其他服务器发送 appendEntry , 就可能变成 follower ,这时,应该停止.
            if (status == NodeStatus.FOLLOWER) {
                log.info("node {} election stops with new appendEntry", peerSet.getSelf().getAddr());
                preElectionTime = System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(10) * 100;
                return;
            }

            // 需要获得超过半数节点的投票
            if (success * 2 >= peers.size()) {
                log.warn("node {} become leader with {} votes ", peerSet.getSelf().getAddr(), success);
                peerSet.setLeader(peerSet.getSelf());
                votedFor = "";
                if (leaderInit())
                    status = LEADER;
            }

            // 未赢得过半的投票，或提交no-op空日志失败
            if (status != LEADER){
                // 重新选举
                log.info("node {} election fail, votes count = {} ", peerSet.getSelf().getAddr(), success);
                votedFor = "";
                status = NodeStatus.FOLLOWER;
            }
            // 更新时间
            preElectionTime = System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(10) * 100;
        }
    }

    /**
     * 1. 初始化所有的 nextIndex 值为自己的最后一条日志的 index + 1
     * 2. 发送并提交no-op空日志，以提交旧领导者未提交的日志
     * 3. apply no-op之前的日志
     */
    private boolean leaderInit() {
        nextIndexs = new ConcurrentHashMap<>();
        for (Peer peer : peerSet.getPeersWithOutSelf()) {
            nextIndexs.put(peer, logModule.getLastIndex() + 1);
        }

        // no-op 空日志
        LogEntry logEntry = LogEntry.builder()
                .command(null)
                .term(currentTerm)
                .build();

        // 写入本地日志并更新logEntry的index
        logModule.write(logEntry);
        log.info("write no-op log success, log index: {}", logEntry.getIndex());

        final AtomicInteger success = new AtomicInteger(0);

        List<Future<Boolean>> futureList = new ArrayList<>();

        //  复制到其他机器
        for (Peer peer : peerSet.getPeersWithOutSelf()) {
            // 并行发起 RPC 复制并获取响应
            futureList.add(replication(peer, logEntry));
        }
        int count = futureList.size();

        CountDownLatch latch = new CountDownLatch(futureList.size());
        List<Boolean> resultList = new CopyOnWriteArrayList<>();

        getRPCAppendResult(futureList, latch, resultList);

        try {
            // 等待getRPCAppendResult中的线程执行完毕
            latch.await(10000, MILLISECONDS);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }

        for (Boolean aBoolean : resultList) {
            if (aBoolean) {
                success.incrementAndGet();
            }
        }

        //  响应客户端(成功一半及以上)
        if (success.get() * 2 >= count) {
            // 提交旧日志并更新commit index
            long nextCommit = getCommitIndex() + 1;
            while (nextCommit < logEntry.getIndex() && logModule.read(nextCommit) != null){
                stateMachine.apply(getLogModule().read(nextCommit));
                nextCommit++;
            }
            setCommitIndex(logEntry.getIndex());
            log.info("successfully commit, logEntry info: {}", logEntry);
            // 返回成功
            return true;
        } else {
            // 提交失败，删除日志
            logModule.removeOnStartIndex(logEntry.getIndex());
            log.warn("no-op commit fail, election again");
            return false;
        }
    }

    /**
     * 发送心跳信号，通过线程池执行
     * 如果收到了来自任期更大的节点的响应，则转为跟随者
     * RPC请求类型为A_ENTRIES
     */
    class HeartBeatTask implements Runnable {

        @Override
        public void run() {

            if (status != LEADER) {
                return;
            }

            long current = System.currentTimeMillis();
            if (current - preHeartBeatTime < heartBeatTick) {
                return;
            }

            preHeartBeatTime = System.currentTimeMillis();
            AentryParam param = AentryParam.builder()
                    .entries(null)// 心跳,空日志.
                    .leaderId(peerSet.getSelf().getAddr())
                    .term(currentTerm)
                    .leaderCommit(getCommitIndex())
                    .build();
            List<Future<Boolean>> futureList = new ArrayList<>();

            for (Peer peer : peerSet.getPeersWithOutSelf()) {
                Request request = new Request(
                        Request.A_ENTRIES,
                        param,
                        peer.getAddr());

                // 并行发起 RPC 复制并获取响应
                futureList.add(RaftThreadPool.submit(() -> {
                    try {
                        AentryResult aentryResult = rpcClient.send(request);
                        long term = aentryResult.getTerm();

                        if (term > currentTerm) {
                            log.warn("follow new leader {}", peer.getAddr());
                            currentTerm = term;
                            votedFor = "";
                            status = NodeStatus.FOLLOWER;
                        }
                        return aentryResult.isSuccess();
                    } catch (Exception e) {
                        log.error("HeartBeatTask RPC Fail, request URL : {} ", request.getUrl());
                        return false;
                    }
                }));
            }
            int count = futureList.size();
            int success = 0;

            CountDownLatch latch = new CountDownLatch(futureList.size());
            List<Boolean> resultList = new CopyOnWriteArrayList<>();

            getRPCAppendResult(futureList, latch, resultList);

            try {
                // 等待getRPCAppendResult中的线程执行完毕
                latch.await(3000, MILLISECONDS);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }

            for (Boolean aBoolean : resultList) {
                if (aBoolean) {
                    success++;
                }
            }

            //  心跳响应成功，通知阻塞的线程
            if (success * 2 >= count) {
                synchronized (consistencySignal){
                    consistencySignal.notify();
                }
            }
        }
    }

    @Override
    public Result addPeer(Peer newPeer) {
        return delegate.addPeer(newPeer);
    }

    @Override
    public Result removePeer(Peer oldPeer) {
        return delegate.removePeer(oldPeer);
    }

}
