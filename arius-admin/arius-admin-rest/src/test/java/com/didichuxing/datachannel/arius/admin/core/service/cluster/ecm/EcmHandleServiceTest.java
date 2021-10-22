package com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.EcmParamBase;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.response.EcmOperateAppBase;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.response.EcmTaskStatus;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.ecm.EcmTask;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.biz.workorder.utils.WorkOrderTaskConverter;
import com.didichuxing.datachannel.arius.admin.biz.worktask.ecm.EcmTaskManager;
import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;

/**
 * @author lyn
 * @date 2021-01-06
 */
public class EcmHandleServiceTest extends AriusAdminApplicationTests {

    @Autowired
    private EcmHandleService ecmHandleService;

    @Autowired
    private EcmTaskManager ecmTaskManager;

    @Test
    public void getESClusterStatusTest() {
        EcmTask ecmTask = ecmTaskManager.getEcmTask(392L);
        List<EcmParamBase> ecmParamBases = WorkOrderTaskConverter.convert2EcmParamBaseList(ecmTask);
        List<EcmTaskStatus> tempList = Lists.newArrayList();
        ecmParamBases
                .stream()
                .filter(Objects::nonNull)
                .forEach(r -> {
                    Result<List<EcmTaskStatus>> status = ecmHandleService.getESClusterStatus(r,
                            ecmTask.getOrderType() ,"linyunan_i");
                    if (!AriusObjUtils.isNull(status.getData())) {
                        List<EcmTaskStatus> data = status.getData();
                        tempList.addAll(data);
                    }
                });

        System.out.println(tempList);
    }

    @Test
    public void startESClusterTest(){
        EcmTask ecmTask = ecmTaskManager.getEcmTask(389L);
        List<EcmParamBase> ecmParamBases = WorkOrderTaskConverter.convert2EcmParamBaseList(ecmTask);
        ecmParamBases
                .stream()
                .filter(Objects::nonNull)
                .forEach(r -> {
                    Result<EcmOperateAppBase> result = ecmHandleService.startESCluster(r, "linyunan_i");
                });
        List<String> list = Lists.newArrayList();
        System.out.println(list);
    }

    @Test
    public void scaleESClusterTest() {
        EcmTask ecmTask = ecmTaskManager.getEcmTask(388L);
        List<EcmParamBase> ecmParamBases = WorkOrderTaskConverter.convert2EcmParamBaseList(ecmTask);
        ecmParamBases
                .stream()
                .filter(Objects::nonNull)
                .forEach(r -> {
                    Result<EcmOperateAppBase> result = ecmHandleService.scaleESCluster(r, "linyunan_i");
                });

        List<String> list = Lists.newArrayList();
        System.out.println(list);
    }

    @Test
    public void restartESClusterTest(){
        EcmTask ecmTask = ecmTaskManager.getEcmTask(388L);
        List<EcmParamBase> ecmParamBases = WorkOrderTaskConverter.convert2EcmParamBaseList(ecmTask);
        ecmParamBases
                .stream()
                .filter(Objects::nonNull)
                .forEach(r -> {
                    Result<EcmOperateAppBase> result = ecmHandleService.restartESCluster(r, "linyunan_i");
                });

        List<String> list = Lists.newArrayList();
        System.out.println(list);
    }

    @Test
    public void upgradeESClusterTest(){
        EcmTask ecmTask = ecmTaskManager.getEcmTask(388L);
        List<EcmParamBase> ecmParamBases = WorkOrderTaskConverter.convert2EcmParamBaseList(ecmTask);
        ecmParamBases
                .stream()
                .filter(Objects::nonNull)
                .forEach(r -> {
                    Result<EcmOperateAppBase> result = ecmHandleService.upgradeESCluster(r, "linyunan_i");
                });

        List<String> list = Lists.newArrayList();
        System.out.println(list);
    }

    @Test
    public void deleteESClusterTest(){
        Result result = ecmHandleService.deleteESCluster(1062L, "linyunan");
        System.out.println(result);
    }

    @Test
    public void  infoESCluster(){
        Result result = ecmHandleService.infoESCluster(1074L, "linyunan_i");
        System.out.println(result);
    }
}