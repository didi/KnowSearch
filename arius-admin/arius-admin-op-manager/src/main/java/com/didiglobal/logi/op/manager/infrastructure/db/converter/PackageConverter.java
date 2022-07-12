package com.didiglobal.logi.op.manager.infrastructure.db.converter;

import com.didiglobal.logi.op.manager.domain.packages.entity.Package;
import com.didiglobal.logi.op.manager.infrastructure.db.PackagePO;
import com.didiglobal.logi.op.manager.infrastructure.util.ConvertUtil;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-11 4:38 下午
 */
public class PackageConverter {

    public static PackagePO convertPackageDO2PO(Package pk) {
        return ConvertUtil.obj2Obj(pk, PackagePO.class);
    }

    public static List<Package> convertPackagePO2DOList(List<PackagePO> poList) {
        return ConvertUtil.list2List(poList, Package.class);
    }
}
