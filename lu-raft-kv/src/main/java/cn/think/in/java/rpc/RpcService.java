package cn.think.in.java.rpc;

import cn.think.in.java.LifeCycle;

/**
 * @author 莫那·鲁道
 */
public interface RpcService extends LifeCycle {

    /**
     * 处理请求.
     * @param request 请求参数.
     * @return 返回值.
     */
    Response<?> handlerRequest(Request request);

}
