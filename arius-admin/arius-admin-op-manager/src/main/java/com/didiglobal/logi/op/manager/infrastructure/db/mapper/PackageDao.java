package com.didiglobal.logi.op.manager.infrastructure.db.mapper;

import com.didiglobal.logi.op.manager.domain.packages.entity.Package;
import com.didiglobal.logi.op.manager.infrastructure.db.PackagePO;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-011 7:22 下午
 */
@Repository
public interface PackageDao {
    /**
     * 通过id获取PackagePo
     * @param id
     * @return PackagePo
     */
    PackagePO findById(int id);

    /**
     * 通过name获取PackagePo
     * @param name
     * @return PackagePo
     */
    PackagePO findByName(String name);

    /**
     * 通过version获取PackagePo
     * @param version
     * @return PackagePo
     */
    PackagePO findByVersion(String version);

    /**
     * 新建安装包
     * @param po
     * @return
     */
    int insert(PackagePO po);

    /**
     * 更新安装包
     * @param po
     * @return
     */
    void update(PackagePO po);
    /**
     * 查询安装包
     * @param po
     * @return List<PackagePO>
     */
    List<PackagePO> query(PackagePO po);
}
