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

import cn.think.in.java.entity.LogEntry;
import cn.think.in.java.rpc.DefaultRpcClient;
import cn.think.in.java.rpc.Request;
import cn.think.in.java.rpc.RpcClient;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * raft客户端RPC调用模块
 *
 * @author gwk_2，大东
 * @date 2023/2/12 21:04
 */
public class RaftClientRPC {

    private static List<String> list = Lists.newArrayList("localhost:8775", "localhost:8776", "localhost:8777", "localhost:8778", "localhost:8779");

    private final static RpcClient CLIENT = new DefaultRpcClient();

    private AtomicLong count = new AtomicLong(3);

    public RaftClientRPC() throws Throwable {
        CLIENT.init();
    }

    /**
     * @param key 查询的key值
     * @return
     */
    public String get(String key, String requestId) {

        // raft客户端协议请求体
        ClientKVReq obj = ClientKVReq.builder().key(key).type(ClientKVReq.GET).requestId(requestId).build();

        int index = (int) (count.incrementAndGet() % list.size());

        String addr = list.get(index);

        // rpc协议请求体
        Request r = Request.builder().obj(obj).url(addr).cmd(Request.CLIENT_REQ).build();

        ClientKVAck response = null;
        while (response == null){
            // 不断重试，直到获取服务端响应
            try {
                response = CLIENT.send(r);
            } catch (Exception e) {
                r.setUrl(list.get((int) ((count.incrementAndGet()) % list.size())));
            }
        }
        if (response.getResult() == null || response.getResult().equals("fail")){
            return null;
        }

        return (String) response.getResult();
    }

    /**
     * @param key
     * @param value
     * @return
     */
    public String put(String key, String value, String requestId) {
        int index = (int) (count.incrementAndGet() % list.size());

        String addr = list.get(index);
        ClientKVReq obj = ClientKVReq.builder().key(key).value(value).type(ClientKVReq.PUT).requestId(requestId).build();

        Request r = Request.builder().obj(obj).url(addr).cmd(Request.CLIENT_REQ).build();
        ClientKVAck response = null;
        while (response == null || response.getResult().equals("fail")){
            // 不断重试，直到获取服务端响应
            try {
                response = CLIENT.send(r);
            } catch (Exception e) {
                r.setUrl(list.get((int) ((count.incrementAndGet()) % list.size())));
            }
        }

        return (String) response.getResult();
    }
}
