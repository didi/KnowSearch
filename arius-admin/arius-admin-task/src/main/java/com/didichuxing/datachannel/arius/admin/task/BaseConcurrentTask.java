package com.didichuxing.datachannel.arius.admin.task;

import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.threadpool.AriusTaskThreadPool;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.PostConstruct;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * base任务 模板并发处理
 * @author d06679
 * @date 2019/3/21
 */
public abstract class BaseConcurrentTask<T> {

    private static final ILog     LOGGER     = LogFactory.getLog(BaseConcurrentTask.class);
    private static final int      QUEUE_SIZE = 50000;

    protected AriusTaskThreadPool ariusTaskThreadPool;

    @Autowired
    private OperateRecordService  operateRecordService;

    @PostConstruct
    public void init() {
        ariusTaskThreadPool = new AriusTaskThreadPool();
        ariusTaskThreadPool.init(poolSize(), getTaskName(), QUEUE_SIZE);
    }

    /**
     * 并发处理任务
     */
    public boolean execute() {
        long start = System.currentTimeMillis();

        List<TaskBatch<T>> batches = splitBatch(getAllItems());
        int currentQueueWorkerSize = ariusTaskThreadPool.getCurrentQueueWorkerSize();
        if (currentQueueWorkerSize >= QUEUE_SIZE) {
            LOGGER.warn(
                "class=BaseConcurrentTask||method=execute||msg=currentQueueWorkerSize:{} is more than thread pool queue size:{}",
                getTaskName(), currentQueueWorkerSize, QUEUE_SIZE);
            return false;
        }
        if (CollectionUtils.isEmpty(batches)) {
            LOGGER.warn("class=BaseConcurrentTask||method=execute||batches is empty||task={}", getTaskName());
            return true;
        }

        LOGGER.info(
            "class=BaseConcurrentTask||method=execute||ConcurrentClusterTask||msg=task start||task={}||batchSize={}",
            getTaskName(), batches.size());

        CountDownLatch countDownLatch = new CountDownLatch(batches.size());
        AtomicBoolean succ = new AtomicBoolean(true);

        for (TaskBatch<T> taskBatch : batches) {
            ariusTaskThreadPool.run(() -> {
                try {
                    if (!executeByBatch(taskBatch)) {
                        succ.set(false);
                    }
                } catch (Exception e) {
                    succ.set(false);
                    // 需要Odin监控错误日志
                    LOGGER.error("class=BaseConcurrentTask||method=execute||errMsg={}||task={}", e.getMessage(),
                        getTaskName(), e);
                } finally {
                    countDownLatch.countDown();
                }
            });

            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(TaskConcurrentConstants.SLEEP_SECONDS_PER_BATCH));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.warn("class=BaseConcurrentTask||method=execute||ConcurrentClusterTask Interrupted||task={}",
                    getTaskName(), e);
            }
        }

        try {
            //等待所有任务完成
            if (countDownLatch.await(60L, TimeUnit.MINUTES)) {
                LOGGER.info(
                    "class=BaseConcurrentTask||method=execute||ConcurrentClusterTask||msg=all task finish||task={}",
                    getTaskName());
            } else {
                LOGGER.warn(
                    "class=BaseConcurrentTask||method=execute||ConcurrentClusterTask||msg=has task time out||task={}",
                    getTaskName());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.warn("class=BaseConcurrentTask||method=execute||ConcurrentClusterTask Interrupted||task={}",
                getTaskName(), e);
        }
        try {
            //记录任务的完成任务时间
            long cost = System.currentTimeMillis() - start;
            //operateRecordService.save(new OperateRecord.Builder()
            //                .bizId(getTaskName())
            //                .content(getTaskName() + "完成，耗时：" + cost)
            //                .operationTypeEnum(OperateTypeEnum.SENSE_OP_EDIT)
            //                .triggerWayEnum(TriggerWayEnum.TIMING_TASK)
            //                .userOperation(SYSTEM.getDesc())
            //        .build());

        } catch (Exception e) {
            LOGGER.error("class=BaseConcurrentTask||method=execute||errMsg={}", e.getMessage(), e);
        }

        return succ.get();
    }

    /**
     * 任务全集
     * @return
     */
    protected abstract List<T> getAllItems();

    /**
     * 处理一个批次任务
     * @param taskBatch
     */
    protected abstract boolean executeByBatch(TaskBatch<T> taskBatch) throws AdminOperateException;

    /**
     * 获取任务名称
     * @return 任务名称
     */
    public  String getTaskName(){
        return getClass().getSimpleName();
    }

    /**
     * 任务的线程个数
     * @return
     */
    public abstract int poolSize();

    /**
     * 并发度
     * @return
     */
    public abstract int current();

    /**************************************************** private method ****************************************************/

    /**
     * 获取任务批次
     * @return
     */
    private List<TaskBatch<T>> splitBatch(List<T> allItems) {

        if (CollectionUtils.isEmpty(allItems)) {
            return Lists.newArrayList();
        }

        // 打乱顺序
        Collections.shuffle(allItems);

        int workerCount = Math.min(ariusTaskThreadPool.getPoolSize(), current());
        if (workerCount < 1) {
            workerCount = 1;
        }

        int batchSize = allItems.size() / workerCount + 1;

        LOGGER.info(
            "class=BaseConcurrentTask||method=splitBatch||ConcurrentClusterTask||msg=splitBatch||task={}||workerCount={}||batchSize={}",
            getTaskName(), workerCount, batchSize);

        List<TaskBatch<T>> taskBatches = Lists.newArrayList();
        TaskBatch<T> batch = new TaskBatch<>();
        for (int i = 0; i < allItems.size(); i++) {
            if (batch.getItems().size() >= batchSize) {
                taskBatches.add(batch);
                batch = new TaskBatch<>();
            }
            batch.getItems().add(allItems.get(i));
        }

        if (!batch.getItems().isEmpty()) {
            taskBatches.add(batch);
        }

        return taskBatches;
    }

}