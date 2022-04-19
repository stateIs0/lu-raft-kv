package cn.think.in.java.entity;

import cn.think.in.java.Consensus;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 请求投票 RPC 参数.
 *
 * @author 莫那·鲁道
 * @see Consensus#requestVote(RvoteParam)
 */
@Getter
@Setter
@Builder
@Data
public class RvoteParam {
    /** 候选人的任期号  */
    private long term;

    /** 被请求者 ID(ip:selfPort) */
    private String serverId;

    /** 请求选票的候选人的 Id(ip:selfPort) */
    private String candidateId;

    /** 候选人的最后日志条目的索引值 */
    private long lastLogIndex;

    /** 候选人最后日志条目的任期号  */
    private long lastLogTerm;
}
