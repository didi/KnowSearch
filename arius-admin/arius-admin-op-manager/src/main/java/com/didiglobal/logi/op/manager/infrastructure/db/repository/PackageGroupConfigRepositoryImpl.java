package com.didiglobal.logi.op.manager.infrastructure.db.repository;

import com.didiglobal.logi.op.manager.domain.packages.entity.value.PackageGroupConfig;
import com.didiglobal.logi.op.manager.domain.packages.repository.PackageGroupConfigRepository;
import com.didiglobal.logi.op.manager.infrastructure.db.PackageGroupConfigPO;
import com.didiglobal.logi.op.manager.infrastructure.db.converter.PackageGroupConfigConverter;
import com.didiglobal.logi.op.manager.infrastructure.db.mapper.PackageGroupConfigDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-11 2:28 下午
 */
@Repository
public class PackageGroupConfigRepositoryImpl implements PackageGroupConfigRepository {

    @Autowired
    private PackageGroupConfigDao packageGroupConfigDao;

    @Override
    public void insertPackageGroupConfig(PackageGroupConfig packageGroupConfig) {
        PackageGroupConfigPO po = PackageGroupConfigConverter.convertScriptDO2PO(packageGroupConfig);
        packageGroupConfigDao.insert(po);
    }

    @Override
    public List<PackageGroupConfig> queryConfigByPackageId(int id) {
        List<PackageGroupConfigPO> list = packageGroupConfigDao.listByPackageId(id);
        return PackageGroupConfigConverter.convertScriptPO2DOList(list);
    }
}
