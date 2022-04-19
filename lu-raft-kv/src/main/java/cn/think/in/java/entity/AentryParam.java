package cn.think.in.java.entity;

import cn.think.in.java.Consensus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * 附加日志 RPC 参数. handlerAppendEntries
 *
 * @author 莫那·鲁道
 * @see Consensus#appendEntries(AentryParam)
 */
@Getter
@Setter
@ToString
@Builder
public class AentryParam {

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
