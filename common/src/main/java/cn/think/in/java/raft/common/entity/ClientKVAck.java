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
package cn.think.in.java.raft.common.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 *
 * @author 莫那·鲁道
 */
@Getter
@Setter
@ToString
public class ClientKVAck implements Serializable {

    Object result;

    public ClientKVAck(Object result) {
        this.result = result;
    }

    private ClientKVAck(Builder builder) {
        setResult(builder.result);
    }

    public static ClientKVAck ok() {
        return new ClientKVAck("ok");
    }

    public static ClientKVAck fail() {
        return new ClientKVAck("fail");
    }

    public static Builder newBuilder() {
        return new Builder();
    }


    public static final class Builder {

        private Object result;

        private Builder() {
        }

        public Builder result(Object val) {
            result = val;
            return this;
        }

        public ClientKVAck build() {
            return new ClientKVAck(this);
        }
    }
}
