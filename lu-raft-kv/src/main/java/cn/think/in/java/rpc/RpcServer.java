package cn.think.in.java.rpc;

/**
 * @author 莫那·鲁道
 */
public interface RpcServer {

    void start();

    void stop();

    Response handlerRequest(Request request);

}
