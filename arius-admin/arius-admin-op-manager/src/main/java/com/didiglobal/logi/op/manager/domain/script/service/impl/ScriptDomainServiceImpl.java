package com.didiglobal.logi.op.manager.domain.script.service.impl;

import com.didiglobal.logi.op.manager.domain.script.entity.Script;
import com.didiglobal.logi.op.manager.domain.script.repository.ScriptRepository;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.ResultCode;
import com.didiglobal.logi.op.manager.infrastructure.deployment.DeploymentService;
import com.didiglobal.logi.op.manager.infrastructure.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.didiglobal.logi.op.manager.infrastructure.util.FileUtil.getDeleteFileName;
import static com.didiglobal.logi.op.manager.infrastructure.util.FileUtil.getUniqueFileName;


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

        //判断新建的脚本名称是否已经在数据库中存在，确保数据库中脚本名称唯一
        if (null != scriptRepository.findByName(script.getName())) {
            return Result.fail(ResultCode.PARAM_ERROR.getCode(),"脚本名称重复请重新输入");
        }

        //新建
        script.create();

        //上传
        Result<String> storageRes = storageService.upload(getUniqueFileName(script.getName(), script.getUploadFile().getOriginalFilename()),
                script.getUploadFile());
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
            Result<String> storageRes = storageService.upload(getUniqueFileName(script.getName(), script.getUploadFile().getOriginalFilename()), script.getUploadFile());
            if (storageRes.failed()) {
                return Result.fail(storageRes.getCode(), storageRes.getMessage());
            }
            script.setContentUrl(storageRes.getData());

            //修改部署的脚本
            Result<Void> deployRes = deploymentService.editScript(script);
            if (deployRes.failed()) {
                return Result.fail(deployRes.getCode(), deployRes.getMessage());
            }
        }

        //更新
        scriptRepository.updateScript(script);
        return Result.success();
    }

    @Override
    public List<Script> pagingByCondition(Script script, Long page, Long size) {
        return scriptRepository.pagingByCondition(script,page,size);
    }

    @Override
    public Long countByCondition(Script script) {
        return scriptRepository.countByCondition(script);
    }

    @Override
    public Result<Void> deleteScript(Script script) {
        //删除文件存储中的相关脚本文件
        Result<String> deleteStorage = storageService.remove(getDeleteFileName(script.getContentUrl()));
        if (deleteStorage.failed()) {
            return Result.fail(deleteStorage.getCode(),deleteStorage.getMessage());
        }
        //部署脚本删除,这里是zeus的一个缺陷不支持模板的删除，所以这里未判断是否成功，删除不成功也不影响
        deploymentService.removeScript(script);
        //删除脚本
        scriptRepository.deleteScript(script.getId());
        return Result.success();
    }

}
