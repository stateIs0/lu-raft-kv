package cn.think.in.java.rpc;

import cn.think.in.java.entity.AentryParam;
import cn.think.in.java.entity.RvoteParam;
import lombok.Builder;
import lombok.Data;
import raft.client.ClientKVReq;

import java.io.Serializable;

/**
 * @author 莫那·鲁道
 */
@Builder
@Data
public class Request implements Serializable {

    /** 请求投票 */
    public static final int R_VOTE = 0;
    /** 附加日志 */
    public static final int A_ENTRIES = 1;
    /** 客户端 */
    public static final int CLIENT_REQ = 2;
    /** 配置变更. add */
    public static final int CHANGE_CONFIG_ADD = 3;
    /** 配置变更. remove */
    public static final int CHANGE_CONFIG_REMOVE = 4;
    /** 请求类型 */
    private int cmd = -1;

    /**
     * param
     *
     * @see AentryParam
     * @see RvoteParam
     * @see ClientKVReq
     */
    private Object obj;

    private String url;

    public Request() {
    }

    public Request(int cmd, Object obj, String url) {
        this.cmd = cmd;
        this.obj = obj;
        this.url = url;
    }
}
