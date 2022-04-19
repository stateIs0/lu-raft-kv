package cn.think.in.java.entity;

import cn.think.in.java.LogModule;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 日志条目
 *
 * @author 莫那·鲁道
 * @see LogModule
 */
@Data
@Builder
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
