package com.didiglobal.logi.op.manager.domain.script.service.impl;

import com.didiglobal.logi.op.manager.domain.script.entity.Script;
import com.didiglobal.logi.op.manager.domain.script.repository.ScriptRepository;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.deployment.DeploymentService;
import com.didiglobal.logi.op.manager.infrastructure.storage.StorageService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.UUID;


/**
 * @author didi
 * @date 2022-07-04 7:07 下午
 */
@Service
public class ScriptDomainServiceImpl implements com.didiglobal.logi.op.manager.domain.script.service.impl.ScriptDomainService {

    public static final Logger LOGGER = LoggerFactory.getLogger(ScriptDomainServiceImpl.class);

    @Autowired
    private ScriptRepository scriptRepository;

    @Autowired
    private DeploymentService deploymentService;

    @Autowired
    private StorageService storageService;

    @Override
    public Result<Script> getScriptById(int id) {
        return Result.success(scriptRepository.findById(id));
    }

    @Override
    public Result<List<Script>> queryScript(Script script) {
        return Result.success(scriptRepository.queryScript(script));
    }



    @Override
    public Result<Void> createScript(Script script) {
        //新建
        script.create();

        //上传
        Result<String> storageRes = storageService.upload(getUniqueFileName(script), script.getUploadFile());
        if (storageRes.failed()) {
            return Result.fail(storageRes.getCode(), storageRes.getMessage());
        }
        script.setContentUrl(storageRes.getData());

        //部署
        Result<String> deployRes = deploymentService.deployScript(script);
        if (deployRes.failed()) {
            return Result.fail(deployRes.getCode(), deployRes.getMessage());
        }
        script.setTemplateId(deployRes.getData());

        //入库
        scriptRepository.insertScript(script);
        return Result.success();
    }

    @Override
    public Result<Void> updateScript(Script script) {
        //更新
        script.update();

        if (null != script.getUploadFile()) {
            //上传
            Result<String> storageRes = storageService.upload(getUniqueFileName(script), script.getUploadFile());
            if (storageRes.failed()) {
                return Result.fail(storageRes.getCode(), storageRes.getMessage());
            }
            script.setContentUrl(storageRes.getData());

            //修改
            Result<String> deployRes = deploymentService.deployScript(script);
            if (deployRes.failed()) {
                return Result.fail(deployRes.getCode(), deployRes.getMessage());
            }
            script.setTemplateId(deployRes.getData());
        }

        //更新
        scriptRepository.updateScript(script);
        return Result.success();
    }

    @Override
    public Result<Void> deleteScript(int id) {
        scriptRepository.deleteScript(id);
        return Result.success();
    }

    @NotNull
    private String getUniqueFileName(Script script) {
        return script.getName() + "_" + System.currentTimeMillis();
    }

}
