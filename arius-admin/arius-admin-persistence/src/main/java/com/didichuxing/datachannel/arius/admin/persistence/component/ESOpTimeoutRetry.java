package com.didichuxing.datachannel.arius.admin.persistence.component;

import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.BaseException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.RetryExecutor;
import com.didichuxing.datachannel.arius.admin.common.util.RetryExecutor.HandlerWithReturnValue;
import java.util.concurrent.TimeUnit;
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
    private static final int SEC_30 = 30 * 1000;
    private static final int MIN_5  = 5 * 60 * 1000;
    private static final int MAX_10_SECONDS  = 10;
    

    private ESOpTimeoutRetry() {
    }

    public static boolean esRetryExecute(String methodName, int tryCount,
                                         RetryExecutor.Handler handler) throws ESOperateException {
        try {
            return RetryExecutor.builder().name(methodName).retryCount(tryCount).handler(new RetryExecutor.Handler() {
                @Override
                public boolean process() throws BaseException {
                    return handler.process();
                }

                @Override
                public boolean needRetry(Exception e) {
                    return e instanceof ProcessClusterEventTimeoutException
                           || e instanceof ElasticsearchTimeoutException ;
                }
    
                /**
                 * 如果您不需要抛出异常，请不要抛出它们。
                 *
                 * @param e 抛出的异常。
                 * @return 正在返回默认方法。
                 */
                @Override
                public boolean needToThrowExceptions(Exception e) {
                    return e instanceof AdminOperateException;
                }
    
                @Override
                public int retrySleepTime(int retryTimes) {
                    int totalSleepTime = RandomUtils.nextInt(0, retryTimes);
                    return (int) TimeUnit.SECONDS.toMillis(Math.max(totalSleepTime, MAX_10_SECONDS));
                }
            }).execute();
        } catch (ESOperateException e) {
            throw new ESOperateException(e.getMessage(),e.getCause());
        }catch (Exception e){
             throw new ESOperateException(e.getMessage(), e);
        }
    }

    
    public static <T> T esRetryExecuteWithReturnValue(String methodName, int tryCount,
                                         RetryExecutor.HandlerWithReturnValue<T> handlerWithReturnValue,Predicate<T> predicate) throws ESOperateException {
        try {
            final RetryExecutor<T> retryExecutor = RetryExecutor.builder().<T>name(methodName).retryCount(tryCount)
                    .HandlerWithReturnValue(new HandlerWithReturnValue<T>() {
                        @Override
                        public T process() throws BaseException {
                            return handlerWithReturnValue.process();
                        }
                
                        @Override
                        public boolean needRetry(Exception e) {
                            return e instanceof ProcessClusterEventTimeoutException
                                   || e instanceof ElasticsearchTimeoutException;
                        }
    
                        /**
                         * 如果您不需要抛出异常，请不要抛出它们。
                         *
                         * @param e 抛出的异常。
                         * @return 正在返回默认方法。
                         */
                        @Override
                        public boolean needToThrowExceptions(Exception e) {
                            return e instanceof AdminOperateException;
                        }
    
                        @Override
                        public int retrySleepTime(int retryTimes) {
                            int totalSleepTime = RandomUtils.nextInt(0, retryTimes);
                            return (int) TimeUnit.SECONDS.toMillis(Math.max(totalSleepTime, MAX_10_SECONDS));
                        }
                    });
            return retryExecutor.execute(predicate);
    
        } catch (ESOperateException e) {
            throw new ESOperateException(e.getMessage(),e.getCause());
        }catch (Exception e){
             throw new ESOperateException(e.getMessage(), e);
        }
    }
    /**
     * 定制重试方法等待的时间
     * @param methodName 方法名称
     * @param tryCount 重试次数
     * @param handler 重试的操作
     * @param retrySleepTime 重试间隔的等待时间
     * @return 整个重试方法执行的结果
     * @throws ESOperateException 抛异常
     */
    public static boolean esRetryExecuteWithGivenTime(String methodName, int tryCount, RetryExecutor.Handler handler,
                                                      Function<Integer, Integer> retrySleepTime) throws ESOperateException {
        try {
            return RetryExecutor.builder().name(methodName).retryCount(tryCount).handler(new RetryExecutor.Handler() {
                @Override
                public boolean process() throws BaseException {
                    return handler.process();
                }

                @Override
                public boolean needRetry(Exception e) {
                    return e instanceof ProcessClusterEventTimeoutException
                           || e instanceof ElasticsearchTimeoutException || e instanceof AdminOperateException;
                }

                @Override
                public int retrySleepTime(int retryTimes) {
                    return retrySleepTime.apply(retryTimes);
                }
            }).execute();
        } catch (Exception e) {
            throw new ESOperateException(e.getMessage(), e);
        }
    }
}