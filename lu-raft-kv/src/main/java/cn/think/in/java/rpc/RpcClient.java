package cn.think.in.java.rpc;

import cn.think.in.java.LifeCycle;

/**
 *
 * @author 莫那·鲁道
 */
public interface RpcClient extends LifeCycle {

    Response send(Request request);

    Response send(Request request, int timeout);
}
