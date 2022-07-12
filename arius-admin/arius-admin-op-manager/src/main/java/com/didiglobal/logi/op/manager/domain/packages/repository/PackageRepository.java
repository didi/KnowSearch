package com.didiglobal.logi.op.manager.domain.packages.repository;

import com.didiglobal.logi.op.manager.domain.packages.entity.Package;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-11 2:23 下午
 */
public interface PackageRepository {
    /**
     * 新增安装包
     * @param pk
     * @return
     */
    int insertPackage(Package pk);

    /**
     * 查询安装包
     * @param pk
     * @return
     */
    List<Package> queryPackage(Package pk);
}
