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
package cn.think.in.java.raft.server.changes;

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
