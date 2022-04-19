package raft.client;

import cn.think.in.java.current.SleepHelper;
import cn.think.in.java.entity.LogEntry;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 莫那·鲁道
 */
@Slf4j
public class RaftClient3 {

    public static void main(String[] args) throws Throwable {

        RaftClientRPC rpc = new RaftClientRPC();

        int keyNum = 4;
        try {

            String key = "hello:" + keyNum;
            String value = "world:" + keyNum;

            String putResult = rpc.put(key, value);

            log.info("put response : {}, key={}, value={}", putResult, key, value);

            SleepHelper.sleep(1000);

            LogEntry logEntry = rpc.get(key);

            if (logEntry == null) {
                log.error("get logEntry : null, key={}", key);
                System.exit(1);
                return;
            }
            log.info("get logEntry : {}, key = {}", logEntry, key);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.exit(1);

    }

}
