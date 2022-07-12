package com.didiglobal.logi.op.manager.interfaces.assembler;

import com.didiglobal.logi.op.manager.domain.packages.entity.Package;
import com.didiglobal.logi.op.manager.infrastructure.util.ConvertUtil;
import com.didiglobal.logi.op.manager.interfaces.dto.PackageDTO;
import com.didiglobal.logi.op.manager.interfaces.vo.PackageVO;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-11 2:59 下午
 */
public class PackageAssembler {

    public static Package toDO(PackageDTO dto) {
        return ConvertUtil.obj2Obj(dto, Package.class);
    }

    public static List<PackageVO> toVOList(List<Package> packageList) {
        return ConvertUtil.list2List(packageList, PackageVO.class);
    }
}
