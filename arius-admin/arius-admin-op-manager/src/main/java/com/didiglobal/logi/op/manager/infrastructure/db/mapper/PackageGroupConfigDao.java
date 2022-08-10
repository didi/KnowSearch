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
     * @param po
     */
    void insert(PackageGroupConfigPO po);

    /**
     * 根据安装包id获取配置组
     * @param id
     * @return
     */
    List<PackageGroupConfigPO> listByPackageId(int id);

    /**
     * 根据安装包id删除配置组
     * @param id
     * @return
     */
    void deleteByPackageId(int id);
}
