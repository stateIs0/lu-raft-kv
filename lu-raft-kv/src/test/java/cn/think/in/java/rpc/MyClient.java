package cn.think.in.java.rpc;

import com.alipay.remoting.exception.RemotingException;

import java.io.Serializable;

public class MyClient  {
    private final static com.alipay.remoting.rpc.RpcClient CLIENT = new com.alipay.remoting.rpc.RpcClient();
    static {
        CLIENT.init();
    }

    public static void main(String[] args) throws RemotingException, InterruptedException {
        send();
    }
    public static void send() throws RemotingException, InterruptedException {
        Response res = null;
        for (int i = 0; i < 10; i++) {
            Request request = Request.newBuilder()
                    .cmd(Request.A_ENTRIES)
                    .obj("req")
                    .build();
            res = (Response) CLIENT.invokeSync("127.0.0.1:8080", request, 200000);
            System.out.println("Client" + (i +1 ) + "次收到服务端回应" + res.getResult());
        }
    }
}
