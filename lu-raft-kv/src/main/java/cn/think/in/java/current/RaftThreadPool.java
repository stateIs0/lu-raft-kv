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
package cn.think.in.java.current;

import java.util.concurrent.*;

/**
 *
 * @author 莫那·鲁道
 */
public class RaftThreadPool {

    private static int cup = Runtime.getRuntime().availableProcessors();
    private static int maxPoolSize = cup * 2;
    private static final int queueSize = 1024;
    private static final long keepTime = 1000 * 60;
    private static TimeUnit keepTimeUnit = TimeUnit.MILLISECONDS;

    private static final ScheduledExecutorService ss;
    private static final ThreadPoolExecutor te;

    static {
        ss = new ScheduledThreadPoolExecutor(cup, new NameThreadFactory());
        te = new RaftThreadPoolExecutor(
                    cup,
                    maxPoolSize,
                    keepTime,
                    keepTimeUnit,
                    new LinkedBlockingQueue<>(queueSize),
                    new NameThreadFactory());
    }


    public static void scheduleAtFixedRate(Runnable r, long initDelay, long delay) {
        ss.scheduleAtFixedRate(r, initDelay, delay, TimeUnit.MILLISECONDS);
    }


    public static void scheduleWithFixedDelay(Runnable r, long delay) {
        ss.scheduleWithFixedDelay(r, 0, delay, TimeUnit.MILLISECONDS);
    }

    public static <T> Future submit(Callable r) {
        // 将 r 的返回值封装成 Future<T>
        return te.submit(r);
    }

    public static void execute(Runnable r) {
        te.execute(r);
    }

    public static void execute(Runnable r, boolean sync) {
        if (sync) {
            r.run();
        } else {
            te.execute(r);
        }
    }

    static class NameThreadFactory implements ThreadFactory {

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new RaftThread("Raft thread", r);
            t.setDaemon(true);
            t.setPriority(5);
            return t;
        }
    }

}
