package com.didichuxing.datachannel.arius.admin.v3.normal;

import com.didichuxing.datachannel.arius.admin.base.BasePhyClusterInfoTest;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.po.ecm.ESMachineNormsPO;
import com.didichuxing.datachannel.arius.admin.method.v3.normal.NormalMachineNormsControllerMethod;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

/**
 * @author wuxuan
 * @Date 2022/3/31
 */
public class NormalMachineNormsTests extends BasePhyClusterInfoTest {

    @Test
    public void testListMachineNorms() throws IOException {
        Result<List<ESMachineNormsPO>> result= NormalMachineNormsControllerMethod.listMachineNorms("masternode");
        Assert.assertTrue(result.success());
    }
    //todo
    @Test
    public void testMachineNormsDetail() throws IOException{
        Result<ESMachineNormsPO> result=NormalMachineNormsControllerMethod.machineNormsDetail(phyClusterInfo.getPhyClusterId());
        Assert.assertTrue(result.success());
    }

}
