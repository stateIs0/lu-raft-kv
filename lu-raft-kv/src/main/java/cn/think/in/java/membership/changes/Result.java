package cn.think.in.java.membership.changes;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author 莫那·鲁道
 */
@Getter
@Setter
public class Result {

    public static final int FAIL = 0;
    public static final int SUCCESS = 1;

    int status;

    String leaderHint;

    public Result() {
    }

    public Result(Builder builder) {
        setStatus(builder.status);
        setLeaderHint(builder.leaderHint);
    }

    @Override
    public String toString() {
        return "Result{" +
            "status=" + status +
            ", leaderHint='" + leaderHint + '\'' +
            '}';
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Getter
    public enum Status {
        FAIL(0), SUCCESS(1);

        int code;

        Status(int code) {
            this.code = code;
        }

        public static Status value(int v) {
            for (Status i : values()) {
                if (i.code == v) {
                    return i;
                }
            }
            return null;
        }
    }

    public static final class Builder {

        private int status;
        private String leaderHint;

        private Builder() {
        }

        public Builder status(int val) {
            status = val;
            return this;
        }

        public Builder leaderHint(String val) {
            leaderHint = val;
            return this;
        }

        public Result build() {
            return new Result(this);
        }
    }
}
