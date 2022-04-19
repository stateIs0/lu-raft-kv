package cn.think.in.java.entity;

import cn.think.in.java.common.Peer;
import lombok.Builder;

import java.util.concurrent.Callable;

/**
 *
 * @author 莫那·鲁道
 */
@Builder
public class ReplicationFailModel {
    static String count = "_count";
    static String success = "_success";

    public String countKey;
    public String successKey;
    public Callable callable;
    public LogEntry logEntry;
    public Peer peer;
    public Long offerTime;
}
