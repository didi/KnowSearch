package com.didichuxing.datachannel.arius.admin.task.template.srv;

import org.springframework.beans.factory.annotation.Autowired;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.indexplan.IndexPlanManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.task.BaseConcurrentTemplateTask;
import com.didiglobal.knowframework.job.annotation.Task;
import com.didiglobal.knowframework.job.common.TaskResult;
import com.didiglobal.knowframework.job.core.job.Job;
import com.didiglobal.knowframework.job.core.job.JobContext;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;

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
@Task(name = "IndexRolloverRandomTask", description = "模板Rollover实现", cron = "0 0 0/1 * * ?", autoRegister = true)
public class IndexRolloverRandomTask extends BaseConcurrentTemplateTask implements Job {

    private static final ILog LOGGER = LogFactory.getLog(IndexRolloverRandomTask.class);

    @Autowired
    private IndexPlanManager  indexPlanManager;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=IndexRolloverRandomTask||method=execute||msg=IndexRolloverRandomTask start");
        if (execute()) {
            return TaskResult.buildSuccess();
        }
        return TaskResult.buildFail();
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
        final Result<Void> result = indexPlanManager.indexRollover(logicId);
        if (result.failed()){
             LOGGER.warn("class=IndexRolloverRandomTask||method=executeByLogicTemplate||logicId={}||msg={}", logicId,
                        result.getMessage());
             return result.failed();
        }
        return result.success();
    }
}