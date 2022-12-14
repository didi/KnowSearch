package com.didiglobal.logi.op.manager.application;

import com.didiglobal.logi.op.manager.domain.component.service.ComponentDomainService;
import com.didiglobal.logi.op.manager.domain.packages.entity.Package;
import com.didiglobal.logi.op.manager.domain.packages.entity.value.PackageGroupConfig;
import com.didiglobal.logi.op.manager.domain.packages.service.PackageDomainService;
import com.didiglobal.logi.op.manager.domain.script.service.impl.ScriptDomainService;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.didiglobal.logi.op.manager.infrastructure.common.ResultCode.PACKAGE_IS_DEPEND_ERROR;
import static com.didiglobal.logi.op.manager.infrastructure.common.ResultCode.PACKAGE_IS_DEPEND_UPDATE_ERROR;

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

    @Autowired
    private ComponentDomainService componentDomainService;


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
        Result<Boolean> res = usingPackage(pk.getId());
        if (res.getData() && null != pk.getUploadFile()) {
            return Result.fail(PACKAGE_IS_DEPEND_UPDATE_ERROR);
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

        //判断,若包已经绑定了组件则不能删除
        Result<Boolean> res = usingPackage(id);
        if (res.failed() || res.getData()) {
            return Result.fail(PACKAGE_IS_DEPEND_ERROR);
        }

        return packageDomainService.deletePackage(pk);
    }

    /**
     * 分页查询软件包列表
     * @param pagingPackage
     * @param page
     * @param size
     * @return
     */
    public List<Package> pagingByCondition(Package pagingPackage, Long page, Long size) {
        List<Package> packageList = packageDomainService.pagingByCondition(pagingPackage, (page - 1) * size, size);
        return packageList;
    }

    /**
     * 查询软件包总数
     * @param pagingPackage
     * @return
     */
    public Long countByCondition(Package pagingPackage) {
        return packageDomainService.countByCondition(pagingPackage);
    }

    /**
     * 根据id查询软件包
     * @param id
     * @return
     */
    public Result<Package> getPackageById(Long id) {
        return packageDomainService.getPackageById(Math.toIntExact(id));
    }

    /**
     * 是否正在使用安装包
     * @param id
     * @return
     */
    public Result<Boolean> usingPackage(Integer id) {
        return componentDomainService.hasPackageDependComponent(id);
    }

    /**
     * 根据软件包类型获取软件包
     * @param packageType
     * @return
     */
    public List<Package> listPackageByPackageType(Integer packageType){
        return packageDomainService.listPackageByPackageType(packageType);
    }

    /**
     * 分页查询时判断软件包是否正在使用
     * @param packageIds
     * @return
     */
    public List<Integer> hasPackagesDependComponent(List<Integer> packageIds) {
        return componentDomainService.hasPackagesDependComponent(packageIds);
    }

    /**
     * 通过es包版本号获取es配置组
     * 软件包类型+软件包版本是唯一
     * @param version
     * @param packageType
     * @return
     */
    public List<PackageGroupConfig> listPackageGroupConfigByVersion(String version, Integer packageType) {
        return packageDomainService.listPackageGroupConfigByVersion(version, packageType);
    }

    /**
     * 通过软件包名称查询软件包
     * @param name
     * @return
     */
    public Package queryPackageByName(String name) {
        return packageDomainService.queryPackageByName(name);
    }

    /**
     * 通过软件包名称获取配置组列表
     * 软件包名称是唯一的
     * @param name
     * @return
     */
    public List<PackageGroupConfig> listPackageGroupConfigByName(String name) {
        return packageDomainService.listPackageGroupConfigByName(name);
    }
}
