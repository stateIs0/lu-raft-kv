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
package raft.client;

import java.io.Serializable;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 客户端协议——请求体
 * @author 莫那·鲁道
 */
@Getter
@Setter
@ToString
@Builder
public class ClientKVReq implements Serializable {

    /** 操作类型 */
    public static int PUT = 0;
    public static int GET = 1;

    int type;

    /** 键 */
    String key;

    /** 值 */
    String value;

    /** 请求id */
    String requestId;

    public enum Type {
        /** 1111 */
        PUT(0), GET(1);
        int code;

        Type(int code) {
            this.code = code;
        }

        public static Type value(int code ) {
            for (Type type : values()) {
                if (type.code == code) {
                    return type;
                }
            }
            return null;
        }
    }

}
