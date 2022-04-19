package cn.think.in.java.rpc;

import com.alipay.remoting.exception.RemotingException;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.think.in.java.exception.RaftRemotingException;

import java.util.concurrent.TimeUnit;

/**
 *
 * @author 莫那·鲁道
 */
@Slf4j
public class DefaultRpcClient implements RpcClient {

    public static Logger logger = LoggerFactory.getLogger(DefaultRpcClient.class.getName());

    private final static com.alipay.remoting.rpc.RpcClient CLIENT = new com.alipay.remoting.rpc.RpcClient();
    static {
        CLIENT.init();
    }


    @Override
    public Response send(Request request) {
        return send(request, (int) TimeUnit.SECONDS.toMillis(5));
    }

    @Override
    public Response send(Request request, int timeout) {
        Response result = null;
        try {
            result = (Response) CLIENT.invokeSync(request.getUrl(), request, timeout);
        } catch (RemotingException e) {
            throw new RaftRemotingException("rpc RaftRemotingException ", e);
        } catch (InterruptedException e) {
            // ignore
        }
        return (result);
    }

    @Override
    public void init() throws Throwable {

    }

    @Override
    public void destroy() throws Throwable {
        CLIENT.shutdown();
        log.info("destroy success");
    }
}
