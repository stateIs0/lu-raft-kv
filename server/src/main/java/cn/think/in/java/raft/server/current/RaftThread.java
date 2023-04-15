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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author 莫那·鲁道
 */
public class RaftThread extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(RaftThread.class);
    private static final UncaughtExceptionHandler uncaughtExceptionHandler = (t, e)
        -> LOGGER.warn("Exception occurred from thread {}", t.getName(), e);

    public RaftThread(String threadName,  Runnable r) {
        super(r, threadName);
        setUncaughtExceptionHandler(uncaughtExceptionHandler);
    }

}
