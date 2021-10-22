package com.didichuxing.datachannel.arius.admin.core.service.worktask.handler;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.EcmParamBase;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.EsConfigAction;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.elasticcloud.ElasticCloudCommonActionParam;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.host.HostParamBase;
import com.didichuxing.datachannel.arius.admin.client.constant.ecm.EcmTaskStatusEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.ecm.EcmTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.esconfig.EsConfigActionEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.esconfig.ESConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.ecm.EcmTask;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESClusterConfigService;
import com.didichuxing.datachannel.arius.admin.biz.workorder.utils.WorkOrderTaskConverter;
import com.didichuxing.datachannel.arius.admin.biz.worktask.ecm.EcmTaskManager;
import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;

/**
 * @author lyn
 * @date 2021-01-26
 */
public class EcmWorkTaskHandlerTest extends AriusAdminApplicationTests {
    @Autowired
    private EcmTaskManager ecmTaskManager;

    @Autowired
    private ESClusterConfigService  esClusterConfigService;

    @Test
    public void onApplicationEventTest(){
        EcmTask ecmTask = ecmTaskManager.getEcmTask(488L);

        //1.判断是不是重启类型的工单
        if (EcmTaskTypeEnum.RESTART.getCode() != ecmTask.getOrderType()) {
            return;
        }
        //2.判断重启类型是否成功
        if(!EcmTaskStatusEnum.SUCCESS.getValue().equals(ecmTask.getStatus())){
            return;
        }

        //3.判断是不是配置重启类型的工单, configIds为空则为非配置重启
        List<Long> configIds = Lists.newArrayList();
        Integer actionType = Integer.MIN_VALUE;
        List<EcmParamBase> ecmParamBaseList = WorkOrderTaskConverter.convert2EcmParamBaseList(ecmTask);

        if (ESClusterTypeEnum.ES_HOST.getCode() == ecmTask.getType()) {
            List<HostParamBase> hostParamBases = ConvertUtil.list2List(ecmParamBaseList, HostParamBase.class);
            actionType  = hostParamBases
                    .stream()
                    .map(HostParamBase::getEsConfigAction)
                    .map(EsConfigAction::getActionType)
                    .findAny()
                    .orElse(null);

            hostParamBases
                    .stream()
                    .filter(r -> !AriusObjUtils.isNull(r)
                            && !AriusObjUtils.isNull(r.getEsConfigAction())
                            && CollectionUtils.isNotEmpty(r.getEsConfigAction()
                            .getInvalidEsConfigIds()))
                    .forEach(param -> configIds.addAll(param.getEsConfigAction().getInvalidEsConfigIds()));
        } else if (ESClusterTypeEnum.ES_DOCKER.getCode() == ecmTask.getType()) {
            List<ElasticCloudCommonActionParam> cloudCommonActionParams = ConvertUtil.list2List(ecmParamBaseList,
                    ElasticCloudCommonActionParam.class);
            actionType  = cloudCommonActionParams
                    .stream()
                    .map(ElasticCloudCommonActionParam::getEsConfigActions)
                    .map(EsConfigAction::getActionType)
                    .findAny()
                    .orElse(null);

            cloudCommonActionParams
                    .stream()
                    .filter(r -> !AriusObjUtils.isNull(r)
                            && !AriusObjUtils.isNull(r.getEsConfigActions())
                            && CollectionUtils.isNotEmpty(r.getEsConfigActions()
                            .getInvalidEsConfigIds()))
                    .forEach(param -> configIds.addAll(param.getEsConfigActions().getInvalidEsConfigIds()));
        }

        if(CollectionUtils.isEmpty(configIds)){
            return;
        }

        //4.任务成功进行配置回写处理
        handleSuccessEcmConfigRestartTask(actionType, configIds, ecmTask);
    }

    private void handleSuccessEcmConfigRestartTask(Integer actionType, List<Long> configIds, EcmTask ecmTask) {
        configIds.stream()
                .filter(Objects::nonNull)
                .forEach(id -> {
                    ESConfig esConfig = esClusterConfigService.getEsConfigById(id);
                    if (AriusObjUtils.isNull(esConfig)) {
                        return;
                    }
                    //删除操作, 至当前版本为无效
                    if (EsConfigActionEnum.DELETE.getCode() == actionType) {
                        Result result = esClusterConfigService.deleteEsClusterConfig(esConfig.getId(), ecmTask.getCreator());
                        return;
                    }

                    //编辑操作, 设置当前版本为有效, 先前版本为无效
                    if (esConfig.getVersionConfig() > 1) {
                        Result result = esClusterConfigService.setConfigValid(id);
                        if (result.failed()) {
                            return;
                        }
                        esClusterConfigService.setOldConfigInvalid(esConfig);

                        //增加操作, 设置当前版本为有效
                    } else if (esConfig.getVersionConfig() == 1) {
                        Result result = esClusterConfigService.setConfigValid(id);
                    }
                });
    }

}