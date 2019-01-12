package cn.think.in.java.entity;

import java.util.concurrent.Callable;

import cn.think.in.java.common.Peer;

/**
 *
 * @author 莫那·鲁道
 */
public class ReplicationFailModel {
    static String count = "_count";
    static String success = "_success";

    public String countKey;
    public String successKey;
    public Callable callable;
    public LogEntry logEntry;
    public Peer peer;
    public Long offerTime;

    public ReplicationFailModel(Callable callable, LogEntry logEntry, Peer peer, Long offerTime) {
        this.callable = callable;
        this.logEntry = logEntry;
        this.peer = peer;
        this.offerTime = offerTime;
        countKey = logEntry.getCommand().getKey() + count;
        successKey = logEntry.getCommand().getKey() + success;
    }

    private ReplicationFailModel(Builder builder) {
        countKey = builder.countKey;
        successKey = builder.successKey;
        callable = builder.callable;
        logEntry = builder.logEntry;
        peer = builder.peer;
        offerTime = builder.offerTime;
    }

    public static Builder newBuilder() {
        return new Builder();
    }


    public static final class Builder {

        private String countKey;
        private String successKey;
        private Callable callable;
        private LogEntry logEntry;
        private Peer peer;
        private Long offerTime;

        private Builder() {
        }

        public Builder countKey(String val) {
            countKey = val;
            return this;
        }

        public Builder successKey(String val) {
            successKey = val;
            return this;
        }

        public Builder callable(Callable val) {
            callable = val;
            return this;
        }

        public Builder logEntry(LogEntry val) {
            logEntry = val;
            return this;
        }

        public Builder peer(Peer val) {
            peer = val;
            return this;
        }

        public Builder offerTime(Long val) {
            offerTime = val;
            return this;
        }

        public ReplicationFailModel build() {
            return new ReplicationFailModel(this);
        }
    }
}
