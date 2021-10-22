package com.didichuxing.datachannel.arius.admin.core.service.worktask.ecm;

import com.didichuxing.datachannel.arius.admin.biz.worktask.ecm.EcmTaskDetailManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.EcmTaskDetail;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.EcmTaskDetailProgress;
import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author lyn
 * @date 2021-01-06
 */
public class EcmTaskDetailManagerTest extends AriusAdminApplicationTests {
    @Autowired
    private EcmTaskDetailManager ecmTaskDetailManager;

    @Test
    public void test01(){
        EcmTaskDetailProgress ecmTaskDetail01 = ecmTaskDetailManager.buildInitialEcmTaskDetail(401L);
        System.out.println(ecmTaskDetail01);

        EcmTaskDetailProgress ecmTaskDetail02 = ecmTaskDetailManager.buildInitialEcmTaskDetail(387L);
        System.out.println(ecmTaskDetail02);

        EcmTaskDetailProgress ecmTaskDetail03 = ecmTaskDetailManager.buildInitialEcmTaskDetail(167L);
        System.out.println(ecmTaskDetail03);
    }

    @Test
    public void test02(){
        EcmTaskDetail ecmTaskDetail = new EcmTaskDetail();
        ecmTaskDetail.setId(1532L);
        ecmTaskDetail.setStatus("running");
        ecmTaskDetail.setRole("masternode");
        ecmTaskDetail.setWorkOrderTaskId(11L);
        ecmTaskDetail.setIdx(1);
        ecmTaskDetail.setGrp(0);
        ecmTaskDetail.setTaskId(123L);
        ecmTaskDetail.setHostname("linyunan.docker.ys");
        Assert.assertNotNull( ecmTaskDetailManager.editEcmTaskDetail(ecmTaskDetail).getData());

    }

}