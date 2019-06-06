package cn.think.in.java.rpc;

import com.alipay.remoting.BizContext;

public class MyServer {
    private static com.alipay.remoting.rpc.RpcServer rpcServer;
    static {
        rpcServer = new com.alipay.remoting.rpc.RpcServer(8080);
        rpcServer.registerUserProcessor(new RaftUserProcessor<Request>() {
            @Override
            public Object handleRequest(BizContext bizCtx, Request request) throws Exception {
                return handlerRequest(request);
            }
        });
    }
    public static void main(String[] args) {
        start();
    }
    public static void start() {
        rpcServer.start();
    }
    private static Response handlerRequest(Request req) {
        System.out.println("dddddd");
        return new Response("服务端返回");
    }
}
