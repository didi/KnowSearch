package com.didichuxing.datachannel.arius.admin.biz.worktask.handler;

import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.biz.workorder.utils.WorkOrderTaskConverter;
import com.didichuxing.datachannel.arius.admin.biz.workorder.WorkOrderManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.EcmParamBase;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.EsConfigAction;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.elasticcloud.ElasticCloudCommonActionParam;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.host.HostParamBase;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESClusterDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.task.WorkTaskProcessDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.task.ecm.EcmTaskDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.order.detail.OrderDetailBaseVO;
import com.didichuxing.datachannel.arius.admin.client.constant.ecm.EcmTaskStatusEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.ecm.EcmTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.esconfig.EsConfigActionEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterTypeEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.task.WorkTaskStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.esconfig.ESConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.WorkTask;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.detail.AbstractTaskDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.ecm.EcmTask;
import com.didichuxing.datachannel.arius.admin.common.bean.po.order.WorkOrderPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.task.ecm.EcmTaskPO;
import com.didichuxing.datachannel.arius.admin.common.constant.order.OperationTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.event.ecm.EcmTaskEditEvent;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.BaseHttpUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESClusterConfigService;
import com.didichuxing.datachannel.arius.admin.biz.worktask.WorkTaskHandler;
import com.didichuxing.datachannel.arius.admin.biz.worktask.WorkTaskManager;
import com.didichuxing.datachannel.arius.admin.biz.worktask.ecm.EcmTaskManager;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESPluginService;
import com.didichuxing.datachannel.arius.admin.biz.workorder.content.PhyClusterPluginOperationContent;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service("ecmWorkTaskHandler")
public class EcmWorkTaskHandler implements WorkTaskHandler, ApplicationListener<EcmTaskEditEvent> {

    private static final ILog      LOGGER = LogFactory.getLog(EcmWorkTaskHandler.class);

    @Autowired
    private EcmTaskManager ecmTaskManager;

    @Autowired
    private WorkTaskManager workTaskManager;

    @Autowired
    private ESClusterConfigService esClusterConfigService;

    @Autowired
    private WorkOrderManager workOrderManager;

    @Autowired
    private ESPluginService        esPluginService;

    @Autowired
    private ESClusterPhyService      esClusterPhyService;

    @Override
    public Result<WorkTask> addTask(WorkTask workTask) {
        if (AriusObjUtils.isNull(workTask.getExpandData())) {
            return Result.buildParamIllegal("提交内容为空");
        }

        EcmTaskDTO ecmTaskDTO = ConvertUtil.str2ObjByJson(workTask.getExpandData(), EcmTaskDTO.class);
        Result<Long> ret = ecmTaskManager.saveEcmTask(ecmTaskDTO);
        if (null == ret || ret.failed()) {
            return Result.buildFail("生成集群新建操作任务失败!");
        }

        workTask.setBusinessKey(ret.getData().intValue());
        workTask.setTitle(ecmTaskDTO.getTitle());
        workTask.setCreateTime(new Date());
        workTask.setUpdateTime(new Date());
        workTask.setStatus(WorkTaskStatusEnum.WAITING.getStatus());
        workTask.setDeleteFlag(false);
        workTaskManager.insert(workTask);

        return Result.buildSucc(workTask);
    }

    @Override
    public boolean existUnClosedTask(Integer key, Integer type) {
        return ecmTaskManager.existUnClosedEcmTask(key.longValue());
    }

    @Override
    public Result process(WorkTask workTask, Integer step, String status, String expandData) {
        if (AriusObjUtils.isNull(workTask.getExpandData())) {
            return Result.buildParamIllegal("提交内容为空");
        }

        EcmTaskPO ecmTaskPO = JSONObject.parseObject(workTask.getExpandData(), EcmTaskPO.class);

        workTask.setStatus(status);
        workTask.setUpdateTime(new Date());
        workTask.setExpandData(JSONObject.toJSONString(ecmTaskPO));
        workTaskManager.updateTaskById(workTask);

        return Result.buildSucc();
    }

    @Override
    public AbstractTaskDetail getTaskDetail(String extensions) {
        return null;
    }

    @Override
    public void onApplicationEvent(EcmTaskEditEvent event) {
        EcmTask ecmTask = event.getEditTask();

        if (null == ecmTask) {
            return;
        }

        handlerRestartConfig(ecmTask);
        handlerRestartPlugin(ecmTask);

        Result<WorkTask> result = workTaskManager.getLatestTask(ecmTask.getId().intValue(), ecmTask.getOrderType());
        if (result.failed()) {
            return;
        }
        WorkTaskProcessDTO processDTO = new WorkTaskProcessDTO();
        processDTO.setStatus(ecmTask.getStatus());
        processDTO.setTaskId(result.getData().getId());
        processDTO.setExpandData(JSONObject.toJSONString(ecmTask));
        workTaskManager.processTask(processDTO);

        LOGGER.info("method=onApplicationEvent||ecmTaskId={}||event=EcmEditTaskEvent", ecmTask.getId());
    }

