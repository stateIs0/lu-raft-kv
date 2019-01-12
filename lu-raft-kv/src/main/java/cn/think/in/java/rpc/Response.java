package cn.think.in.java.rpc;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author 莫那·鲁道
 */
@Getter
@Setter
public class Response<T> implements Serializable {


    private T result;

    public Response(T result) {
        this.result = result;
    }

    private Response(Builder builder) {
        setResult((T) builder.result);
    }

    public static Response ok() {
        return new Response<>("ok");
    }

    public static Response fail() {
        return new Response<>("fail");
    }

    public static Builder newBuilder() {
        return new Builder();
    }


    @Override
    public String toString() {
        return "Response{" +
            "result=" + result +
            '}';
    }

    public static final class Builder {

        private Object result;

        private Builder() {
        }

        public Builder result(Object val) {
            result = val;
            return this;
        }

        public Response build() {
            return new Response(this);
        }
    }
}
