package com.didiglobal.logi.op.manager.application;

import com.didiglobal.logi.op.manager.domain.packages.entity.Package;
import com.didiglobal.logi.op.manager.domain.packages.service.PackageDomainService;
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
     * @param pk 安装包
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

    /**
     * 删除包
     * @param id 要删除的包所对应id
     * @return Result
     */
    public Result<Void> deletePackage(Integer id) {
        Package pk;
        //检验参数id是否为空以及数据库中是否能找到对应id的包
        if (null == id || null == (pk = packageDomainService.getPackageById(id).getData()) ) {
            return Result.fail(ResultCode.PARAM_ERROR.getCode(), "输入的id参数有问题，请核对");
        }

        //TODO
        //判断,若包已经绑定了组件则不能删除

        return packageDomainService.deletePackage(pk);
    }
}
