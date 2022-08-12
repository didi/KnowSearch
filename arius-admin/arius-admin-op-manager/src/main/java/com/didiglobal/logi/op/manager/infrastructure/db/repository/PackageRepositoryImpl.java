package com.didiglobal.logi.op.manager.infrastructure.db.repository;

import com.didiglobal.logi.op.manager.domain.packages.entity.Package;
import com.didiglobal.logi.op.manager.domain.packages.repository.PackageRepository;
import com.didiglobal.logi.op.manager.infrastructure.db.PackagePO;
import com.didiglobal.logi.op.manager.infrastructure.db.converter.PackageConverter;
import com.didiglobal.logi.op.manager.infrastructure.db.mapper.PackageDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-11 2:28 下午
 */
@Repository
public class PackageRepositoryImpl implements PackageRepository {

    @Autowired
    private PackageDao packageDao;

    @Override
    public Package findByName(String name) {
        PackagePO packagePO = packageDao.findByName(name);
        return PackageConverter.convertPackagePO2DO(packagePO);
    }

    @Override
    public Package findByVersion(String version) {
        PackagePO packagePO = packageDao.findByVersion(version);
        return PackageConverter.convertPackagePO2DO(packagePO);
    }

    @Override
    public Package findById(int id) {
        PackagePO packagePO = packageDao.findById(id);
        return PackageConverter.convertPackagePO2DO(packagePO);
    }

    @Override
    public int insertPackage(Package pk) {
        PackagePO po = PackageConverter.convertPackageDO2PO(pk);
        return packageDao.insert(po);
    }

    @Override
    public void updatePackage(Package pk) {
        PackagePO po = PackageConverter.convertPackageDO2PO(pk);
        packageDao.update(po);
    }

    @Override
    public List<Package> queryPackage(Package pk) {
        PackagePO po = PackageConverter.convertPackageDO2PO(pk);
        List<PackagePO> pos = packageDao.query(po);
        return PackageConverter.convertPackagePO2DOList(pos);
    }

    @Override
    public void deletePackage(int id) {
        packageDao.delete(id);
    }
}
