package cn.think.in.java.entity;

import java.util.Arrays;

import cn.think.in.java.Consensus;
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
public class AentryParam extends BaseParam {

    /** 领导人的 Id，以便于跟随者重定向请求 */
    String leaderId;

    /**新的日志条目紧随之前的索引值  */
    long prevLogIndex;

    /** prevLogIndex 条目的任期号  */
    long preLogTerm;

    /** 准备存储的日志条目（表示心跳时为空；一次性发送多个是为了提高效率） */
    LogEntry[] entries;

    /** 领导人已经提交的日志的索引值  */
    long leaderCommit;

    public AentryParam() {
    }

    private AentryParam(Builder builder) {
        setTerm(builder.term);
        setServerId(builder.serverId);
        setLeaderId(builder.leaderId);
        setPrevLogIndex(builder.prevLogIndex);
        setPreLogTerm(builder.preLogTerm);
        setEntries(builder.entries);
        setLeaderCommit(builder.leaderCommit);
    }

    @Override
    public String toString() {
        return "AentryParam{" +
            "leaderId='" + leaderId + '\'' +
            ", prevLogIndex=" + prevLogIndex +
            ", preLogTerm=" + preLogTerm +
            ", entries=" + Arrays.toString(entries) +
            ", leaderCommit=" + leaderCommit +
            ", term=" + term +
            ", serverId='" + serverId + '\'' +
            '}';
    }

    public static Builder newBuilder() {
        return new Builder();
    }


    public static final class Builder {

        private long term;
        private String serverId;
        private String leaderId;
        private long prevLogIndex;
        private long preLogTerm;
        private LogEntry[] entries;
        private long leaderCommit;

        private Builder() {
        }

        public Builder term(long val) {
            term = val;
            return this;
        }

        public Builder serverId(String val) {
            serverId = val;
            return this;
        }

        public Builder leaderId(String val) {
            leaderId = val;
            return this;
        }

        public Builder prevLogIndex(long val) {
            prevLogIndex = val;
            return this;
        }

        public Builder preLogTerm(long val) {
            preLogTerm = val;
            return this;
        }

        public Builder entries(LogEntry[] val) {
            entries = val;
            return this;
        }

        public Builder leaderCommit(long val) {
            leaderCommit = val;
            return this;
        }

        public AentryParam build() {
            return new AentryParam(this);
        }
    }
}
