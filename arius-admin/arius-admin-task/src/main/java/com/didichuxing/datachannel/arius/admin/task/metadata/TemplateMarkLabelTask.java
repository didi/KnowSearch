package com.didichuxing.datachannel.arius.admin.task.metadata;

import com.didichuxing.datachannel.arius.admin.common.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.metadata.job.template.mark.BaseTemplateMarkLabelJob;
import com.didiglobal.logi.auvjob.annotation.Task;
import com.didiglobal.logi.auvjob.core.job.Job;
import com.didiglobal.logi.auvjob.core.job.JobContext;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.Map;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.JOB_SUCCESS;

@Task(name = "templateMarkLabelTask", description = "索引模板标签任务", cron = "0 10 12 * * ?", autoRegister = true)
public class TemplateMarkLabelTask implements Job {
    private final static Logger LOGGER = LoggerFactory.getLogger(TemplateMarkLabelTask.class);

    private Map<String, BaseTemplateMarkLabelJob> templateMarkLabelJobs = Maps.newHashMap();

    @PostConstruct
    public void init(){
        templateMarkLabelJobs = SpringTool.getBeansOfType(BaseTemplateMarkLabelJob.class);
    }

    @Override
    public Object execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=TemplateMarkLabelTask||method=syncTaskStatus||templateMarkLabelJobs={}||msg=start", templateMarkLabelJobs.keySet());

        if(!templateMarkLabelJobs.isEmpty()){
            templateMarkLabelJobs.values().forEach(t -> t.handleJobTask("") );
        }

        return JOB_SUCCESS;
    }
}
