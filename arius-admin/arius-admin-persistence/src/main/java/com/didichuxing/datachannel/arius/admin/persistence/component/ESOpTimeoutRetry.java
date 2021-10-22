package com.didichuxing.datachannel.arius.admin.persistence.component;

import org.elasticsearch.ElasticsearchTimeoutException;
import org.elasticsearch.cluster.metadata.ProcessClusterEventTimeoutException;

import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.RetryExecutor;

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
    private final static int MIN_5  = 5 * 60 * 1000;
    private final static int MIN_30 = 30 * 60 * 1000;

    public static boolean esRetryExecute(String methodName, int tryCount,
                                         RetryExecutor.Handler handler) throws ESOperateException {
        try {
            return RetryExecutor.builder().name(methodName).retryCount(tryCount).handler(new RetryExecutor.Handler() {
                @Override
                public boolean process() throws Throwable {
                    return handler.process();
                }

                @Override
                public boolean needRetry(Throwable e) {
                    return e instanceof ProcessClusterEventTimeoutException
                           || e instanceof ElasticsearchTimeoutException;
                }

                @Override
                public int retrySleepTime(int retryTims){
                    int sleepTime       = retryTims * MIN_5;
                    int randomSleepTime = (int)(Math.random() * 100);
                    int totalSleepTime  = sleepTime + randomSleepTime;

                    return totalSleepTime > MIN_30 ? MIN_30 : totalSleepTime;
                }
            }).execute();
        } catch (Throwable e) {
            throw new ESOperateException(e.getMessage(), e);
        }
    }
}
