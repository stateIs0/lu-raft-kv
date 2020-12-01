package cn.think.in.java;

import java.util.Arrays;

import com.google.common.collect.Lists;

import cn.think.in.java.common.NodeConfig;
import cn.think.in.java.constant.StateMachineSaveType;
import cn.think.in.java.impl.DefaultNode;

/**
 * -DserverPort=8775
 * -DserverPort=8776
 * -DserverPort=8777
 * -DserverPort=8778
 * -DserverPort=8779
 */
public class RaftNodeBootStrap {

    public static void main(String[] args) throws Throwable {
        main0();
    }

    public static void main0() throws Throwable {
        String[] peerAddr = {"localhost:8775","localhost:8776","localhost:8777", "localhost:8778", "localhost:8779"};

        NodeConfig config = new NodeConfig();

        // 自身节点
        config.setSelfPort(Integer.valueOf(System.getProperty("serverPort")));

        // 其他节点地址
        config.setPeerAddrs(Arrays.asList(peerAddr));
        config.setStateMachineSaveType(StateMachineSaveType.REDIS);
        Node node = DefaultNode.getInstance();
        node.setConfig(config);

        node.init();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                node.destroy();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }));

    }

}
