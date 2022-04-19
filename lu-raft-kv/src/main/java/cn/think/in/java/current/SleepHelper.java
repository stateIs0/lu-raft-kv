package cn.think.in.java.current;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 *
 * @author 莫那·鲁道
 */
public class SleepHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(SleepHelper.class);


    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            LOGGER.warn(e.getMessage());
        }

    }

    public static void sleep2(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            LOGGER.warn(e.getMessage());
        }

    }


}
