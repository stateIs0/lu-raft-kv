

![visitors](https://visitor-badge.glitch.me/badge?page_id=stateIs0.lu-raft-kv&left_color=green&right_color=red)
[![build status](https://travis-ci.com/stateis0/lu-raft-kv.svg?branch=master)](https://travis-ci.com/stateis0/lu-raft-kv)
[![codecov](https://codecov.io/gh/stateis0/lu-raft-kv/branch/master/graph/badge.svg)](https://codecov.io/gh/stateis0/lu-raft-kv)
![license](https://img.shields.io/github/license/stateis0/lu-raft-kv.svg)
[![average time to resolve an issue](http://isitmaintained.com/badge/resolution/stateis0/lu-raft-kv.svg)](http://isitmaintained.com/project/stateis0/lu-raft-kv "average time to resolve an issue")
[![percentage of issues still open](http://isitmaintained.com/badge/open/stateis0/lu-raft-kv.svg)](http://isitmaintained.com/project/stateis0/lu-raft-kv "percentage of issues still open")

## Lu-Raft-KV-Storage

这是一个 Java 版本的 Raft(CP) KV  分布式存储实现. 可用于 Raft 初学者深入学习 Raft 协议. 

相关文章 [ Java 版 Raft 分布式 KV 存储](http://thinkinjava.cn/2019/01/12/2019/2019-01-12-lu-raft-kv/) 需要🪜

为了尽可能的保证数据一致性，该实现的"性能"没有基于 AP 的实现好。

目前实现了 Raft 4 大核心功能的其中 2 个功能.

1. leader 选举
2. 日志复制
3. 成员变更(未测试)
4. 快照压缩(未实现)

## Design 

完全是参照 RAFT 论文来写的. 没有任何妥协.

![image](https://user-images.githubusercontent.com/24973360/50371851-b13de880-05fd-11e9-958a-5813b3b6d761.png)



## quick start

🔥🔥🔥🔥🔥 注意：该项目仅支持 oracle jdk8 启动。

🔴🔴🔴🔴🔴 注意：idea 需要安装 lombok 插件。

#### 验证 "leader 选举"

1. 在 idea 中配置 5 个 application 启动项,配置 main 类为 RaftNodeBootStrap 类, 加入 -DserverPort=8775 -DserverPort=8776 -DserverPort=8777 -DserverPort=8778 -DserverPort=8779 
  系统配置, 表示分布式环境下的 5 个机器节点.
2. 依次启动 5 个 RaftNodeBootStrap 节点, 端口分别是 8775，8776， 8777, 8778, 8779.
3. 观察控制台, 约 6 秒后, 会发生选举事件,此时,会产生一个 leader. 而  leader 会立刻发送心跳维持自己的地位.
4. 如果leader 的端口是  8775, 使用 idea 关闭 8775 端口，模拟节点挂掉, 大约 15 秒后, 会重新开始选举, 并且会在剩余的 4 个节点中,产生一个新的 leader.  并开始发送心跳日志。

#### 验证"日志复制"

##### 正常状态下



1. 在 idea 中配置 5 个 application 启动项,配置 main 类为 RaftNodeBootStrap 类, 加入 -DserverPort=8775 -DserverPort=8776 -DserverPort=8777 -DserverPort=8778 -DserverPort=8779 
2. 依次启动 5 个 RaftNodeBootStrap 节点, 端口分别是 8775，8776， 8777, 8778, 8779.
3. 使用客户端写入 kv 数据.
4. 杀掉所有节点, 使用 junit test 读取每个 rocksDB 的值, 验证每个节点的数据是否一致.

##### 非正常状态下

1. 在 idea 中配置 5 个 application 启动项,配置 main 类为 RaftNodeBootStrap 类, 加入 -DserverPort=8775 -DserverPort=8776 -DserverPort=8777 -DserverPort=8778 -DserverPort=8779 
2. 依次启动 5 个 RaftNodeBootStrap 节点, 端口分别是 8775，8776， 8777, 8778, 8779.
3. 使用客户端写入 kv 数据.
4. 杀掉 leader （假设是 8775）.
5. 再次写入数据.
6. 重启 8775.
7. 关闭所有节点, 读取 RocksDB 验证数据一致性.


## Acknowledgments

感谢 SOFA-Bolt 提供 RPC 网络框架 https://github.com/alipay/sofa-bolt

感谢 rocksDB 提供 KV 存储 https://github.com/facebook/rocksdb

## License

[Apache 2.0 License.](https://github.com/stateIs0/lu-raft-kv/blob/master/LICENSE)


## Star History

![Star History Chart](https://starchart.cc/stateis0/lu-raft-kv.svg)


