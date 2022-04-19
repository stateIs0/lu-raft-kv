package cn.think.in.java.rpc;

import cn.think.in.java.LifeCycle;

/**
 * @author 莫那·鲁道
 */
public interface RpcClient extends LifeCycle {

    <R> Response<R> send(Request request, Class<R> c);

    <R> Response<R> send(Request request, Class<R> c, int timeout);
}
