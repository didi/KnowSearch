package com.didichuxing.datachannel.arius.admin.persistence.component;

import com.didichuxing.datachannel.arius.admin.common.exception.BaseException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.NullESClientException;
import com.didichuxing.datachannel.arius.admin.common.util.RetryExecutor;
import java.util.function.Function;
import java.util.function.Predicate;
import org.apache.commons.lang3.RandomUtils;
import org.elasticsearch.ElasticsearchTimeoutException;
import org.elasticsearch.cluster.metadata.ProcessClusterEventTimeoutException;

/**
 * es操作器
 *  1、操作返回false,这里直接返回false
 *  2、超时重试对应的次数后,返回false
 *  3、操作抛出异常(非超时异常), 抛异常
 *  4、操作返回true,这里直接返回true
 *
 * @author d06679
 * @date 2017/8/24
 */
public class ESOpTimeoutRetry {
    private static final int SEC_1 = 1000;
    private static final int SEC_5 = 5000;

    private ESOpTimeoutRetry() {
    }

    /**
     * 定制重试方法，不对返回判断重试
     */
    public static <T> T esRetryExecute(String methodName, int tryCount,
                                       RetryExecutor.Handler<T> handler) throws ESOperateException {
        return esRetryExecute(methodName, tryCount, handler, t -> false);
    }

    /**
     * 定制重试方法，根据返回值来判断是否需要重试
     */
    public static <T> T esRetryExecute(String methodName, int tryCount,
                                       RetryExecutor.Handler<T> handler,
                                       Predicate<T> retNeedRetry) throws ESOperateException {
        return esRetryExecuteInner(methodName, tryCount, handler, retNeedRetry, retryTimes -> {
            int time = retryTimes * SEC_1 + RandomUtils.nextInt(0, 100);
            return Math.min(time, SEC_5);
        } );
    }

    /**
     * 定制重试方法等待的时间
     */
    public static <T> T esRetryExecuteWithGivenTime(String methodName, int tryCount,
                                                    RetryExecutor.Handler<T> handler,
                                                    Function<Integer, Integer> retrySleepTime) throws ESOperateException {
        return esRetryExecuteInner(methodName, tryCount, handler, t -> false, retrySleepTime);
    }

    /**************************************** private method ***************************************************/
    private static <T> T esRetryExecuteInner(String methodName, int tryCount,
                                       RetryExecutor.Handler<T> handler,
                                       Predicate<T> retNeedRetry,
                                       Function<Integer, Integer> retrySleepTime) throws ESOperateException {
        try {
            final RetryExecutor<T> retryExecutor = RetryExecutor.builder().name(methodName).retryCount(tryCount)
                    .handler(new RetryExecutor.Handler() {
                        @Override
                        public T process() throws BaseException {
                            return handler.process();
                        }

                        @Override
                        public boolean needExceptionRetry(Exception e) {
                            return e instanceof ProcessClusterEventTimeoutException
                                    || e instanceof ElasticsearchTimeoutException
                                    || e instanceof NullESClientException;
                        }

                        @Override
                        public boolean needReturnObjRetry(Object t) {
                            return retNeedRetry.test((T)t);
                        }

                        @Override
                        public int retrySleepTime(int retryTimes) {
                            return retrySleepTime.apply(retryTimes);
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