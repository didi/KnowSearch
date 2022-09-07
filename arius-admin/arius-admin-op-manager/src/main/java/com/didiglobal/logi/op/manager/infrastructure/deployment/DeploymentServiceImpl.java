package com.didiglobal.logi.op.manager.infrastructure.deployment;

import com.alibaba.fastjson.JSONObject;
import com.didiglobal.logi.op.manager.domain.script.entity.Script;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.ResultCode;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.TaskActionEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.TaskLogEnum;
import com.didiglobal.logi.op.manager.infrastructure.deployment.zeus.ZeusService;
import com.didiglobal.logi.op.manager.infrastructure.deployment.zeus.ZeusTask;
import com.didiglobal.logi.op.manager.infrastructure.deployment.zeus.ZeusTaskStatus;
import com.didiglobal.logi.op.manager.infrastructure.deployment.zeus.ZeusTemplate;
import com.didiglobal.logi.op.manager.infrastructure.exception.ZeusOperationException;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;

import static com.didiglobal.logi.op.manager.infrastructure.common.Constants.SPLIT;

/**
 * @author didi
 * @date 2022-07-09 11:32 上午
 */
@Component
public class DeploymentServiceImpl implements DeploymentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeploymentServiceImpl.class);

    @Autowired
    ZeusService zeusService;

    @Override
    public Result<String> deployScript(Script script) {
        try {
            ZeusTemplate zeusTemplate = new ZeusTemplate();
            zeusTemplate.setKeywords(script.getName());
            zeusTemplate.setScript(new String(script.getUploadFile().getBytes()));
            return Result.buildSuccess(zeusService.createTemplate(zeusTemplate));
        } catch (IOException e) {
            LOGGER.error("class=DeploymentServiceImpl||method=deployScript||errMsg={}||msg=file to string failed", e.getMessage());
            return Result.fail();
        } catch (ZeusOperationException e) {
            LOGGER.error("class=DeploymentServiceImpl||method=deployScript||errMsg={}||msg=deploy script failed", e.getMessage());
            return Result.fail(e.getCode(), e.getMessage());
        }

    }

    @Override
    public Result<Void> editScript(Script script) {
        try {
            ZeusTemplate zeusTemplate = new ZeusTemplate();
            zeusTemplate.setId(Integer.parseInt(script.getTemplateId()));
            zeusTemplate.setKeywords(script.getName());
            zeusTemplate.setScript(new String(script.getUploadFile().getBytes()));
            zeusService.editTemplate(zeusTemplate);
            return Result.success();
        } catch (IOException e) {
            LOGGER.error("class=DeploymentServiceImpl||method=editScript||errMsg={}||msg=file to string failed", e.getMessage());
            return Result.fail();
        } catch (ZeusOperationException e) {
            LOGGER.error("class=DeploymentServiceImpl||method=editScript||errMsg={}||msg=edit script failed", e.getMessage());
            return Result.fail(e.getCode(), e.getMessage());
        }
    }

    @Override
    public Result<Integer> execute(String templateId, String hosts, String action, Integer batch, String... args) {
        try {
            ZeusTask task = new ZeusTask();
            task.setAction(TaskActionEnum.START.getAction());
            task.setHosts(Arrays.asList(hosts.split(SPLIT)));
            task.setTemplateId(Integer.parseInt(templateId));
            task.setArgs(String.format("%s,,%s", action, String.join(",,", args)));
            if (null != batch) {
                task.setBatch(batch);
            }
            return Result.success(zeusService.executeTask(task));
        } catch (ZeusOperationException e) {
            LOGGER.error("class=DeploymentServiceImpl||method=execute||errMsg={}||msg=execute failed", e.getMessage());
            return Result.fail(e.getCode(), e.getMessage());
        }
    }

    @Override
    public Result<Void> actionTask(int executeTaskId, String action) {
        try {
            JSONObject param = new JSONObject();
            param.put("task_id", executeTaskId);
            param.put("action", action);
            zeusService.actionTask(param);
            return Result.success();
        } catch (ZeusOperationException e) {
            LOGGER.error("class=DeploymentServiceImpl||method=actionTask||errMsg={}||msg=execute failed", e.getMessage());
            return Result.fail(e.getCode(), e.getMessage());
        }
    }

    @Override
    public Result<Void> actionHost(int executeTaskId, String host, String action) {
        try {
            JSONObject param = new JSONObject();
            param.put("task_id", executeTaskId);
            param.put("hostname", host);
            param.put("action", action);
            zeusService.actionHost(param);
            return Result.success();
        } catch (ZeusOperationException e) {
            LOGGER.error("class=DeploymentServiceImpl||method=actionHost||errMsg={}||msg=execute failed", e.getMessage());
            return Result.fail(e.getCode(), e.getMessage());
        }
    }

    @Override
    public Result<ZeusTaskStatus> deployStatus(int taskId) {
        try {
            return Result.success(zeusService.getTaskStatus(taskId));
        } catch (ZeusOperationException e) {
            LOGGER.error("class=DeploymentServiceImpl||method=deployStatus||errMsg={}||msg=get status error", e.getMessage());
            return Result.fail(e.getCode(), e.getMessage());
        }
    }

    @Override
    public Result<String> deployTaskLog(int taskId, String hostname, TaskLogEnum taskLogEnum) {
        try {
            return Result.success(zeusService.getTaskLog(taskId, hostname, taskLogEnum));
        } catch (ZeusOperationException e) {
            LOGGER.error("class=DeploymentServiceImpl||method=deployStdouts||errMsg={}||msg=get stdouts error", e.getMessage());
            return Result.fail(e.getCode(), e.getMessage());
        }
    }

    @Override
    public Result<Void> removeScript(Script script) {
        try {
            zeusService.deleteTemplate(Integer.parseInt(script.getTemplateId()));
            return Result.success();
        } catch (ZeusOperationException e) {
            LOGGER.error("class=DeploymentServiceImpl||method=deployStatus||errMsg={}||msg=remove error", e.getMessage());
            return Result.fail(e.getCode(), e.getMessage());
        } catch (Exception exception) {
            LOGGER.error("script[{}] remove error", script.getName(), exception);
            return Result.fail(ResultCode.ZEUS_OPERATE_ERROR);
        }
    }

}
