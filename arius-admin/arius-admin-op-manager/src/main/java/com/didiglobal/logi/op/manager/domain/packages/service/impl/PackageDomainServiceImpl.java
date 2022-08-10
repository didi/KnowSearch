package com.didiglobal.logi.op.manager.domain.packages.service.impl;

import com.didiglobal.logi.op.manager.domain.packages.entity.Package;
import com.didiglobal.logi.op.manager.domain.packages.entity.value.PackageGroupConfig;
import com.didiglobal.logi.op.manager.domain.packages.repository.PackageGroupConfigRepository;
import com.didiglobal.logi.op.manager.domain.packages.repository.PackageRepository;
import com.didiglobal.logi.op.manager.domain.packages.service.PackageDomainService;
import com.didiglobal.logi.op.manager.domain.script.entity.Script;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.ResultCode;
import com.didiglobal.logi.op.manager.infrastructure.deployment.DeploymentService;
import com.didiglobal.logi.op.manager.infrastructure.storage.StorageService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

import static com.didiglobal.logi.op.manager.infrastructure.common.Constants.SLASH;

/**
 * @author didi
 * @date 2022-07-11 2:35 下午
 */
@Service
public class PackageDomainServiceImpl implements PackageDomainService {

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private PackageGroupConfigRepository packageGroupConfigRepository;

    @Autowired
    private StorageService storageService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> createPackage(Package pk) {

        //判断新建的安装包名称是否已经在数据库中存在，确保数据库中安装包名称唯一
        if (null != packageRepository.findByName(pk.getName())) {
            return Result.fail(ResultCode.PARAM_ERROR.getCode(),"安装包名称已存在，请重新输入");
        }

        //创建
        pk.create();

        //上传
        Result<String> storageRes = storageService.upload(getUniqueFileName(pk), pk.getUploadFile());
        if (storageRes.failed()) {
            return Result.fail(storageRes.getCode(), storageRes.getMessage());
        }
        pk.setUrl(storageRes.getData());

        //入库
        packageRepository.insertPackage(pk);
        int packageId = packageRepository.findByName(pk.getName()).getId();
        if (null != pk.getGroupConfigList()) {
            pk.getGroupConfigList().forEach(packageGroupConfig -> {
                packageGroupConfig.setPackageId(packageId);
                packageGroupConfig.setCreateTime(new Timestamp(System.currentTimeMillis()));
                packageGroupConfigRepository.insertPackageGroupConfig(packageGroupConfig);
            });

        }
        return Result.success();
    }

    @Override
    public Result<List<Package>> queryPackage(Package pk) {
        List<Package> list = packageRepository.queryPackage(pk);
        list.stream().forEach(p -> {
            p.setGroupConfigList(packageGroupConfigRepository.queryConfigByPackageId(p.getId()));
        });
        List<PackageGroupConfig> list1 = packageGroupConfigRepository.queryConfigByPackageId(pk.getId());
        pk.getId();
        return Result.buildSuccess(list);
    }

    @Override
    public Result<Package> getPackageById(int id) {
        return Result.success(packageRepository.findById(id));
    }

    @Override
    public Result<Void> updatePackage(Package pk) {

        //todo
        // 跟组件相关，若绑定了组件，则所绑定的脚本和版本以及安装包都不可以更改。

        //更新
        pk.update();

        if (null != pk.getUploadFile()) {
            Result<String> storageRes = storageService.upload(getUniqueFileName(pk), pk.getUploadFile());
            if (storageRes.failed()) {
                return Result.fail(storageRes.getCode(), storageRes.getMessage());
            }
            pk.setUrl(storageRes.getData());
        }

        //更新
        packageRepository.updatePackage(pk);
        int packageId = pk.getId();
        if (null != pk.getGroupConfigList()) {
            packageGroupConfigRepository.deleteByPackageId(packageId);
            pk.getGroupConfigList().forEach(packageGroupConfig -> {
                packageGroupConfig.setPackageId(packageId);
                packageGroupConfigRepository.insertPackageGroupConfig(packageGroupConfig);
            });

        }
        return Result.success();
    }

    @Override
    public Result<Void> deletePackage(Package pk) {
        //删除文件存储中的相关包文件
        Result<String> deleteStorage = storageService.remove(getDeleteFileName(pk.getUrl()));
        if (deleteStorage.failed()) {
            return Result.fail(deleteStorage.getCode(),deleteStorage.getMessage());
        }
        //删除相关默认组件配置
        packageGroupConfigRepository.deleteByPackageId(pk.getId());
        //删除包
        packageRepository.deletePackage(pk.getId());
        return Result.success();
    }

    @NotNull
    private String getUniqueFileName(Package pk) {
        return pk.getName() + "_" + System.currentTimeMillis();
    }

    @NotNull
    private String getDeleteFileName(String url) {
        return url.substring(url.lastIndexOf(SLASH) + 1);
    }
}
