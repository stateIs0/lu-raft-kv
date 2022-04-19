package raft.client;

import cn.think.in.java.entity.LogEntry;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 莫那·鲁道
 */
@Slf4j
public class RaftClient2 {

    public static void main(String[] args) throws Throwable {

        RaftClientRPC rpc = new RaftClientRPC();

        for (int i = 3; ; i++) {
            try {
                String key = "hello:" + i;

                LogEntry logEntry = rpc.get(key);

                log.info("key={}, get response : {}", key, logEntry);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Thread.sleep(1000);
            }

        }
    }

}
