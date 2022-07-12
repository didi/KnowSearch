package com.didiglobal.logi.op.manager.infrastructure.deployment;

import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import com.didiglobal.logi.op.manager.application.ScriptService;
import com.didiglobal.logi.op.manager.domain.script.entity.Script;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.exception.ZeusOperationException;
import com.didiglobal.logi.op.manager.infrastructure.storage.hander.S3FileStorageHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.didiglobal.logi.op.manager.infrastructure.common.ResultCode.ZEUS_OPERATE_ERROR;

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
            return Result.buildSuccess(zeusService.ditTemplate(zeusTemplate));
        } catch (IOException e) {
            LOGGER.error("class=DeploymentServiceImpl||method=editScript||errMsg={}||msg=file to string failed", e.getMessage());
            return Result.fail();
        } catch (ZeusOperationException e) {
            LOGGER.error("class=DeploymentServiceImpl||method=editScript||errMsg={}||msg=edit script failed", e.getMessage());
            return Result.fail(e.getCode(), e.getMessage());
        }
    }
}
