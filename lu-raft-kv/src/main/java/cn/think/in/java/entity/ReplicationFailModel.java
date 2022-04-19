package cn.think.in.java.entity;

import java.util.concurrent.Callable;

import cn.think.in.java.common.Peer;
import lombok.Builder;

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
