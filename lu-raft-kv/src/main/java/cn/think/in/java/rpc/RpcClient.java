package cn.think.in.java.rpc;

/**
 *
 * @author 莫那·鲁道
 */
public interface RpcClient {

    Response send(Request request);

    Response send(Request request, int timeout);
}
