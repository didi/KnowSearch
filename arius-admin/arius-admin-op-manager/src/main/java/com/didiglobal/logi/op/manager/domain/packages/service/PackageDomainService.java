package com.didiglobal.logi.op.manager.domain.packages.service;

import com.didiglobal.logi.op.manager.domain.packages.entity.Package;
import com.didiglobal.logi.op.manager.domain.packages.entity.value.PackageGroupConfig;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-11 2:34 下午
 */
public interface PackageDomainService {

    /**
     * 创建安装包
     *
     * @param pk 安装包实体
     * @return
     */
    Result<Void> createPackage(Package pk);

    /**
     * 编辑安装包
     *
     * @param pk 安装包实体
     * @return
     */
    Result<Void> updatePackage(Package pk);

    /**
     * 查询安装包
     *
     * @param pk 安装包
     * @return 查询的安装包集合
     */
    Result<List<Package>> queryPackage(Package pk);

    /**
     * 根据id获取安装包
     *
     * @param id 安装包id
     * @return 安装包
     */
    Result<Package> getPackageById(int id);

    /**
     * 删除安装包
     *
     * @param pk 安装包实体
     * @return
     */
    Result<Void> deletePackage(Package pk);

    /**
     * 分页查询软件包列表
     * @param pagingPackage
     * @param page
     * @param size
     * @return
     */
    List<Package> pagingByCondition(Package pagingPackage, Long page, Long size);

    /**
     * 软件包总数
     * @param pagingPackage
     * @return
     */
    Long countByCondition(Package pagingPackage);

    /**
     * 通过包类型获取list
     * @param packageType
     * @return
     */
    List<Package> listPackageByPackageType(Integer packageType);

    /**
     * 通过es包版本号获取es配置组
     * @param version
     * @return
     */
    List<PackageGroupConfig> listPackageGroupConfigByVersion(String version);
}
