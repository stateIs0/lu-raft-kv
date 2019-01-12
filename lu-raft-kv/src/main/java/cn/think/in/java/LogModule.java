package cn.think.in.java;

import cn.think.in.java.entity.LogEntry;

/**
 *
 * @see cn.think.in.java.entity.LogEntry
 * @author 莫那·鲁道
 */
public interface LogModule {

    void write(LogEntry logEntry);

    LogEntry read(Long index);

    void removeOnStartIndex(Long startIndex);

    LogEntry getLast();

    Long getLastIndex();
}
