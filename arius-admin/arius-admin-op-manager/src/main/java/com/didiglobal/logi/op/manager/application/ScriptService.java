package com.didiglobal.logi.op.manager.application;

import com.didiglobal.logi.op.manager.domain.packages.entity.Package;
import com.didiglobal.logi.op.manager.domain.packages.service.PackageDomainService;
import com.didiglobal.logi.op.manager.domain.script.entity.Script;
import com.didiglobal.logi.op.manager.domain.script.service.impl.ScriptDomainService;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-06 3:28 下午
 */
@Component
public class ScriptService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScriptService.class);

    @Autowired
    private ScriptDomainService scriptDomainService;

    @Autowired
    private PackageDomainService packageDomainService;

    /**
     * 根据条件获取所有的脚本list
     * @param script 脚本
     * @return result
     */
    public Result<List<Script>> listScript(Script script) {
        return scriptDomainService.queryScript(script);
    }

    /**
     * 创建脚本
     * @param script
     * @return result
     */
    public Result<Void> createScript(Script script) {
        //校验参数
        Result<Void> checkResult = script.checkCreateParam();
        if (checkResult.failed()) {
            return Result.fail(checkResult.getCode(),checkResult.getMessage());
        }
        //新建脚本
        return scriptDomainService.createScript(script);
    }

    /**
     * 更新脚本
     * @param script
     * @return result
     */
    public Result<Void> updateScript(Script script) {
        //检验参数
        Result<Void>  checkResult = script.checkUpdateParam();
        if (checkResult.failed()) {
            return Result.fail(checkResult.getCode(),checkResult.getMessage());
        }
        //修改脚本
        return scriptDomainService.updateScript(script);
    }

    /**
     * 删除脚本
     * @param id
     * @return
     */
    public Result<Void> deleteScript(Integer id) {
        //检验参数
        if (null == id) {
            Result.fail(ResultCode.PARAM_ERROR.getCode(), "id不能为空");
        }
        //判断,数据库中是否存在该脚本
        if (null == scriptDomainService.getScriptById(id).getData()){
            Result.fail(ResultCode.SCRIPT_OPERATE_ERROR.getCode(), "数据库中不存在该脚本，请核实");
        }
        //判断,若脚本已经绑定了包则不能删除
//        Package pk = new Package();
//        pk.setScriptId(id);
//        if (!packageDomainService.queryPackage(pk).getData().isEmpty()) {
//            Result.fail(ResultCode.SCRIPT_OPERATE_ERROR.getCode(), "脚本已被绑定，不能删除");
//        }

        return scriptDomainService.deleteScript(id);
    }
}
