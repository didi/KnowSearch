package com.didi.cloud.fastdump.common.utils;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.didi.cloud.fastdump.common.content.FastDumpConstant;

/**
 * Created by linyunan on 2022/10/14
 */
public class RetryUtil {
    public static final int       DEFAULT_TIME           = 3;
    public static final int       DEFAULT_INTERVAL_MILLS = 2000;
    protected static final Logger LOGGER                 = LoggerFactory.getLogger(RetryUtil.class);

    public RetryUtil() {}

    public interface RetryContent<T> {
        T retry() throws Exception;
    }

    public static <T> T retryWhenNullOrExceptionAndFailedReturnNull(String method, int retryTime, long intervalMills,
                                                                    RetryContent<T> handler) {
        int count = retryTime;
        while (count-- > 0) {
            T result;
            try {
                result = handler.retry();
            } catch (Exception e) {
                LOGGER.warn("class=RetryUtil||method={}||retryTime={}||errMsg=exception", method, retryTime - count, e);
                sleep(intervalMills);
                continue;
            }

            if (result != null) {
                return result;
            }
            LOGGER.warn("class=RetryUtil||method={}||retryTime={}||msg=result is null", method, retryTime - count);
            sleep(intervalMills);
        }
        return null;
    }

    public static <T> T retryWhenNullOrExceptionAndFailedThrowRuntimeException(String method, int retryTime,
                                                                               long intervalMills,
                                                                               RetryContent<T> handler) {
        int count = retryTime;
        String exceptionMsg = "";
        while (count-- > 0) {
            T result;
            try {
                result = handler.retry();
            } catch (Exception e) {
                LOGGER.warn("class=RetryUtil||method={}||retryTime={}||errMsg=exception", method, retryTime - count, e);
                sleep(intervalMills);
                if (count == 0) {
                    exceptionMsg = StringUtils.substring(e.getMessage(), 0, 2000);
                }
                continue;
            }
            if (result != null) {
                return result;
            }
            LOGGER.warn("class=RetryUtil||method={}||retryTime={}||msg=result is null", method, retryTime - count);
            sleep(intervalMills);
        }

        if (StringUtils.isNotBlank(exceptionMsg)) {
            throw new RuntimeException(String.format("method=%s||errMsg=failed to retry, detail:%s", method, exceptionMsg));
        }
        return  null;
    }

    public static <T> T retryWhenExceptionAndFailedThrowRuntimeException(String method, int retryTime,
                                                                         long intervalMills, RetryContent<T> handler) {
        int count = retryTime;
        String exceptionMsg = "";
        while (count-- > 0) {
            try {
                return handler.retry();
            } catch (Exception e) {
                LOGGER.warn("class=RetryUtil||method={}||retryTime={}||exceptionMsg=exception", method, retryTime - count, e);
                sleep(intervalMills);

                if (count == 0) {
                    exceptionMsg = StringUtils.substring(e.getMessage(), 0, FastDumpConstant.ERR_MSG_LENGTH);
                }
            }
        }

        if (StringUtils.isNotBlank(exceptionMsg)) {
            throw new RuntimeException(String.format("method=%s||errMsg=failed to retry, detail:%s", method, exceptionMsg));
        }
        return  null;
    }

    public static <T> T retryWhenExceptionAndFailedReturnNull(RetryContent<T> handler, String method, int retryTime,
                                                              long intervalMills) {
        int count = retryTime;
        while (count-- > 0) {
            try {
                return handler.retry();
            } catch (Exception e) {
                LOGGER.warn("class=RetryUtil||method={}||retryTime={}||errMsg=exception", method, retryTime - count, e);
                sleep(intervalMills);
            }
        }
        return null;
    }

    private static void sleep(long intervalMills) {
        try {
            TimeUnit.MILLISECONDS.sleep(intervalMills);
        } catch (InterruptedException ignored) {
        }
    }

    public static void main(String[] args) {
        Integer num2 = RetryUtil.retryWhenExceptionAndFailedThrowRuntimeException("test2", 3, 2000, () -> {
            int n = new Random().nextInt(6);
            System.out.println("产生随机数:" + n);
            if (n <= 5) {
                throw new RuntimeException("小于5异常");
            } else {
                return n;
            }
        });
        System.out.println(num2);

        Integer num1 = RetryUtil.retryWhenNullOrExceptionAndFailedReturnNull("test1", 3, 2000, () -> {
            int n = new Random().nextInt(6);

            System.out.println("产生随机数:" + n);

            return n > 5 ? n : null;

        });
        System.out.println("获得随机数" + num1);
    }

}
