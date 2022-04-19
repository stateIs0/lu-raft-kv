package cn.think.in.java.rpc;

import cn.think.in.java.LifeCycle;

/**
 * @author 莫那·鲁道
 */
public interface RpcServer extends LifeCycle {

    Response handlerRequest(Request request);

}
