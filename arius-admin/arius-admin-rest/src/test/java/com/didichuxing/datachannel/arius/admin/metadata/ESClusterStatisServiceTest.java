package com.didichuxing.datachannel.arius.admin.metadata;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import com.didichuxing.datachannel.arius.admin.common.bean.po.stats.ClusterLogicStatisPO;
import com.didichuxing.datachannel.arius.admin.metadata.service.ESClusterStatisService;

public class ESClusterStatisServiceTest extends AriusAdminApplicationTests {

    @Autowired
    private ESClusterStatisService esClusterStatisService;

    @Test
    public void getPhyClusterStatisticsInfo(){
        String clusterName = "dc-es02";
        ClusterLogicStatisPO clusterStatisPO = esClusterStatisService.getPhyClusterStatisticsInfo(clusterName);

        Assert.assertNotNull(clusterStatisPO);
    }
}
