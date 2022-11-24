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
     *
     * @param name
     * @return Package
     */
    Package findByName(String name);

    /**
     * 根据version获取安装包
     *
     * @param version
     * @return Package
     */
    Package findByVersion(String version);

    /**
     * 根据id获取安装包
     *
     * @param id
     * @return Package
     */
    Package findById(int id);

    /**
     * 新增安装包
     *
     * @param pk
     * @return int
     */
    int insertPackage(Package pk);

    /**
     * 更新安装包
     *
     * @param pk
     * @return 更新条数
     */
    int updatePackage(Package pk);

    /**
     * 查询安装包
     *
     * @param pk
     * @return List<Package>
     */
    List<Package> queryPackage(Package pk);

    /**
     * 删除包
     *
     * @param id 要删除的包对应的id
     * @return 删除条数
     */
    int deletePackage(int id);

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
     * 根据软件包类型获取软件包版本
     * @param packageType
     * @return
     */
    List<String> listPackageVersionByPackageType(Integer packageType);

    /**
     * 通过包类型和当前版本查询比当前版本低的版本
     * @param packageType
     * @param versionCast2Int
     * @return
     */
    List<Package> listPackageWithLowerVersionByPackageTypeAndVersion(Integer packageType, Integer versionCast2Int);
}
