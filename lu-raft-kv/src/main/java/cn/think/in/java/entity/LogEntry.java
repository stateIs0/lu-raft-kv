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
package cn.think.in.java.entity;

import cn.think.in.java.LogModule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 日志条目
 *
 * @author 莫那·鲁道
 * @see LogModule
 */
@Data
@Builder
@AllArgsConstructor
public class LogEntry implements Serializable, Comparable {

    private Long index;

    private long term;

    private Command command;

    @Override
    public int compareTo(Object o) {
        if (o == null) {
            return -1;
        }
        if (this.getIndex() > ((LogEntry) o).getIndex()) {
            return 1;
        }
        return -1;
    }



}
