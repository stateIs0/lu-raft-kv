package raft.client;

import cn.think.in.java.current.SleepHelper;
import cn.think.in.java.entity.LogEntry;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 莫那·鲁道
 */
@Slf4j
public class RaftClient1 {

    public static void main(String[] args) throws Throwable {

        RaftClientRPC rpc = new RaftClientRPC();

        for (int i = 3; i > -1; i++) {
            try {
                String key = "hello:" + i;
                String value = "world:" + i;

                String putResult = rpc.put(key, value);

                log.info("key = {}, value = {}, put response : {}", key, value, putResult);

                SleepHelper.sleep(1000);

                LogEntry logEntry = rpc.get(key);

                log.info("key = {}, value = {}, get response : {}", key, value, logEntry);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                i = i - 1;
            }

            SleepHelper.sleep(5000);
        }


    }

}
