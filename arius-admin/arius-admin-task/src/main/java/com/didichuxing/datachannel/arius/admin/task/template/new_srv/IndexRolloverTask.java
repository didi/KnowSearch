package com.didichuxing.datachannel.arius.admin.task.template.new_srv;

import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.indexplan.IndexPlanManager;
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
 *
 * rollover能力，目标是保证单个索引的大小在合适的范围内（即索引的主shard数 * 50G），一小时执行一次、或者更频繁的执行
 * 获取主shard个数，只能从ES中获取，不能从数据库表获取
 * 按天滚动的模版：
 *    如果当天索引主shard占用磁盘大小小于主shardCnt*30G，则不进行升级版本
 *    如果当天索引主shard占用磁盘大小大于主shardCnt*50G，则直接升级版本
 *    否则为了应对突发的数据大量递增（一般都是业务方的索引会出现这种情况），如果当天的索引大小超过了过去7天索引大小的最大值
 *    则需要做好预防措施，创建当天新的高版本索引（同时升级该索引对应模版的版本）。
 * 按月滚动的模版：
 *    如果当天索引主shard占用磁盘大小大于主shardCnt*50G，则直接升级版本，其他情况不升级版本
 * 不滚动的模版：
 *    如果当天索引主shard占用磁盘大小大于主shardCnt*50G，则直接升级版本，其他情况不升级版本
 *
 * xxxx-2021-10-22 -> xxxx-2021-10-22_v1
 */
@Task(name = "IndexRolloverTask", description = "模板Rollover实现", cron = "0 0 0/1 * * ?", autoRegister = true)
public class IndexRolloverTask extends BaseConcurrentTemplateTask implements Job {

    private static final ILog LOGGER = LogFactory.getLog(IndexRolloverTask.class);

    @Autowired
    private IndexPlanManager indexPlanManager;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=IndexRolloverTask||method=execute||msg=IndexRolloverTask start");
        if (execute()) {
            return TaskResult.SUCCESS;
        }
        return TaskResult.FAIL;
    }

    @Override
    public String getTaskName() {
        return "IndexRolloverTask";
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
        return indexPlanManager.indexRollover(logicId).success();
    }
}
