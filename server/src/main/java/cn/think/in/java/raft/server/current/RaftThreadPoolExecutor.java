/*
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package cn.think.in.java.raft.server.current;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author 莫那·鲁道
 */
@Slf4j
public class RaftThreadPoolExecutor extends ThreadPoolExecutor {

    private static final ThreadLocal<Long> COST_TIME_WATCH = ThreadLocal.withInitial(System::currentTimeMillis);

    public RaftThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                                  BlockingQueue<Runnable> workQueue, RaftThreadPool.NameThreadFactory nameThreadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, nameThreadFactory);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        COST_TIME_WATCH.get();
        log.debug("raft thread pool before Execute");
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        log.debug("raft thread pool after Execute, cost time : {}", System.currentTimeMillis() - COST_TIME_WATCH.get());
        COST_TIME_WATCH.remove();
    }

    @Override
    protected void terminated() {
        log.info("active count : {}, queueSize : {}, poolSize : {}", getActiveCount(), getQueue().size(), getPoolSize());
    }
}
