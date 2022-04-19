package raft.client;

import cn.think.in.java.entity.LogEntry;
import cn.think.in.java.rpc.DefaultRpcClient;
import cn.think.in.java.rpc.Request;
import cn.think.in.java.rpc.RpcClient;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author gwk_2
 * @date 2022/4/19 15:50
 */
public class RaftClientRPC {

    private static List<String> list = Lists.newArrayList("localhost:8777", "localhost:8778", "localhost:8779");

    private final static RpcClient CLIENT = new DefaultRpcClient();

    private AtomicLong count = new AtomicLong(3);

    public RaftClientRPC() throws Throwable {
        CLIENT.init();
    }

    /**
     * @param key
     * @return
     */
    public LogEntry get(String key) {
        ClientKVReq obj = ClientKVReq.builder().key(key).type(ClientKVReq.GET).build();

        int index = (int) (count.incrementAndGet() % list.size());

        String addr = list.get(index);

        LogEntry response;
        Request r = Request.builder().obj(obj).url(addr).cmd(Request.CLIENT_REQ).build();
        try {
            response = CLIENT.send(r);
        } catch (Exception e) {
            r.setUrl(list.get((int) ((count.incrementAndGet()) % list.size())));
            response = CLIENT.send(r);
        }

        return response;
    }

    /**
     * @param key
     * @param value
     * @return
     */
    public String put(String key, String value) {
        int index = (int) (count.incrementAndGet() % list.size());

        String addr = list.get(index);
        ClientKVReq obj = ClientKVReq.builder().key(key).value(value).type(ClientKVReq.PUT).build();

        Request r = Request.builder().obj(obj).url(addr).cmd(Request.CLIENT_REQ).build();
        String response;
        try {
            response = CLIENT.send(r);
        } catch (Exception e) {
            r.setUrl(list.get((int) ((count.incrementAndGet()) % list.size())));
            response = CLIENT.send(r);
        }

        return response;
    }
}
