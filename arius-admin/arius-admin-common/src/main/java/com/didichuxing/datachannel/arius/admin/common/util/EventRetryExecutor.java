package com.didichuxing.datachannel.arius.admin.common.util;

import com.didichuxing.datachannel.arius.admin.common.exception.BaseException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;

import java.util.function.Predicate;

/**
 * 事件重试器
 *  1、重试的睡眠时间为10ms
 *  2、若事件没有操作成功则会重试，重试次数为3次，超过3次不再重试
 *  3、事件重试器会对异常进行判断，若是ESOperateException会继续进行重试，若不是该异常或者超过重试次数出现异常则抛出
 *  4、操作返回true,这里直接返回true
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
    public static <T> T eventRetryExecute(String methodName, RetryExecutor.Handler<T> handler) throws ESOperateException {
        return eventRetryExecute(methodName, handler, t -> false);
    }

    /**
     * 定制事件重试方法，根据事件的返回值来判断是否需要重试
     */
    public static <T> T eventRetryExecute(String methodName, RetryExecutor.Handler<T> handler,
                                          Predicate<T> retNeedRetry) throws ESOperateException {
        return eventRetryExecuteInner(methodName, handler, retNeedRetry);
    }

    /**************************************** private method ***************************************************/
    private static <T> T eventRetryExecuteInner(String methodName, RetryExecutor.Handler<T> handler,
                                             Predicate<T> retNeedRetry) throws ESOperateException {
        try {
            final RetryExecutor<T> retryExecutor = RetryExecutor.builder().name(methodName).retryCount(RETRY_COUNT)
                    .handler(new RetryExecutor.Handler() {
                        @Override
                        public T process() throws BaseException {
                            return handler.process();
                        }

                        @Override
                        public boolean needExceptionRetry(Exception e) {
                            return e instanceof ESOperateException;
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
        } catch (ESOperateException e) {
            throw new ESOperateException(e.getMessage(),e.getCause());
        }catch (Exception e){
            throw new ESOperateException(e.getMessage(), e);
        }
    }
}
