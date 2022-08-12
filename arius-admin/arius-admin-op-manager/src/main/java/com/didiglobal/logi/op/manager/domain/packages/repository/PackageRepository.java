package com.didiglobal.logi.op.manager.domain.packages.repository;

import com.didiglobal.logi.op.manager.domain.packages.entity.Package;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-11 2:23 下午
 */
public interface PackageRepository {

    /**
     * 根据name获取安装包
     * @param name
     * @return Package
     */
    Package findByName(String name);

    /**
     * 根据version获取安装包
     * @param version
     * @return Package
     */
    Package findByVersion(String version);

    /**
     * 根据id获取安装包
     * @param id
     * @return Package
     */
    Package findById(int id);

    /**
     * 新增安装包
     * @param pk
     * @return int
     */
    int insertPackage(Package pk);

    /**
     * 更新安装包
     * @param pk
     */
    void updatePackage(Package pk);

    /**
     * 查询安装包
     * @param pk
     * @return List<Package>
     */
    List<Package> queryPackage(Package pk);

    /**
     * 删除包
     * @param id 要删除的包对应的id
     */
    void deletePackage(int id);
}
