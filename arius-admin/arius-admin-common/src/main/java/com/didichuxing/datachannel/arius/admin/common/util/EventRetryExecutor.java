package com.didichuxing.datachannel.arius.admin.common.util;

import com.didichuxing.datachannel.arius.admin.common.exception.BaseException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.EventException;

import java.util.function.Predicate;

/**
 * 事件重试器
 *  1、重试的睡眠时间为10ms
 *  2、操作返回false,这里直接返回false，表示操作没有成功
 *  3、若事件没有操作成功则会重试，重试次数为3次，超过3次不再重试
 *  4、事件重试器会对所有异常进行判断，若为异常则重试，超过重试次数仍然出现异常则抛出
 *  5、操作返回true,这里直接返回true，不再重试
 *
 * @author wx
 * @date 2022/10/13
 */
public class EventRetryExecutor {
    private static final int RETRY_TIMES = 10;
    private static final int RETRY_COUNT = 3;

    private EventRetryExecutor() {
    }

    /**
     * 定制事件重试方法，不对返回判断重试
     */
    public static <T> T eventRetryExecute(String methodName, RetryExecutor.Handler<T> handler) throws EventException {
        return eventRetryExecute(methodName, handler, t -> false);
    }

    /**
     * 定制事件重试方法，根据事件的返回值来判断是否需要重试
     */
    public static <T> T eventRetryExecute(String methodName, RetryExecutor.Handler<T> handler,
                                          Predicate<T> retNeedRetry) throws EventException {
        return eventRetryExecuteInner(methodName, handler, retNeedRetry);
    }

    /**************************************** private method ***************************************************/
    private static <T> T eventRetryExecuteInner(String methodName, RetryExecutor.Handler<T> handler,
                                             Predicate<T> retNeedRetry) throws EventException {
        try {
            final RetryExecutor<T> retryExecutor = RetryExecutor.builder().name(methodName).retryCount(RETRY_COUNT)
                    .handler(new RetryExecutor.Handler() {
                        @Override
                        public T process() throws BaseException {
                            return handler.process();
                        }

                        @Override
                        public boolean needExceptionRetry(Exception e) {
                            return e instanceof Exception;
                        }

                        @Override
                        public boolean needReturnObjRetry(Object t) {
                            return retNeedRetry.test((T)t);
                        }

                        @Override
                        public int retrySleepTime(int retryTimes) {
                            return RETRY_TIMES;
                        }
                    });
            return retryExecutor.execute();
        } catch (Exception e){
            throw new EventException(e.getMessage(), e);
        }
    }
}