    /**
     * Ecm重启操作后处理Es集群配置相关信息
     * @param ecmTask
     */
    private void handlerRestartConfig(EcmTask ecmTask) {
        //1.判断是不是重启类型的工单
        if (EcmTaskTypeEnum.RESTART.getCode() != ecmTask.getOrderType()) {
            return;
        }
        //2.判断重启类型是否成功
        if (!EcmTaskStatusEnum.SUCCESS.getValue().equals(ecmTask.getStatus())) {
            return;
        }

        //3.判断是不是配置重启类型的工单, configIds为空则为非配置重启
        List<Long> invalidEsConfigIds = Lists.newArrayList();
        Integer actionType = Integer.MIN_VALUE;
        List<EcmParamBase> ecmParamBaseList = WorkOrderTaskConverter.convert2EcmParamBaseList(ecmTask);

        if (ESClusterTypeEnum.ES_HOST.getCode() == ecmTask.getType()) {
            List<HostParamBase> hostParamBases = ConvertUtil.list2List(ecmParamBaseList, HostParamBase.class);
            actionType = hostParamBases.stream().map(HostParamBase::getEsConfigAction)
                .map(EsConfigAction::getActionType).findAny().orElse(null);

            hostParamBases.stream()
                .filter(r -> !AriusObjUtils.isNull(r) && !AriusObjUtils.isNull(r.getEsConfigAction())
                             && CollectionUtils.isNotEmpty(r.getEsConfigAction().getInvalidEsConfigIds()))
                .forEach(param -> invalidEsConfigIds.addAll(param.getEsConfigAction().getInvalidEsConfigIds()));

        } else if (ESClusterTypeEnum.ES_DOCKER.getCode() == ecmTask.getType()) {
            List<ElasticCloudCommonActionParam> cloudCommonActionParams = ConvertUtil.list2List(ecmParamBaseList,
                ElasticCloudCommonActionParam.class);
            actionType = cloudCommonActionParams.stream().map(ElasticCloudCommonActionParam::getEsConfigActions)
                .map(EsConfigAction::getActionType).findAny().orElse(null);

            cloudCommonActionParams.stream()
                .filter(r -> !AriusObjUtils.isNull(r) && !AriusObjUtils.isNull(r.getEsConfigActions())
                             && CollectionUtils.isNotEmpty(r.getEsConfigActions().getInvalidEsConfigIds()))
                .forEach(param -> invalidEsConfigIds.addAll(param.getEsConfigActions().getInvalidEsConfigIds()));
        } else {
            LOGGER.error(
                "class=EcmWorkTaskHandler||method=handlerRestartConfig||ecmTaskId={}||msg=Type does not exist, require docker or host",
                ecmTask.getId());
        }

        if (CollectionUtils.isEmpty(invalidEsConfigIds)) {
            return;
        }

        try {
            //4.任务成功进行配置回写处理
            handleSuccessEcmConfigRestartTask(actionType, invalidEsConfigIds, ecmTask);
        } catch (Exception e) {
            LOGGER.error("class=EcmWorkTaskHandler||method=handlerRestartConfig||ecmTaskId={}||msg={}", ecmTask.getId(),
                e.getStackTrace());
        }

    }

    private void handleSuccessEcmConfigRestartTask(Integer actionType, List<Long> invalidEsConfigIds, EcmTask ecmTask) {
        for (Long invalidEsConfigId : invalidEsConfigIds) {
            ESConfig esConfig = esClusterConfigService.getEsConfigById(invalidEsConfigId);
            if (AriusObjUtils.isNull(esConfig)) {
                LOGGER.error(
                    "class=EcmWorkTaskHandler||method=handlerRestartConfig||ecmTaskId={}||clusterId={}||msg=es config does not exist",
                    ecmTask.getId(), ecmTask.getPhysicClusterId());
                return;
            }
            //删除操作, 至当前版本为无效
            if (EsConfigActionEnum.DELETE.getCode() == actionType) {
                Result result = esClusterConfigService.deleteEsClusterConfig(esConfig.getId(), ecmTask.getCreator());
                if (result.failed()) {
                    LOGGER.error(
                        "class=EcmWorkTaskHandler||method=handlerRestartConfig||ecmTaskId={}||clusterId={}||msg=fail to set new config valid",
                        ecmTask.getId(), ecmTask.getPhysicClusterId());
                }
                return;
            }

            //编辑操作, 设置当前版本为有效, 原版本为无效
            //TODO：待优化当前版本有效后,宕机了
            if (esConfig.getVersionConfig() > 1) {
                Result result = esClusterConfigService.setConfigValid(invalidEsConfigId);
                if (result.failed()) {
                    LOGGER.error(
                        "class=EcmWorkTaskHandler||method=handlerRestartConfig||ecmTaskId={}||clusterId={}||msg=fail to set edit config valid",
                        ecmTask.getId(), ecmTask.getPhysicClusterId());
                    return;
                }
                esClusterConfigService.setOldConfigInvalid(esConfig);

                //增加操作, 设置当前版本为有效
            } else if (esConfig.getVersionConfig() == 1) {
                Result result = esClusterConfigService.setConfigValid(invalidEsConfigId);
                if (result.failed()) {
                    LOGGER.error(
                        "class=EcmWorkTaskHandler||method=handlerRestartConfig||ecmTaskId={}||clusterId={}||msg=fail to set new config valid",
                        ecmTask.getId(), ecmTask.getPhysicClusterId());
                }
            }
        }
    }

