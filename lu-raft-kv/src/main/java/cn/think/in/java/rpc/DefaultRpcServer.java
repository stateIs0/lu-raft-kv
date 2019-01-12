package cn.think.in.java.rpc;

import com.alipay.remoting.BizContext;

import cn.think.in.java.common.Peer;
import cn.think.in.java.entity.AentryParam;
import cn.think.in.java.entity.RvoteParam;
import cn.think.in.java.impl.DefaultNode;
import cn.think.in.java.membership.changes.ClusterMembershipChanges;
import raft.client.ClientKVReq;

/**
 *
 * Raft Server
 *
 * @author 莫那·鲁道
 *
 */
@SuppressWarnings("unchecked")
public class DefaultRpcServer implements RpcServer {

    private volatile boolean flag;

    private DefaultNode node;

    private com.alipay.remoting.rpc.RpcServer rpcServer;

    public DefaultRpcServer(int port, DefaultNode node) {

        if (flag) {
            return;
        }
        synchronized (this) {
            if (flag) {
                return;
            }

            rpcServer = new com.alipay.remoting.rpc.RpcServer(port, false, false);

            rpcServer.registerUserProcessor(new RaftUserProcessor<Request>() {

                @Override
                public Object handleRequest(BizContext bizCtx, Request request) throws Exception {
                    return handlerRequest(request);
                }
            });

            this.node = node;
            flag = true;
        }

    }

    @Override
    public void start() {
        rpcServer.start();
    }

    @Override
    public void stop() {
        rpcServer.stop();
    }

    @Override
    public Response handlerRequest(Request request) {
        if (request.getCmd() == Request.R_VOTE) {
            return new Response(node.handlerRequestVote((RvoteParam) request.getObj()));
        } else if (request.getCmd() == Request.A_ENTRIES) {
            return new Response(node.handlerAppendEntries((AentryParam) request.getObj()));
        } else if (request.getCmd() == Request.CLIENT_REQ) {
            return new Response(node.handlerClientRequest((ClientKVReq) request.getObj()));
        } else if (request.getCmd() == Request.CHANGE_CONFIG_REMOVE) {
            return new Response(((ClusterMembershipChanges) node).removePeer((Peer) request.getObj()));
        } else if (request.getCmd() == Request.CHANGE_CONFIG_ADD) {
            return new Response(((ClusterMembershipChanges) node).addPeer((Peer) request.getObj()));
        }
        return null;
    }


}
