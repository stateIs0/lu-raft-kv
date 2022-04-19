package cn.think.in.java.rpc;

import cn.think.in.java.common.Peer;
import cn.think.in.java.entity.AentryParam;
import cn.think.in.java.entity.RvoteParam;
import cn.think.in.java.impl.DefaultNode;
import cn.think.in.java.membership.changes.ClusterMembershipChanges;
import com.alipay.remoting.BizContext;
import com.alipay.remoting.rpc.RpcServer;
import lombok.extern.slf4j.Slf4j;
import raft.client.ClientKVReq;

/**
 * Raft Server
 *
 * @author 莫那·鲁道
 */
@Slf4j
public class DefaultRpcServiceImpl implements RpcService {

    private final DefaultNode node;

    private final RpcServer rpcServer;

    public DefaultRpcServiceImpl(int port, DefaultNode node) {
        rpcServer = new RpcServer(port, false, false);
        rpcServer.registerUserProcessor(new RaftUserProcessor<Request>() {

            @Override
            public Object handleRequest(BizContext bizCtx, Request request) {
                return handlerRequest(request);
            }
        });

        this.node = node;
    }


    @Override
    public Response<?> handlerRequest(Request request) {
        if (request.getCmd() == Request.R_VOTE) {
            return new Response<>(node.handlerRequestVote((RvoteParam) request.getObj()));
        } else if (request.getCmd() == Request.A_ENTRIES) {
            return new Response<>(node.handlerAppendEntries((AentryParam) request.getObj()));
        } else if (request.getCmd() == Request.CLIENT_REQ) {
            return new Response<>(node.handlerClientRequest((ClientKVReq) request.getObj()));
        } else if (request.getCmd() == Request.CHANGE_CONFIG_REMOVE) {
            return new Response<>(((ClusterMembershipChanges) node).removePeer((Peer) request.getObj()));
        } else if (request.getCmd() == Request.CHANGE_CONFIG_ADD) {
            return new Response<>(((ClusterMembershipChanges) node).addPeer((Peer) request.getObj()));
        }
        return null;
    }


    @Override
    public void init() {
        rpcServer.start();
    }

    @Override
    public void destroy() {
        rpcServer.stop();
        log.info("destroy success");
    }
}