    /**
     * Ecm重启操作后处理Es集群插件相关信息
     * @param ecmTask 任务
     */
    private void handlerRestartPlugin(EcmTask ecmTask) {
        // 1.判断是不是插件操作类型的工单
        if (EcmTaskTypeEnum.PLUG_OPERATION.getCode() != ecmTask.getOrderType()) {
            return;
        }
        // 2.判断重启类型是否成功
        if (!EcmTaskStatusEnum.SUCCESS.getValue().equals(ecmTask.getStatus())) {
            return;
        }
        try {
            // 3.任务成功进行插件回写处理
            handleSuccessEcmPluginRestartTask(ecmTask);
        } catch (Exception e) {
            LOGGER.error(
                    "class=EcmWorkTaskHandler||method=handlerRestartPlugin||ecmTaskId={}||msg={}",
                    ecmTask.getId(),
                    e.getStackTrace());
        }
    }

    /**
     * 当因插件操作任务而重启集群成功后，进行一些插件信息的回写
     * @param ecmTask 任务
     */
    private void handleSuccessEcmPluginRestartTask(EcmTask ecmTask) {
        OrderDetailBaseVO orderDetailBaseVO = workOrderManager.getById(ecmTask.getWorkOrderId()).getData();
        PhyClusterPluginOperationContent content = JSONObject.parseObject(orderDetailBaseVO.getDetail(), PhyClusterPluginOperationContent.class);

        ESClusterPhy esClusterPhy = esClusterPhyService.getClusterById(ecmTask.getPhysicClusterId().intValue());
        List<Long> plugIdList = ListUtils.string2LongList(esClusterPhy.getPlugIds());

        // 获取es集群插件信息
        // todo 放到service中形成一个方法
        // TODO 搞个类ESResponsePluginInfo: name, component, version
        final String[] esClusterReadAddressList = esClusterPhy.getHttpAddress().split(",");
        final Map<String, List<String>> node2PluginMap = new HashMap<>();
        for (String readAddress : esClusterReadAddressList) {
            // todo url后加 ?format=json 直接返回json串，然后解析即可
            String url = "http://" + readAddress + "/_cat/plugins";
            final String response = BaseHttpUtil.get(url, null);
            final String[] plugins = response.split("\n");
            for (String plugin : plugins) {
                final String[] strings = plugin.split(" ");
                String nodeName = strings[0];
                String pluginName = strings[1];
                node2PluginMap.computeIfAbsent(nodeName, key -> new ArrayList<>());
                node2PluginMap.get(nodeName).add(pluginName);
            }
            if (!node2PluginMap.isEmpty()) {
                break;
            }
        }
        final String pluginName = esPluginService.getESPluginById(content.getPluginId()).getName();

        if (OperationTypeEnum.INSTALL.getCode().equals(content.getOperationType())) {
            for (Map.Entry<String, List<String>> entry : node2PluginMap.entrySet()) {
                final List<String> pluginList = entry.getValue();
                if (!pluginList.contains(pluginName)) {
                    LOGGER.warn("节点{}安装插件{}失败", entry.getKey(), pluginName);
                    return;
                }
            }

            // 将插件信息同步到物理集群中
            plugIdList.add(content.getPluginId());
            // 将插件信息置为 已安装
            esPluginService.installESPlugin(content.getPluginId());
        } else if (OperationTypeEnum.UNINSTALL.getCode().equals(content.getOperationType())) {
            for (Map.Entry<String, List<String>> entry : node2PluginMap.entrySet()) {
                final List<String> pluginList = entry.getValue();
                if (pluginList.contains(pluginName)) {
                    LOGGER.warn("节点{}卸载插件{}失败", entry.getKey(), pluginName);
                    return;
                }
            }
            // 将插件信息同步到物理集群中
            plugIdList.remove(content.getPluginId());
            // 将插件信息置为 未安装
            esPluginService.uninstallESPlugin(content.getPluginId());
        }
        esClusterPhy.setPlugIds(ListUtils.longList2String(plugIdList.stream().distinct().collect(Collectors.toList())));
        esClusterPhyService.editCluster(ConvertUtil.obj2Obj(esClusterPhy, ESClusterDTO.class), orderDetailBaseVO.getApplicant().getName());
    }
}
