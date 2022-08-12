package com.didiglobal.logi.op.manager.domain.packages.service;

import com.didiglobal.logi.op.manager.domain.packages.entity.Package;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-11 2:34 下午
 */
public interface PackageDomainService {

    /**
     * 创建脚本
     * @param pk 安装包
     * @return result
     */
    Result<Void> createPackage(Package pk);

    /**
     * 编辑安装包
     * @param pk
     * @return
     */
    Result<Void> updatePackage(Package pk);

    /**
     * 查询安装包
     * @param pk 安装包
     * @return Result<List<Package>> 所查询的安装包集合
     */
    Result<List<Package>> queryPackage(Package pk);

    /**
     * 根据id获取安装包
     * @param id
     * @return Result<Package> 安装包
     */
    Result<Package> getPackageById(int id);

    /**
     * 删除包
     * @param pk 包
     * @return Result
     */
    Result<Void> deletePackage(Package pk);

}
