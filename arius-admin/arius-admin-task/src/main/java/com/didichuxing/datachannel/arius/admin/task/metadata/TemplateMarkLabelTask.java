package com.didichuxing.datachannel.arius.admin.task.metadata;

import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.metadata.job.template.mark.BaseTemplateMarkLabelJob;
import com.didiglobal.logi.job.common.TaskResult;
import com.didiglobal.logi.job.core.job.Job;
import com.didiglobal.logi.job.core.job.JobContext;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.Map;

//@Task(name = "templateMarkLabelTask", description = "索引模板标签任务", cron = "0 10 12 * * ?", autoRegister = true)
public class TemplateMarkLabelTask implements Job {
    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateMarkLabelTask.class);

    private Map<String, BaseTemplateMarkLabelJob> templateMarkLabelJobs = Maps.newHashMap();

    @PostConstruct
    public void init(){
        templateMarkLabelJobs = SpringTool.getBeansOfType(BaseTemplateMarkLabelJob.class);
    }

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=TemplateMarkLabelTask||method=execute||templateMarkLabelJobs={}||msg=start", templateMarkLabelJobs.keySet());

        if(!templateMarkLabelJobs.isEmpty()){
            templateMarkLabelJobs.values().forEach(t -> t.handleJobTask("") );
        }

        return TaskResult.SUCCESS;
    }
}
