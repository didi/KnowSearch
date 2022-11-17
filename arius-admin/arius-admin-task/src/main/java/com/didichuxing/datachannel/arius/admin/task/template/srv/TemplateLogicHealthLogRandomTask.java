package com.didichuxing.datachannel.arius.admin.task.template.srv;

import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.task.BaseConcurrentTemplateTask;
import com.didiglobal.knowframework.job.annotation.Task;
import com.didiglobal.knowframework.job.common.TaskResult;
import com.didiglobal.knowframework.job.core.job.Job;
import com.didiglobal.knowframework.job.core.job.JobContext;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;

/**
 * 健康随机任务 这是一个每 5 分钟运行一次的任务，名为 HealthRandomTask。
 *
 * @author shizeying
 * @date 2022/08/16
 */
@Task(name = "TemplateLogicHealthLogRandomTask", description = "逻辑模板健康率同步任务", cron = "0 0/5 * * * ? *", autoRegister =
		true)
public class TemplateLogicHealthLogRandomTask extends BaseConcurrentTemplateTask implements Job {
	private static final ILog LOGGER = LogFactory.getLog(ColdDataMoveRandomTask.class);
	
	@Override
	public TaskResult execute(JobContext jobContext) throws Exception {
		LOGGER.info("class={}||method=execute||msg=HealthRandomTask start",getClass().getSimpleName());
		if (execute()) {
			return TaskResult.SUCCESS;
		}
		return TaskResult.FAIL;
	}
	

	@Override
	protected boolean executeByLogicTemplate(Integer logicId) throws AdminOperateException {
		return templateLogicManager.updateTemplateHealthByLogicId(logicId);
	}
	
	/**
	 * 任务的线程个数
	 *
	 * @return
	 */
	@Override
	public int poolSize() {
		return 10;
	}
	
	/**
	 * 并发度
	 *
	 * @return
	 */
	@Override
	public int current() {
		return 5;
	}
}