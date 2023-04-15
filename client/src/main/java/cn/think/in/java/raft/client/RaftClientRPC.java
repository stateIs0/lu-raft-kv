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
package cn.think.in.java.raft.client;

//import raft.server.entity.LogEntry;
//import raft.server.rpc.DefaultRpcClient;
//import raft.server.rpc.Request;
//import raft.server.rpc.RpcClient;
//import com.google.common.collect.Lists;

import cn.think.in.java.raft.common.entity.ClientKVAck;
import cn.think.in.java.raft.common.entity.ClientKVReq;
import cn.think.in.java.raft.common.rpc.DefaultRpcClient;
import cn.think.in.java.raft.common.entity.LogEntry;
import cn.think.in.java.raft.common.rpc.Request;
import cn.think.in.java.raft.common.rpc.RpcClient;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author gwk_2
 * @date 2022/4/19 15:50
 */
public class RaftClientRPC {

    private static List<String> list = Lists.newArrayList("localhost:8777", "localhost:8778", "localhost:8779");

    private final static RpcClient CLIENT = new DefaultRpcClient();

    private AtomicLong count = new AtomicLong(3);

    public RaftClientRPC() throws Throwable {
        CLIENT.init();
    }

    /**
     * @param key
     * @return
     */
    public LogEntry get(String key) {
        ClientKVReq obj = ClientKVReq.builder().key(key).type(ClientKVReq.GET).build();

        int index = (int) (count.incrementAndGet() % list.size());

        String addr = list.get(index);

        ClientKVAck response;
        Request r = Request.builder().obj(obj).url(addr).cmd(Request.CLIENT_REQ).build();
        try {
            response = CLIENT.send(r);
        } catch (Exception e) {
            r.setUrl(list.get((int) ((count.incrementAndGet()) % list.size())));
            response = CLIENT.send(r);
        }

        return (LogEntry)response.getResult();
    }

    /**
     * @param key
     * @param value
     * @return
     */
    public String put(String key, String value) {
        int index = (int) (count.incrementAndGet() % list.size());

        String addr = list.get(index);
        ClientKVReq obj = ClientKVReq.builder().key(key).value(value).type(ClientKVReq.PUT).build();

        Request r = Request.builder().obj(obj).url(addr).cmd(Request.CLIENT_REQ).build();
        ClientKVAck response;
        try {
            response = CLIENT.send(r);
        } catch (Exception e) {
            r.setUrl(list.get((int) ((count.incrementAndGet()) % list.size())));
            response = CLIENT.send(r);
        }

        return response.getResult().toString();
    }
}
