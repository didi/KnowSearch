package com.didiglobal.logi.op.manager.infrastructure.deployment;

import com.didiglobal.logi.op.manager.domain.script.entity.Script;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.TaskActionEnum;
import com.didiglobal.logi.op.manager.infrastructure.exception.ZeusOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

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
    public Result<String> editScript(Script script) {
        try {
            ZeusTemplate zeusTemplate = new ZeusTemplate();
            zeusTemplate.setId(script.getTemplateId());
            zeusTemplate.setKeywords(script.getName());
            zeusTemplate.setScript(new String(script.getUploadFile().getBytes()));
            return Result.buildSuccess(zeusService.editTemplate(zeusTemplate));
        } catch (IOException e) {
            LOGGER.error("class=DeploymentServiceImpl||method=editScript||errMsg={}||msg=file to string failed", e.getMessage());
            return Result.fail();
        } catch (ZeusOperationException e) {
            LOGGER.error("class=DeploymentServiceImpl||method=editScript||errMsg={}||msg=edit script failed", e.getMessage());
            return Result.fail(e.getCode(), e.getMessage());
        }
    }

    @Override
    public Result<Integer> execute(String templateId, Integer taskId, String groupName, String hosts, String... args) {
        try {
            ZeusTask task = new ZeusTask();
            task.setAction(TaskActionEnum.START.getAction());
            task.setHosts(hosts);
            task.setTemplateId(Integer.parseInt(templateId));
            task.setArgs(String.format("%s %s %s %s", "execute", groupName, taskId, String.join(" ", args)));
            return Result.success(zeusService.executeTask(task));
        } catch (ZeusOperationException e) {
            LOGGER.error("class=DeploymentServiceImpl||method=execute||errMsg={}||msg=execute failed", e.getMessage());
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
    public Result<Void> removeScript(Script script) {
        try {
            zeusService.deleteTemplate(Integer.parseInt(script.getTemplateId()));
            return Result.success();
        } catch (ZeusOperationException e) {
            LOGGER.error("class=DeploymentServiceImpl||method=deployStatus||errMsg={}||msg=get status error", e.getMessage());
            return Result.fail(e.getCode(), e.getMessage());
        }
    }

}
