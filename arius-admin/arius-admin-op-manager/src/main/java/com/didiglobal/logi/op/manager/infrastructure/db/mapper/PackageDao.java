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
     * 新建安装包
     * @param po
     * @return
     */
    int insert(PackagePO po);

    List<PackagePO> query(PackagePO po);
}
