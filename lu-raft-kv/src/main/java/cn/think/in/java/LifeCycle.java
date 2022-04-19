package cn.think.in.java;

/**
 *
 * @author 莫那·鲁道
 */
public interface LifeCycle {

    /**
     * 初始化.
     * @throws Throwable
     */
    void init() throws Throwable;

    /**
     * 关闭资源.
     * @throws Throwable
     */
    void destroy() throws Throwable;
}
