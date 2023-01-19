package com.didiglobal.logi.op.manager.infrastructure.db.converter;

import com.didiglobal.logi.op.manager.domain.packages.entity.value.PackageGroupConfig;
import com.didiglobal.logi.op.manager.infrastructure.db.PackageGroupConfigPO;
import com.didiglobal.logi.op.manager.infrastructure.util.ConvertUtil;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-11 5:02 下午
 */
public class PackageGroupConfigConverter {

    public static PackageGroupConfigPO convertPackageDO2PO(PackageGroupConfig packageGroupConfig) {
        return ConvertUtil.obj2Obj(packageGroupConfig, PackageGroupConfigPO.class);
    }

    public static List<PackageGroupConfig> convertPackagePO2DOList(List<PackageGroupConfigPO> poList) {
        return ConvertUtil.list2List(poList, PackageGroupConfig.class);
    }

    public static List<PackageGroupConfigPO> convertPackageDOList2POList(List<PackageGroupConfig> dolist) {
        return ConvertUtil.list2List(dolist,PackageGroupConfigPO.class);
    }
}
