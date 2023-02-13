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
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;

/**
 * @author 莫那·鲁道
 */
@Slf4j
public class RaftClient2 {

    public static void main(String[] args) throws Throwable {

        RaftClientRPC rpc = new RaftClientRPC();
        InetAddress localHost = InetAddress.getLocalHost();
        String prefix = localHost.getHostAddress();

        for (int i = 3; ; i++) {
            try {
                String key = "hello:" + i;
                // 客户端请求唯一id
                String requestId = prefix + i;

                String res = rpc.get(key, requestId);

                log.info("key={}, get response : {}", key, res);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Thread.sleep(1000);
            }

        }
    }

}
