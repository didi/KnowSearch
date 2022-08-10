package com.didiglobal.logi.op.manager.domain.packages.service;

import com.didiglobal.logi.op.manager.domain.packages.entity.Package;
import com.didiglobal.logi.op.manager.domain.script.entity.Script;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-11 2:34 下午
 */
public interface PackageDomainService {

    /**
     * 创建脚本
     * @param pk
     * @return
     */
    Result<Void> createPackage(Package pk);

    /**
     * 编辑安装包
     * @param pk
     * @return
     */
    Result<Void> updatePackage(Package pk);

    /**
     * 查询脚本
     * @param pk
     * @return
     */
    Result<List<Package>> queryPackage(Package pk);

    /**
     * 根据id获取安装包
     * @param id
     * @return
     */
    Result<Package> getPackageById(int id);

    /**
     * 删除包
     * @param pk
     * @return
     */
    Result<Void> deletePackage(Package pk);

}
