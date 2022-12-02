package com.didiglobal.logi.op.manager.domain.packages.service.impl;

import com.didiglobal.logi.op.manager.domain.packages.entity.Package;
import com.didiglobal.logi.op.manager.domain.packages.entity.value.PackageGroupConfig;
import com.didiglobal.logi.op.manager.domain.packages.repository.PackageGroupConfigRepository;
import com.didiglobal.logi.op.manager.domain.packages.repository.PackageRepository;
import com.didiglobal.logi.op.manager.domain.packages.service.PackageDomainService;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.ResultCode;
import com.didiglobal.logi.op.manager.infrastructure.storage.StorageService;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

import static com.didiglobal.logi.op.manager.infrastructure.util.FileUtil.getDeleteFileName;
import static com.didiglobal.logi.op.manager.infrastructure.util.FileUtil.getUniqueFileName;

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
        if (Objects.nonNull(packageRepository.findByVersion(pk.getVersion(), pk.getPackageType()))) {
            return Result.fail(ResultCode.PARAM_ERROR.getCode(), "当前版本已存在");
        }
        //创建
        pk.create();

        //上传
        Result<String> storageRes = storageService.upload(getUniqueFileName(pk.getName(), pk.getUploadFile().getOriginalFilename()), pk.getUploadFile());
        if (storageRes.failed()) {
            return Result.fail(storageRes.getCode(), storageRes.getMessage());
        }
        pk.setUrl(storageRes.getData());

        int packageId = packageRepository.insertPackage(pk);
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
        return Result.buildSuccess(list);
    }

    @Override
    public Result<Package> getPackageById(int id) {
        return Result.success(packageRepository.findById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> updatePackage(Package pk) {
        //更新
        pk.update();

        if (null != pk.getUploadFile()) {
            Result<String> storageRes = storageService.upload(getUniqueFileName(pk.getName(), pk.getUploadFile().getOriginalFilename()), pk.getUploadFile());
            if (storageRes.failed()) {
                return Result.fail(storageRes.getCode(), storageRes.getMessage());
            }
            pk.setUrl(storageRes.getData());
        }
        //更新数据库
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
    public List<Package> pagingByCondition(Package pagingPackage, Long page, Long size) {
        return packageRepository.pagingByCondition(pagingPackage,page,size);
    }

    @Override
    public List<Package> listPackageByPackageType(Integer packageType) {
        return packageRepository.listPackageByPackageType(packageType);
    }

    @Override
    public List<PackageGroupConfig> listPackageGroupConfigByVersion(String version, Integer packageType) {
        Package packageByVersion = packageRepository.findByVersion(version, packageType);
        if(Objects.isNull(packageByVersion)){
            return Lists.newArrayList();
        }
        return packageGroupConfigRepository.queryConfigByPackageId(packageByVersion.getId());
    }

    @Override
    public Long countByCondition(Package pagingPackage) {
        return packageRepository.countByCondition(pagingPackage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
}
