package com.didiglobal.logi.op.manager.interfaces.assembler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.didiglobal.logi.op.manager.domain.packages.entity.Package;
import com.didiglobal.logi.op.manager.domain.packages.entity.value.PackageGroupConfig;
import com.didiglobal.logi.op.manager.infrastructure.util.ConvertUtil;
import com.didiglobal.logi.op.manager.interfaces.dto.PackageDTO;
import com.didiglobal.logi.op.manager.interfaces.vo.PackageVO;

import java.util.ArrayList;
import java.util.List;

/**
 * @author didi
 * @date 2022-07-11 2:59 下午
 */
public class PackageAssembler {

    public static Package toDO(PackageDTO dto) {
        Package pk = ConvertUtil.obj2Obj(dto, Package.class);
        if (null != dto.getGroupConfigListString()) {
            JSONArray jsonArray = JSONArray.parseArray(dto.getGroupConfigListString());
            List<PackageGroupConfig> groupConfigList = new ArrayList<>(jsonArray.size());
            for (int i=0;i<jsonArray.size();i++) {
                groupConfigList.add(JSON.toJavaObject(jsonArray.getJSONObject(i), PackageGroupConfig.class));
            }
            pk.setGroupConfigList(groupConfigList);
        }
        return pk;
    }

    public static List<PackageVO> toVOList(List<Package> packageList) {
        return ConvertUtil.list2List(packageList, PackageVO.class);
    }
}
