package cn.think.in.java.raft.client;

import cn.think.in.java.raft.common.entity.LogEntry;
import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;

/**
 * Created by 大东 on 2023/2/25.
 */
@Slf4j
public class RaftClientWithCommandLine {

    public static void main(String[] args) throws Throwable {

        RaftClientRPC rpc = new RaftClientRPC();

        // 从键盘接收数据
        Scanner scan = new Scanner(System.in);

        // nextLine方式接收字符串
        System.out.println("Raft client is running, please input command:");

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
                LogEntry logEntry = rpc.get(raftArgs[1]);
                if (logEntry == null || logEntry.getCommand() == null){
                    System.out.println("null");
                } else {
                    System.out.println(logEntry.getCommand().getValue());
                }

            }

            // [op] [key] [value]
            if (n == 3){
                if (raftArgs[0].equals("put")){
                    System.out.println(rpc.put(raftArgs[1], raftArgs[2]));
                } else {
                    System.out.println("invalid input");
                }
            }
        }

    }

}
