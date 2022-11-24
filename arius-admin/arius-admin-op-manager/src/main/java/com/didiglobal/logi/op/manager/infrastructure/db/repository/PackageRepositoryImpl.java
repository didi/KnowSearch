package com.didiglobal.logi.op.manager.infrastructure.db.repository;

import com.didiglobal.logi.op.manager.domain.packages.entity.Package;
import com.didiglobal.logi.op.manager.domain.packages.repository.PackageRepository;
import com.didiglobal.logi.op.manager.infrastructure.db.PackagePO;
import com.didiglobal.logi.op.manager.infrastructure.db.converter.PackageConverter;
import com.didiglobal.logi.op.manager.infrastructure.db.mapper.PackageDao;
import com.didiglobal.logi.op.manager.infrastructure.util.ConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

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
        packageDao.insert(po);
        return po.getId();
    }

    @Override
    public int updatePackage(Package pk) {
        PackagePO po = PackageConverter.convertPackageDO2PO(pk);
        return packageDao.update(po);
    }

    @Override
    public List<Package> queryPackage(Package pk) {
        PackagePO po = PackageConverter.convertPackageDO2PO(pk);
        List<PackagePO> pos = packageDao.query(po);
        return PackageConverter.convertPackagePO2DOList(pos);
    }

    @Override
    public List<Package> pagingByCondition(Package pagingPackage, Long page, Long size) {
        PackagePO packagePO = PackageConverter.convertPackageDO2PO(pagingPackage);
        return PackageConverter.convertPackagePO2DOList(packageDao.pagingByCondition(packagePO,page,size));
    }

    @Override
    public List<Package> listPackageWithLowerVersionByPackageTypeAndVersion(Integer packageType, Integer version) {
        List<PackagePO> packageList = packageDao.listPackageWithLowerVersionByPackageTypeAndVersion(packageType,version);
        return ConvertUtil.list2List(packageList,Package.class);
    }

    @Override
    public List<String> listPackageVersionByPackageType(Integer packageType) {
        List<PackagePO> packageList = packageDao.listPackageVersionByPackageType(packageType);
        return packageList.stream().map(PackagePO::getVersion).collect(Collectors.toList());
    }

    @Override
    public Long countByCondition(Package pagingPackage) {
        PackagePO packagePO = PackageConverter.convertPackageDO2PO(pagingPackage);
        return packageDao.countByCondition(packagePO);
    }

    @Override
    public int deletePackage(int id) {
        return packageDao.delete(id);
    }
}
