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
package cn.think.in.java.raft.server;

import cn.think.in.java.raft.common.entity.NodeConfig;
import cn.think.in.java.raft.server.constant.StateMachineSaveType;
import cn.think.in.java.raft.server.impl.DefaultNode;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

import static cn.think.in.java.raft.server.config.Constant.*;

/**
 * -DserverPort=8775
 * -DserverPort=8776
 * -DserverPort=8777
 * -DserverPort=8778
 * -DserverPort=8779
 */
@Slf4j
public class RaftNodeBootStrap {

    public static final String[] DEFAULT_PROCESS = new String[]{"localhost:8775", "localhost:8776", "localhost:8777", "localhost:8778", "localhost:8779"};

    public static void main(String[] args) throws Throwable {
        boot();
    }
    
    public static void boot() throws Throwable {
        String property = System.getProperty(CLUSTER_ADDR_LIST);
        String[] peerAddr;

        if (StringUtil.isNullOrEmpty(property)) {
            peerAddr = DEFAULT_PROCESS;
        } else {
            peerAddr = property.split(SPLIT);
        }

        NodeConfig config = new NodeConfig();

        // 自身节点
        config.setSelfPort(Integer.parseInt(System.getProperty(SERVER_PORT, "8779")));

        // 其他节点地址
        config.setPeerAddrs(Arrays.asList(peerAddr));
        config.setStateMachineSaveType(StateMachineSaveType.ROCKS_DB.getTypeName());

        Node node = DefaultNode.getInstance();
        node.setConfig(config);

        node.init();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            synchronized (node) {
                node.notifyAll();
            }
        }));

        log.info("gracefully wait");

        synchronized (node) {
            node.wait();
        }

        log.info("gracefully stop");
        node.destroy();
    }

}
