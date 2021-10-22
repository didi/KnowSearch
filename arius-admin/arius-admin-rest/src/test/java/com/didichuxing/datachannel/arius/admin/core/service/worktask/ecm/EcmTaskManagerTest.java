package com.didichuxing.datachannel.arius.admin.core.service.worktask.ecm;

import com.didichuxing.datachannel.arius.admin.biz.worktask.ecm.EcmTaskDetailManager;
import com.didichuxing.datachannel.arius.admin.biz.worktask.ecm.EcmTaskManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author lyn
 * @date 2021-01-07
 */
public class EcmTaskManagerTest extends AriusAdminApplicationTests {
    @Autowired
    private EcmTaskManager ecmTaskManager;

    @Autowired
    private EcmTaskDetailManager ecmTaskDetailManager;

    @Test
    public void refreshEcmTaskStatusTest() {

      //  Result<EcmTaskStatusEnum> statusEnumResult = ecmTaskManager.refreshEcmTask(392);
        //System.out.println(statusEnumResult);
    }

    @Test
    public  void startClusterEcmTaskTest(){
        Result result = ecmTaskManager.createClusterEcmTask(390L, "linyunan_i");
    }

    @Test
    public  void  continuesClusterEcmTask(){
     //   ecmTaskManager.actionClusterEcmTask(438L, EcmActionEnum.CONTINUE, null, "linyunan_i");
        //ecmTaskManager.refreshEcmTask(438L);
    }
}