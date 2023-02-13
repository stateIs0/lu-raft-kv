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

import com.alipay.remoting.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.util.Scanner;
import java.util.UUID;

/**
 * @author 大东
 * @date 2023/2/13 15:00
 */
@Slf4j
public class RaftClient {

    public static void main(String[] args) throws Throwable {

        RaftClientRPC rpc = new RaftClientRPC();
        InetAddress localHost = InetAddress.getLocalHost();
        String prefix = localHost.getHostAddress() + UUID.randomUUID().toString().substring(0, 5);
        int cnt = 0;
        // 从键盘接收数据
        Scanner scan = new Scanner(System.in);

        // nextLine方式接收字符串
        System.out.println("Raft client is running");

        while (scan.hasNextLine()){
            String input = scan.nextLine();
            if (input.equals("exit")){
                scan.close();
                return;
            }
            String[] raftArgs = input.split(" ");
            int n = raftArgs.length;
            if (n != 2 && n != 3){
                System.out.println("invalid input");
                continue;
            }

            // get [key]
            if (n == 2){
                if (!raftArgs[0].equals("get")){
                    System.out.println("invalid input");
                    continue;
                }
                System.out.println(rpc.get(raftArgs[1], prefix + cnt++));
            }

            // put [key] [value]
            if (n == 3){
                if (!raftArgs[0].equals("put")){
                    System.out.println("invalid input");
                    continue;
                }
                System.out.println(rpc.put(raftArgs[1], raftArgs[2], prefix + cnt++));
            }
        }

    }

}
