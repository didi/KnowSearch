package com.didiglobal.logi.op.manager.domain.packages.service.impl;

import com.didiglobal.logi.op.manager.domain.packages.entity.Package;
import com.didiglobal.logi.op.manager.domain.packages.repository.PackageGroupConfigRepository;
import com.didiglobal.logi.op.manager.domain.packages.repository.PackageRepository;
import com.didiglobal.logi.op.manager.domain.packages.service.PackageDomainService;
import com.didiglobal.logi.op.manager.domain.script.entity.Script;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.deployment.DeploymentService;
import com.didiglobal.logi.op.manager.infrastructure.storage.StorageService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        //创建
        pk.create();

        //上传
        Result<String> storageRes = storageService.upload(getUniqueFileName(pk), pk.getUploadFile());
        if (storageRes.failed()) {
            return Result.fail(storageRes.getCode(), storageRes.getMessage());
        }
        pk.setUrl(storageRes.getData());

        //入库
        int packageId = packageRepository.insertPackage(pk);
        if (!pk.getGroupConfigList().isEmpty()) {
            pk.getGroupConfigList().forEach(packageGroupConfig -> {
                packageGroupConfig.setPackageId(packageId);
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
        return Result.buildSuccess(list);
    }

    @NotNull
    private String getUniqueFileName(Package pk) {
        return pk.getName() + "_" + System.currentTimeMillis();
    }
}
