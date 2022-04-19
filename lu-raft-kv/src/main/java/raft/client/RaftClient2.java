package raft.client;

import java.util.List;

import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.think.in.java.entity.LogEntry;
import cn.think.in.java.rpc.DefaultRpcClient;
import cn.think.in.java.rpc.Request;
import cn.think.in.java.rpc.Response;
import cn.think.in.java.rpc.RpcClient;

/**
 *
 * @author 莫那·鲁道
 */
public class RaftClient2 {

    private static final Logger LOGGER = LoggerFactory.getLogger(RaftClient.class);


    private final static RpcClient client = new DefaultRpcClient();

    static String addr = "localhost:8778";
    static List<String> list3 = Lists.newArrayList("localhost:8777", "localhost:8778", "localhost:8779");
    static List<String> list2 = Lists.newArrayList( "localhost:8777", "localhost:8779");
    static List<String> list1 = Lists.newArrayList( "localhost:8779");

    public static void main(String[] args) throws InterruptedException {
        for (int i = 3; ; i++) {

            try {
                Request r = new Request();

                int size = list2.size();

                ClientKVReq obj = ClientKVReq.newBuilder().key("hello:" + i).type(ClientKVReq.GET).build();
                int index = (i) % size;
                addr = list2.get(index);
                r.setUrl(addr);
                r.setObj(obj);
                r.setCmd(Request.CLIENT_REQ);

                LogEntry response2 = client.send(r);

                LOGGER.info("request content : {}, url : {}, get response : {}", obj.key + "=" + obj.getValue(), r.getUrl(), response2);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Thread.sleep(1000);

            }

        }
    }

}
