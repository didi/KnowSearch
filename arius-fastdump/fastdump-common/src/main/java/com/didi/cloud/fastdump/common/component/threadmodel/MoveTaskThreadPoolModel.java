package com.didi.cloud.fastdump.common.component.threadmodel;

/**
 * Created by linyunan on 2022/9/25
 */
public interface MoveTaskThreadPoolModel<Param, RunningThreadPool> {

    /**
     * 根据指定条件获取任务线程池
     * @param params  条件型参
     * @return        RunningThreadPool
     * @throws Exception
     */
    RunningThreadPool fetch(Param params) throws Exception;

    /**
     * 释放任务线程池
     * @param runningThreadPool   RunningThreadPool
     * @throws Exception
     */
    void release(RunningThreadPool runningThreadPool) throws Exception;
}
