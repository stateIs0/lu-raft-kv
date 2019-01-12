package cn.think.in.java;

import cn.think.in.java.entity.AentryParam;
import cn.think.in.java.entity.AentryResult;
import cn.think.in.java.entity.RvoteParam;
import cn.think.in.java.entity.RvoteResult;

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
