package cn.think.in.java.rpc;

import cn.think.in.java.exception.RaftRemotingException;
import com.alipay.remoting.exception.RemotingException;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author 莫那·鲁道
 */
@Slf4j
public class DefaultRpcClient implements RpcClient {

    private final static com.alipay.remoting.rpc.RpcClient CLIENT = new com.alipay.remoting.rpc.RpcClient();

    @Override
    public <R> R send(Request request) {
        return send(request, (int) TimeUnit.SECONDS.toMillis(5));
    }

    @Override
    public <R> R send(Request request, int timeout) {
        Response<R> result;
        try {
            result = (Response<R>) CLIENT.invokeSync(request.getUrl(), request, timeout);
            return result.getResult();
        } catch (RemotingException e) {
            throw new RaftRemotingException("rpc RaftRemotingException ", e);
        } catch (InterruptedException e) {
            // ignore
        }
        return null;
    }

    @Override
    public void init() {
        CLIENT.init();
    }

    @Override
    public void destroy() {
        CLIENT.shutdown();
        log.info("destroy success");
    }
}
