package com.didiglobal.logi.op.manager.application;

import com.didiglobal.logi.op.manager.domain.packages.entity.Package;
import com.didiglobal.logi.op.manager.domain.packages.service.PackageDomainService;
import com.didiglobal.logi.op.manager.domain.script.entity.Script;
import com.didiglobal.logi.op.manager.domain.script.service.impl.ScriptDomainService;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-11 2:40 下午
 */
@Component
public class PackageService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PackageService.class);

    @Autowired
    private PackageDomainService packageDomainService;

    @Autowired
    private ScriptDomainService scriptDomainService;

    /**
     * 创建安装包
     * @param pk
     * @return Result
     */
    public Result<Void> createPackage(Package pk) {
        //校验参数
        Result checkResult = pk.checkCreateParam();
        if (checkResult.failed()) {
            return checkResult;
        }

        if (null == scriptDomainService.getScriptById(pk.getScriptId()).getData()) {
            return Result.fail("绑定的脚本不存在");
        }

        //新建安装包
        return packageDomainService.createPackage(pk);
    }

    /**
     * 更新安装包
     * @param pk
     * @return result
     */
    public Result<Void> updatePackage(Package pk) {
        //检验参数
        Result<Void>  checkResult = pk.checkUpdateParam();
        if (checkResult.failed()) {
            return checkResult;
        }
        //修改安装包
        return packageDomainService.updatePackage(pk);
    }

    public Result<List<Package>> queryPackage(Package pk) {
        return packageDomainService.queryPackage(pk);
    }
}
