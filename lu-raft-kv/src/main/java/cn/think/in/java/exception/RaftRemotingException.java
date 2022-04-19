package cn.think.in.java.exception;

/**
 *
 */
public class RaftRemotingException extends RuntimeException {

    public RaftRemotingException() {
        super();
    }

    public RaftRemotingException(String message) {
        super(message);
    }

    public RaftRemotingException(Throwable cause) {
        super(cause);
    }

    public RaftRemotingException(String message, Throwable cause) {
        super(message, cause);
    }
}
