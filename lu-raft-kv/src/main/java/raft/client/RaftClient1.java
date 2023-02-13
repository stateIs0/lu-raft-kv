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

import cn.think.in.java.current.SleepHelper;
import cn.think.in.java.entity.LogEntry;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.util.UUID;

/**
 * @author 莫那·鲁道
 */
@Slf4j
public class RaftClient1 {

    public static void main(String[] args) throws Throwable {

        RaftClientRPC rpc = new RaftClientRPC();
        InetAddress localHost = InetAddress.getLocalHost();
        String prefix = localHost.getHostAddress() + UUID.randomUUID().toString().substring(0, 5);

        for (int i = 3; i > -1; i++) {
            String key = "[test4:" + i +"]";
            String value = "[test4:" + i + "]";
            // 客户端请求唯一id
            String requestId = prefix + i;
            try {
                String putResult = rpc.put(key, value, requestId);
                log.info("key = {}, value = {}, put response : {}", key, value, putResult);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

            Thread.sleep(5000);

            try {
                String res = rpc.get(key, requestId);
                log.info("key = {}, value = {}, get response : {}", key, value, res);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

        }


    }

}
