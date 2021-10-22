package com.didichuxing.datachannel.arius.admin.biz.espackage;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.espackage.ESPackage;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.google.common.collect.Lists;
import java.util.List;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.espackage.ESPackageVO;
import com.didichuxing.datachannel.arius.admin.common.util.AriusOptional;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESPackageService;
import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;

/**
 * @author linyunan
 * @date 2021-05-21
 */
public class ESPackageManagerTest extends AriusAdminApplicationTests {

    @Autowired
    private ESPackageService packageService;

    @Test
    public void ofObjNullableTest() {
        Result<ESPackageVO> result = AriusOptional
            .ofObjNullable(ConvertUtil.obj2Obj(packageService.getESPackagePOById(1L), ESPackageVO.class))
            .orGetResult(() -> Result.buildFail("ES安装包不存在"));
        ESPackageVO data = result.getData();
        Assert.assertNotNull(data);
    }

    @Test
    public void ofListNullableTest() {
        List<ESPackage> esPackages = Lists.newArrayList();
        List<ESPackageVO> esPackageVOS = ConvertUtil.list2List(esPackages, ESPackageVO.class);
        Result<List<ESPackageVO>> result = AriusOptional.ofListNullable(esPackageVOS)
            .orGetResult(() -> Result.buildFail("安装包列表为空"));
        List<ESPackageVO> data = result.getData();
        Assert.assertNotNull(data);
    }
}