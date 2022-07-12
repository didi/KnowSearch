package com.didiglobal.logi.op.manager.domain.packages.repository;

import com.didiglobal.logi.op.manager.domain.packages.entity.PackageGroupConfig;

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
     * 根据安装包id查询配置组
     * @param id
     * @return
     */
    List<PackageGroupConfig> queryConfigByPackageId(int id);
}
