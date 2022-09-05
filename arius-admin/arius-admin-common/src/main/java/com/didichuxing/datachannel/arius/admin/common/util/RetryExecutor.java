package com.didichuxing.datachannel.arius.admin.common.util;

import com.didichuxing.datachannel.arius.admin.common.exception.BaseException;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class RetryExecutor<T> {
    private static final ILog LOGGER     = LogFactory.getLog(RetryExecutor.class);

    /**
     * 最多的重试次数
     */
    private static final int  RETRY_MAX  = 10;
    /**
     * 操作名字
     */
    private String            name       = "";
    /**
     * es操作内容
     */
    private Handler<T>        handler;
    /**
     * 重试次数
     */
    private Integer           retryCount = 0;

    /**
     * es操作
     * Created by d06679 on 2017/8/24.
     */

    public interface Handler<T> {
        /**
         * 处理方法
         * @return
         * @throws Throwable
         */
        T process() throws BaseException;

        /***
         * 异常是否需要重试
         * @param e 异常
         * @return
         */
        default boolean needExceptionRetry(Exception e) {
            return false;
        }

        /**
         * 返回值是否需要重试
         * @param t
         * @return
         */
        default boolean needReturnObjRetry(Object t) {
            return false;
        }

        /**
         * 重试Sleep时间间隔
         * @param retryTimes 重试次数
         * @return
         */
        default int retrySleepTime(int retryTimes) {
            return 0;
        }
    }

    public static <T> RetryExecutor<T> builder() {
        return new RetryExecutor();
    }

    public RetryExecutor<T> name(String name) {
        this.name = name;
        return this;
    }

    public RetryExecutor<T> handler(Handler<T> handler) {
        this.handler = handler;
        return this;
    }

    public RetryExecutor<T> retryCount(Integer retryCount) {
        this.retryCount = (retryCount > RETRY_MAX) ? RETRY_MAX : retryCount;
        return this;
    }

    /**
     * 重试操作，要么handler执行成功有返回值,要么报异常
     * @throws Exception 操作的异常
     */
    public T execute() throws Exception {
        T t = null;
        int tryCount = 0;
        do {
            try {
                int retrySleepTime = handler.retrySleepTime(tryCount);
                if (tryCount > 0 && retrySleepTime > 0) {
                    TimeUnit.MILLISECONDS.sleep(retrySleepTime);
                }

                t = handler.process();

                if(!handler.needReturnObjRetry(t)){
                    return t;
                }
            } catch (Exception e) {
                if (!handler.needExceptionRetry(e) || tryCount == retryCount) {
                    LOGGER.warn("class=RetryExecutor||method=execute||errMsg={}||handlerName={}||tryCount={}",
                            e.getMessage(), name, tryCount, e);
                    throw e;
                }
               
                LOGGER.warn(
                        "class=RetryExecutor||method=execute||errMsg={}||handlerName={}||tryCount={}||maxTryCount={}",
                        e.getMessage(), name, tryCount, retryCount);
            }
        } while (tryCount++ < retryCount);

        return t;
    }
}