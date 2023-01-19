package com.didi.cloud.fastdump.common.component.threadmodel;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.didi.cloud.fastdump.common.threadpool.FutureUtil;

/**
 * Created by linyunan on 2022/9/22
 *
 */
@Component
public class LuceneMoveTaskThreadPoolModel implements MoveTaskThreadPoolModel<Void, FutureUtil<Void>> {
    private static final Logger                         LOGGER = LoggerFactory
        .getLogger(LuceneMoveTaskThreadPoolModel.class);

    @Value("${node.concurrent.Handle.shard.num:5}")
    public int                                          nodeConcurrentHandleShardNum;

    @Value("${index.bulk.thread.pool.size:50}")
    public int                                          singleBulkSize;

    @Value("${index.bulk.thread.pool.queue:10000}")
    public int                                          queueSize;

    /**
     * 执行segment-bulk任务池子
     */
    private static ArrayBlockingQueue<FutureUtil<Void>> segmentBulkFutureUtilArrayBlockingQueue;

    @PostConstruct
    private void init() {
        // 公平阻塞队列, 先来先运行
        segmentBulkFutureUtilArrayBlockingQueue = new ArrayBlockingQueue(nodeConcurrentHandleShardNum, true);
        for (int i = 0; i < nodeConcurrentHandleShardNum; i++) {
            segmentBulkFutureUtilArrayBlockingQueue
                .add(FutureUtil.init(String.format("Bulk-FutureUtil-%s", i), singleBulkSize, singleBulkSize, queueSize));
        }
    }

    @Override
    public FutureUtil<Void> fetch(Void params) throws InterruptedException {
        return segmentBulkFutureUtilArrayBlockingQueue.take();
    }

    @Override
    public void release(FutureUtil<Void> futureUtil) {
        if (null == futureUtil) { return;}
        segmentBulkFutureUtilArrayBlockingQueue.offer(futureUtil);
    }

    public static void main(String[] args) throws InterruptedException {
        FutureUtil init = FutureUtil.init(String.format("Bulk-FutureUtil-%s", 1), 20, 20, 10);

        for (int i = 0; i < 100; i++) {
            int count = 0;
            while (true) {
                try {
                    init.runnableTask(() -> {
                        try {
                            Thread.sleep(1000000L);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    });
                } catch (Exception e) {
                    if (e instanceof RejectedExecutionException) {
                        count++;
                        System.out.println(String.format("重试次数:%s", count));
                        Thread.sleep(3000);
                    }
                }
            }
        }

        System.out.println();

        LuceneMoveTaskThreadPoolModel luceneMoveTaskThreadPoolModel = new LuceneMoveTaskThreadPoolModel();
        luceneMoveTaskThreadPoolModel.init();
        FutureUtil<Object> test = FutureUtil.init("test", 20, 20, 500);
        for (int i = 0; i < 10; i++) {
            test.runnableTask(() -> {
                FutureUtil<Void> futureUtil = null;
                try {
                    futureUtil = luceneMoveTaskThreadPoolModel.fetch(null);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                futureUtil.runnableTask(() -> {
                    try {
                        Thread.sleep(20000L);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
                futureUtil.waitExecuteUnlimited();
                luceneMoveTaskThreadPoolModel.release(futureUtil);
            });
        }

        Thread.sleep(3000);

        for (int i = 0; i < 5; i++) {
            test.runnableTask(() -> {
                FutureUtil<Void> futureUtil = null;
                try {
                    long currentTimeMillis = System.currentTimeMillis();
                    futureUtil = luceneMoveTaskThreadPoolModel.fetch(null);
                    System.out.println(System.currentTimeMillis() - currentTimeMillis);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                futureUtil.runnableTask(() -> {
                    int count = 10;
                    try {
                        while (count-- > 0) {
                            Thread.sleep(5000);
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
                futureUtil.waitExecuteUnlimited();
                luceneMoveTaskThreadPoolModel.release(futureUtil);
            });
        }

        test.waitExecuteUnlimited();
    }
}
