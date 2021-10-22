package com.didichuxing.datachannel.arius.admin.biz.espackage;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESPackageDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.espackage.ESPackageVO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.espackage.ESPackage;
import com.didichuxing.datachannel.arius.admin.common.util.AriusOptional;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESPackageService;

/**
 * @author linyunan
 * @date 2021-05-19
 */
@Component
public class ESPackageManager {

    @Autowired
    private ESPackageService packageService;

    public Result<List<ESPackageVO>> listESPackage() {
        return AriusOptional
                .ofListNullable(ConvertUtil.list2List(packageService.listESPackage(), ESPackageVO.class))
                .orGetResult(() -> Result.buildFail("ES安装包列表为空"));
    }

    public Result<ESPackageVO> getESPackageById(Long id) {
        return AriusOptional
                .ofObjNullable(ConvertUtil.obj2Obj(packageService.getESPackagePOById(id), ESPackageVO.class))
                .orGetResult(() -> Result.buildFail("ES安装包不存在"));
    }

    public Result<Long> addESPackage(ESPackageDTO esPackageDTO, String operator) {
        return packageService.addESPackage(esPackageDTO, operator);
    }

    public Result<ESPackageVO> updateESPackage(ESPackageDTO esPackageDTO, String operator) {

        Result<ESPackage> esPackageResult = packageService.updateESPackage(esPackageDTO, operator);
        if (esPackageResult.failed()) {
            return Result.buildFail(esPackageResult.getMessage());
        }

        return Result.buildSucc(ConvertUtil.obj2Obj(esPackageResult.getData(), ESPackageVO.class));
    }
}
