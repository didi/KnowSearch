package com.didichuxing.datachannel.arius.admin.common;

import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.function.BiFunctionWithESOperateException;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 重试跑龙套
 *
 * @author shizeying
 * @date 2022/08/01
 */
public final class RetryUtils {
       public static final Integer ES_OPERATE_MIN_TIMEOUT           = 10;
        /**
     * 重试操作的方法体
     *
     * @param tryTimes     试次
     * @param esClientFunc ES客户端函数
     * @param doWhilePredicate   do while的另一种判断模式
     * @return {@code T}
     */
    public static  <T> T performTryTimesMethods(BiFunctionWithESOperateException<Long, TimeUnit,T> esClientFunc,
                                                Predicate<T> doWhilePredicate, Integer tryTimes) throws ESOperateException {
        Long minTimeoutNum = 1L;
        Long maxTimeoutNum = tryTimes.longValue();
        T t = null;
        do {
            t = esClientFunc.apply(/*降低因为抖动导致的等待时常,等待时常从低到高进行重试*/minTimeoutNum * ES_OPERATE_MIN_TIMEOUT,
                    TimeUnit.SECONDS);
            minTimeoutNum++;
            if (minTimeoutNum > maxTimeoutNum) {
                minTimeoutNum = maxTimeoutNum;
            }
        } while (tryTimes-- > 0 && doWhilePredicate.test(t));
        
        return t;
    }
            /**
     * 重试操作的方法体
     *
     * @param tryTimes     试次
     * @param esClientFunc ES客户端函数
     * @param doWhilePredicate   do while的另一种判断模式
     * @return {@code T}
     */
    public static  <T> T performTryTimesMethods(BiFunction<Long, TimeUnit,T> esClientFunc,
                                           Predicate<T> doWhilePredicate, Integer tryTimes) {
        Long minTimeoutNum = 1L;
        Long maxTimeoutNum = tryTimes.longValue();
        T t = null;
        do {
            t = esClientFunc.apply(/*降低因为抖动导致的等待时常,等待时常从低到高进行重试*/minTimeoutNum * ES_OPERATE_MIN_TIMEOUT,
                    TimeUnit.SECONDS);
            minTimeoutNum++;
            if (minTimeoutNum > maxTimeoutNum) {
                minTimeoutNum = maxTimeoutNum;
            }
        } while (tryTimes-- > 0 && doWhilePredicate.test(t));
        
        return t;
    }
    public static <T> T performTryTimesMethods(Supplier<T> esClientFunc, Predicate<T> doWhilePredicate, Integer tryTimes) {
        T t = null;
        do {
            t = esClientFunc.get();
        } while (tryTimes-- > 0 && doWhilePredicate.test(t));
        
        return t;
    }
}