package com.didichuxing.datachannel.arius.admin.biz.espackage;

import java.util.List;
import java.util.stream.Collectors;

import com.didichuxing.datachannel.arius.admin.client.constant.espackage.AriusESPackageEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ESVersionUtil;
import org.apache.commons.collections4.CollectionUtils;
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
        List<ESPackage> esPackageList = packageService.listESPackage();
        if (CollectionUtils.isEmpty(esPackageList)) {
            return Result.buildSucc();
        }

        return Result.buildSucc(esPackageList.stream().map(this::buildESPackageVO).collect(Collectors.toList()));
    }

    public Result<ESPackageVO> getESPackageById(Long id) {
        return AriusOptional
                .ofObjNullable(buildESPackageVO(packageService.getESPackagePOById(id)))
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

        return Result.buildSucc(buildESPackageVO(esPackageResult.getData()));
    }

    private ESPackageVO buildESPackageVO(ESPackage esPackage) {
        ESPackageVO esPackageVO = ConvertUtil.obj2Obj(esPackage, ESPackageVO.class);

        // 根据es程序包的版本号判断是否为滴滴内部版本，当版本号为四位时，表示为滴滴内部版本，否则为外部开源的版本
        esPackageVO.setPackageType(AriusESPackageEnum.valueOfLength(ESVersionUtil.getVersionLength(esPackage.getEsVersion())).getCode());

        return esPackageVO;
    }
}
