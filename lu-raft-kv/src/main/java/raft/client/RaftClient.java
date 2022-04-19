package raft.client;

import cn.think.in.java.current.SleepHelper;
import cn.think.in.java.entity.LogEntry;
import cn.think.in.java.rpc.DefaultRpcClient;
import cn.think.in.java.rpc.Request;
import cn.think.in.java.rpc.RpcClient;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author 莫那·鲁道
 */
public class RaftClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(RaftClient.class);


    private final static RpcClient CLIENT = new DefaultRpcClient();

    static String addr = "localhost:8778";
    static List<String> list = Lists.newArrayList("localhost:8777", "localhost:8778", "localhost:8779");

    public static void main(String[] args) throws InterruptedException {

        AtomicLong count = new AtomicLong(3);

        for (int i = 3; ; i++) {
            try {
                int index = (int) (count.incrementAndGet() % list.size());
                addr = list.get(index);

                ClientKVReq obj = ClientKVReq.newBuilder().key("hello:" + i).value("world:" + i).type(ClientKVReq.PUT).build();

                Request r = new Request();
                r.setObj(obj);
                r.setUrl(addr);
                r.setCmd(Request.CLIENT_REQ);
                String response;
                try {
                    response = CLIENT.send(r);
                } catch (Exception e) {
                    r.setUrl(list.get((int) ((count.incrementAndGet()) % list.size())));
                    response = CLIENT.send(r);
                }

                LOGGER.info("request content : {}, url : {}, put response : {}", obj.key + "=" + obj.getValue(), r.getUrl(), response);

                SleepHelper.sleep(1000);

                obj = ClientKVReq.newBuilder().key("hello:" + i).type(ClientKVReq.GET).build();

                addr = list.get(index);
                addr = list.get(index);
                r.setUrl(addr);
                r.setObj(obj);

                LogEntry response2;
                try {
                    response2 = CLIENT.send(r);
                } catch (Exception e) {
                    r.setUrl(list.get((int) ((count.incrementAndGet()) % list.size())));
                    response2 = CLIENT.send(r);
                }

                LOGGER.info("request content : {}, url : {}, get response : {}", obj.key + "=" + obj.getValue(), r.getUrl(), response2);
            } catch (Exception e) {
                e.printStackTrace();
                i = i - 1;
            }

            SleepHelper.sleep(5000);
        }


    }

}
