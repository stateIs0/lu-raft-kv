package cn.think.in.java.membership.changes;

import cn.think.in.java.common.Peer;

/**
 *
 * 集群配置变更接口.
 *
 * @author 莫那·鲁道
 */
public interface ClusterMembershipChanges {

    /**
     * 添加节点.
     * @param newPeer
     * @return
     */
    Result addPeer(Peer newPeer);

    /**
     * 删除节点.
     * @param oldPeer
     * @return
     */
    Result removePeer(Peer oldPeer);
}

