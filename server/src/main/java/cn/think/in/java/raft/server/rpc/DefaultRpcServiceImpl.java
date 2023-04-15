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
package cn.think.in.java.raft.server.rpc;

import cn.think.in.java.raft.common.entity.ClientKVReq;
import cn.think.in.java.raft.server.changes.ClusterMembershipChanges;
import cn.think.in.java.raft.common.entity.AentryParam;
import cn.think.in.java.raft.common.entity.Peer;
import cn.think.in.java.raft.common.entity.RvoteParam;
import cn.think.in.java.raft.server.impl.DefaultNode;
import cn.think.in.java.raft.common.rpc.Request;
import cn.think.in.java.raft.common.rpc.Response;
import com.alipay.remoting.BizContext;
import com.alipay.remoting.rpc.RpcServer;
import lombok.extern.slf4j.Slf4j;

/**
 * Raft Server
 *
 * @author 莫那·鲁道
 */
@Slf4j
public class DefaultRpcServiceImpl implements RpcService {

    private final DefaultNode node;

    private final RpcServer rpcServer;

    public DefaultRpcServiceImpl(int port, DefaultNode node) {
        rpcServer = new RpcServer(port, false, false);
        rpcServer.registerUserProcessor(new RaftUserProcessor<Request>() {

            @Override
            public Object handleRequest(BizContext bizCtx, Request request) {
                return handlerRequest(request);
            }
        });

        this.node = node;
    }


    @Override
    public Response<?> handlerRequest(Request request) {
        if (request.getCmd() == Request.R_VOTE) {
            return new Response<>(node.handlerRequestVote((RvoteParam) request.getObj()));
        } else if (request.getCmd() == Request.A_ENTRIES) {
            return new Response<>(node.handlerAppendEntries((AentryParam) request.getObj()));
        } else if (request.getCmd() == Request.CLIENT_REQ) {
            return new Response<>(node.handlerClientRequest((ClientKVReq) request.getObj()));
        } else if (request.getCmd() == Request.CHANGE_CONFIG_REMOVE) {
            return new Response<>(((ClusterMembershipChanges) node).removePeer((Peer) request.getObj()));
        } else if (request.getCmd() == Request.CHANGE_CONFIG_ADD) {
            return new Response<>(((ClusterMembershipChanges) node).addPeer((Peer) request.getObj()));
        }
        return null;
    }


    @Override
    public void init() {
        rpcServer.start();
    }

    @Override
    public void destroy() {
        rpcServer.stop();
        log.info("destroy success");
    }
}
