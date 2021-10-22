package com.didichuxing.datachannel.arius.admin.task;

import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.threadpool.AriusTaskThreadPool;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.ModuleEnum.SCHEDULE;
import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum.EDIT;
import static com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUser.SYSTEM;

/**
 * base任务 模板并发处理
 * @author d06679
 * @date 2019/3/21
 */
public abstract class BaseConcurrentTask {

    private static final ILog     LOGGER = LogFactory.getLog(BaseConcurrentTask.class);

    @Autowired
    protected AriusTaskThreadPool ariusTaskThreadPool;

    @Autowired
    private OperateRecordService  operateRecordService;

    private CountDownLatch        countDownLatch;

    private volatile boolean      succ   = true;

    /**
     * 并发处理任务
     */
    public boolean execute() {
        long start = System.currentTimeMillis();

        List<TaskBatch> batches = splitBatch(getAllItems());

        if (CollectionUtils.isEmpty(batches)) {
            LOGGER.warn("batches is empty||task={}", getTaskName());
            return true;
        }

        LOGGER.info("ConcurrentClusterTask||msg=task start||task={}||batchSize={}", getTaskName(), batches.size());

        countDownLatch = new CountDownLatch(batches.size());
        succ = true;

        for (TaskBatch taskBatch : batches) {
            ariusTaskThreadPool.run(() -> {
                try {
                    if (!executeByBatch(taskBatch)) {
                        succ = false;
                    }
                } catch (Exception e) {
                    succ = false;
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
                // do nothing.
            }
        }

        try {
            //等待所有任务完成
            if (countDownLatch.await(60L, TimeUnit.MINUTES)) {
                LOGGER.info("ConcurrentClusterTask||msg=all task finish||task={}", getTaskName());
            } else {
                LOGGER.warn("ConcurrentClusterTask||msg=has task time out||task={}", getTaskName());
            }

            //记录任务的完成任务时间
            long cost = System.currentTimeMillis() - start;
            operateRecordService.save(SCHEDULE, EDIT, getTaskName(), getTaskName() + "完成，耗时：" + cost, SYSTEM.getDesc());
        } catch (Exception e) {
            LOGGER.warn("ConcurrentClusterTask Interrupted||task={}", getTaskName(), e);
        }

        return succ;
    }

    /**
     * 任务全集
     * @return
     */
    protected abstract List getAllItems();

    /**
     * 处理一个批次任务
     * @param taskBatch
     */
    protected abstract boolean executeByBatch(TaskBatch taskBatch) throws AdminOperateException;

    /**
     * 获取任务名称
     * @return 任务名称
     */
    public abstract String getTaskName();

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
    private List<TaskBatch> splitBatch(List allItems) {

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

        LOGGER.info("ConcurrentClusterTask||msg=splitBatch||task={}||workerCount={}||batchSize={}", getTaskName(),
            workerCount, batchSize);

        List<TaskBatch> taskBatches = Lists.newArrayList();
        TaskBatch batch = new TaskBatch();
        for (int i = 0; i < allItems.size(); i++) {
            if (batch.getItems().size() >= batchSize) {
                taskBatches.add(batch);
                batch = new TaskBatch();
            }
            batch.getItems().add(allItems.get(i));
        }

        if (batch.getItems().size() > 0) {
            taskBatches.add(batch);
        }

        return taskBatches;
    }

}
