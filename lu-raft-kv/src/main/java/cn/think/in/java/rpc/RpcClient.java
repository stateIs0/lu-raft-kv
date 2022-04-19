package cn.think.in.java.rpc;

import cn.think.in.java.LifeCycle;

/**
 * @author 莫那·鲁道
 */
public interface RpcClient extends LifeCycle {

    /**
     * 发送请求, 并同步等待返回值.
     * @param request 参数
     * @param <R> 返回值泛型
     * @return
     */
    <R> R send(Request request);

    <R> R send(Request request, int timeout);
}
