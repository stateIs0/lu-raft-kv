package cn.think.in.java.rpc;

import lombok.Data;

import java.io.Serializable;

@Data
public class MyResponse<T> implements Serializable {
    private T res;
}
