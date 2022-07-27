package com.didichuxing.datachannel.arius.admin.task.template.srv;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.indexplan.IndexPlanManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.task.BaseConcurrentTemplateTask;
import com.didiglobal.logi.job.annotation.Task;
import com.didiglobal.logi.job.common.TaskResult;
import com.didiglobal.logi.job.core.job.Job;
import com.didiglobal.logi.job.core.job.JobContext;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author chengxiang, jiamin
 * 每天凌晨3点执行
 * 模版shard动态调整能力，目标是为了调控索引主shard的个数在合适的大小，因为索引创建后就不能改变主shard，所以影响的是后天的创建的索引主shard个数
 * 为什么是后天呢？假如某一时刻索引模版的主shardCnt更变了，但由于索引预创建定时任务是一小时执行一次，所以第二天的索引早就早已被创建，这个时候新的shardCnt只能到后天才能落实到具体的索引中
 * 为了优化这个点，就是想要明天的索引就能使用最新的shardCnt，我们就要删除明天已经被创建出来的索引
 *
 * 按天滚动的模版：
 *    获取所有物理模版，判断是否存在主从（先改主，再改从）
 *    计算物理模版合适的shard个数，获取近七天（今天是10-22，则获取10-15~10-21的索引），某一天的索引可能会有多个版本（则累加汇做一天）
 *    取这近七天，某一天索引占用磁盘容量的最大值，用于计算shard，如果计算出的shard与原来的不一致，则对模版进行修改。
 * 非滚动的模版：
 *    不动态调整shard，首先模版就只对应一个索引（并且再创建该模版的时候，计算主shard的个数为diskQuota/50G）
 * 按月滚动的模版：
 *    不动态调整shard
 */
@Task(name = "ShardNumAdjustRandomTask", description = "shard规划任务", cron = "0 0 3 1/1 * ? *", autoRegister = true)
public class ShardNumAdjustRandomTask extends BaseConcurrentTemplateTask implements Job {

    private static final ILog LOGGER = LogFactory.getLog(ShardNumAdjustRandomTask.class);

    @Autowired
    private IndexPlanManager  indexPlanManager;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=ShardNumAdjustRandomTask||method=execute||msg=ShardNumAdjustRandomTask start");
        if (execute()) {
            return TaskResult.SUCCESS;
        }
        return TaskResult.FAIL;
    }

    

    @Override
    public int poolSize() {
        return 10;
    }

    @Override
    public int current() {
        return 5;
    }
    
    @Override
    protected boolean executeByLogicTemplate(Integer logicId) {
        try {
            final Result<Boolean> result = indexPlanManager.adjustShardNum(logicId);
            if (Boolean.FALSE.equals(result.getData())) {
                LOGGER.warn("class=ShardNumAdjustRandomTask||method=executeByLogicTemplate||logicId={}||msg={}", logicId,
                        result.getMessage());
            }
        } catch (Exception e) {
            LOGGER.error("class=ShardNumAdjustRandomTask||method=executeByLogicTemplate||logicId={}||msg=shard规划任务", logicId,
                    e);
        }
        
        return Boolean.TRUE;
    }
}