package com.didiglobal.logi.op.manager.infrastructure.db.mapper;

import com.didiglobal.logi.op.manager.infrastructure.db.PackageGroupConfigPO;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-04 7:22 下午
 */
@Repository
public interface PackageGroupConfigDao {

    /**
     * 新增安装包默认配置组
     *
     * @param po
     */
    void insert(PackageGroupConfigPO po);

    /**
     * 批量新增安装包默认配置组
     *
     * @param poList 安装包配置组集合
     */
    void batchInsert(List<PackageGroupConfigPO> poList);

    /**
     * 根据安装包id获取配置组
     *
     * @param id 安装包id
     * @return 配置组list
     */
    List<PackageGroupConfigPO> listByPackageId(int id);


    /**
     * 根据安装包id删除配置组
     *
     * @param id 安装包id
     * @return 删除条数
     */
    int deleteByPackageId(int id);
}
