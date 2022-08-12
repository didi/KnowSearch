package com.didiglobal.logi.op.manager.domain.packages.repository;

import com.didiglobal.logi.op.manager.domain.packages.entity.value.PackageGroupConfig;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-11 2:24 下午
 */
public interface PackageGroupConfigRepository {

    /**
     * 新增安装包默认配置
     * @param packageGroupConfig
     */
    void insertPackageGroupConfig(PackageGroupConfig packageGroupConfig);

    /**
     * 批量新增安装包默认配置
     * @param packageGroupConfigs 安装包默认配置组
     */
    void batchInsertPackageGroupConfig(List<PackageGroupConfig> packageGroupConfigs);

    /**
     * 根据安装包id查询配置组
     * @param id
     * @return
     */
    List<PackageGroupConfig> queryConfigByPackageId(int id);

    /**
     * 根据安装包id删除配置组
     * @param id
     * @return
     */
    void deleteByPackageId(int id);
}
