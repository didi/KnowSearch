package com.didichuxing.datachannel.arius.admin.biz.task.handler.cluster;

import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterTypeEnum.ES_DOCKER;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.task.content.ClusterIndecreaseDockerContent;
import com.didichuxing.datachannel.arius.admin.biz.task.content.ClusterIndecreaseHostContent;
import com.didichuxing.datachannel.arius.admin.biz.workorder.utils.OpOrderTaskConverter;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord.Builder;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.ESClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.EcmParamBase;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.host.HostParamBase;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.host.HostScaleActionParam;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.ecm.EcmTaskDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.Getter;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 集群扩缩容
 *
 * @author ohushenglin_v
 * @date 2022-05-20
 */
@Service("clusterScaleTaskHandler")
public class ClusterScaleTaskHandler extends AbstractClusterTaskHandler {

    @Override
    Result<Void> initHostParam(OpTask opTask) {
        ClusterIndecreaseHostContent clusterOpIndecreaseHostContent = ConvertUtil.str2ObjByJson(opTask.getExpandData(),
            ClusterIndecreaseHostContent.class);
        // 如果当前角色对应pid_count为null，则设置为默认值1
        if (null == clusterOpIndecreaseHostContent.getPidCount()) {
            clusterOpIndecreaseHostContent.setPidCount(ClusterConstant.DEFAULT_CLUSTER_PAID_COUNT);
        }

        // 填充工单中的ip字段,port端口号填充
        Map<String, String> portOfRoleMapFromHost = getPortOfRoleMapFromHost(
            clusterOpIndecreaseHostContent.getPhyClusterId());
        for (ESClusterRoleHost esClusterRoleHost : clusterOpIndecreaseHostContent.getClusterRoleHosts()) {
            esClusterRoleHost.setIp(Getter.strWithDefault(esClusterRoleHost.getIp(), esClusterRoleHost.getHostname()));
            esClusterRoleHost.setPort(portOfRoleMapFromHost.get(esClusterRoleHost.getRole()));
        }

        opTask.setExpandData(JSON.toJSONString(clusterOpIndecreaseHostContent));
        return Result.buildSucc();
    }

    @Override
    Result<Void> validateHostParam(String param) throws NotFindSubclassException {
        ClusterIndecreaseHostContent content = ConvertUtil.str2ObjByJson(param, ClusterIndecreaseHostContent.class);

        if (AriusObjUtils.isNull(content.getPhyClusterId())) {
            return Result.buildParamIllegal("物理集群id为空");
        }

        ClusterPhy clusterPhy = clusterPhyService.getClusterById(content.getPhyClusterId().intValue());
        if (AriusObjUtils.isNull(clusterPhy)) {
            return Result.buildParamIllegal("物理集群不存在");
        }

        if (opTaskManager.existUnClosedTask(content.getPhyClusterId().intValue(),
            OpTaskTypeEnum.CLUSTER_EXPAND.getType())
            || opTaskManager.existUnClosedTask(content.getPhyClusterId().intValue(),
                OpTaskTypeEnum.CLUSTER_SHRINK.getType())) {
            return Result.buildParamIllegal("该集群上存在未完成的集群扩缩容任务");
        }

        // 对于datanode的缩容，如果该节点上存在数据分片,做出警告
        if (content.getOperationType() == OpTaskTypeEnum.CLUSTER_SHRINK.getType()) {
            Map<String, Integer> segmentsOfIpByCluster = null;
            try {
                segmentsOfIpByCluster = esClusterService
                        .synGetSegmentsOfIpByCluster(content.getPhyClusterName());
            } catch (ESOperateException e) {
                LOGGER.error("class=ClusterScaleTaskHandler||method=validateHostParam||errMsg=fail to get get segments of ip by cluster",
                        e);
                Result.buildFail("获取集群ip上的segment数目异常");
            }

            for (ESClusterRoleHost esClusterRoleHost : content.getClusterRoleHosts()) {
                if (esClusterRoleHost.getRole().equals(ESClusterNodeRoleEnum.DATA_NODE.getDesc())
                        && segmentsOfIpByCluster.containsKey(esClusterRoleHost.getHostname())
                        && !segmentsOfIpByCluster.get(esClusterRoleHost.getHostname()).equals(0)) {
                    return Result.buildFail("数据节点上存在分片，请迁移分片之后再进行该节点的缩容");
                }
            }
        }
        return Result.buildSucc();
    }

    @Override
    Result<Void> buildHostEcmTaskDTO(EcmTaskDTO ecmTaskDTO, String param, String creator) {
        ClusterIndecreaseHostContent content = ConvertUtil.str2ObjByJson(param, ClusterIndecreaseHostContent.class);
        ecmTaskDTO.setPhysicClusterId(content.getPhyClusterId());
        ecmTaskDTO.setOrderType(content.getOperationType());

        final Result<List<EcmParamBase>> hostScaleParamBaseListResult = getHostScaleParamBaseList(
            content.getPhyClusterId().intValue(), content.getClusterRoleHosts(), content.getPidCount());
        if (hostScaleParamBaseListResult.failed()) {
            return Result.buildFrom(hostScaleParamBaseListResult);
        }
        List<EcmParamBase> hostScaleParamBaseList = hostScaleParamBaseListResult.getData();

        ecmTaskDTO.setClusterNodeRole(ListUtils.strList2String(
            hostScaleParamBaseList.stream().map(EcmParamBase::getRoleName).collect(Collectors.toList())));
        ecmTaskDTO.setEcmParamBaseList(hostScaleParamBaseList);
        String hostOption=content.getOperationType()==2?"扩容":"缩容";
        if (CollectionUtils.isEmpty(content.getOriginClusterRoleHosts())) {
            return Result.buildFail("集群角色对应的原始的主机列表不能为空");
        }
        final String clientNode = content.getOriginClusterRoleHosts().stream()
                .filter(esClusterRoleHost -> StringUtils.equalsIgnoreCase(esClusterRoleHost.getRole(), "clientnode"))
                .map(ESClusterRoleHost::getHostname).distinct().collect(Collectors.joining(","));
    
        final String datanode = content.getOriginClusterRoleHosts().stream()
                .filter(esClusterRoleHost -> StringUtils.equalsIgnoreCase(esClusterRoleHost.getRole(), "datanode"))
                .map(ESClusterRoleHost::getHostname).distinct().collect(Collectors.joining(","));
        
        //扩缩容操作记录
         final OperateRecord operateRecord = new Builder().userOperation(creator)
                .project(projectService.getProjectBriefByProjectId(AuthConstant.SUPER_PROJECT_ID))
                .operationTypeEnum(OperateTypeEnum.PHYSICAL_CLUSTER_CAPACITY)
                .content(String.format("集群%s:clientNode:【%s】;datanode:【%s】",hostOption,clientNode,datanode))
                .bizId(content.getPhyClusterId()).buildDefaultManualTrigger();
        operateRecordService.save(operateRecord);
        return Result.buildSucc();
    }

    @Override
    Result<Void> buildDockerEcmTaskDTO(EcmTaskDTO ecmTaskDTO, String param, String creator) {
        ClusterIndecreaseDockerContent content = ConvertUtil.obj2ObjByJSON(param, ClusterIndecreaseDockerContent.class);
        ecmTaskDTO.setPhysicClusterId(content.getPhyClusterId());
        ecmTaskDTO.setOrderType(content.getOperationType());

        List<EcmParamBase> ecmParamBaseList = OpOrderTaskConverter.convert2EcmParamBaseList(ES_DOCKER,
            OpTaskTypeEnum.valueOfType(content.getOperationType()), content);
        ecmTaskDTO.setClusterNodeRole(ListUtils
            .strList2String(ecmParamBaseList.stream().map(EcmParamBase::getRoleName).collect(Collectors.toList())));
        ecmTaskDTO.setEcmParamBaseList(ecmParamBaseList);
        return Result.buildSucc();
    }

    private Result<List<EcmParamBase>> getHostScaleParamBaseList(Integer phyClusterId,
                                                                 List<ESClusterRoleHost> roleClusterHosts,
                                                                 Integer pidCount) {
        List<String> roleNameList = new ArrayList<>();
        for (ESClusterRoleHost clusterRoleHost : roleClusterHosts) {
            if (!roleNameList.contains(clusterRoleHost.getRole())) {
                roleNameList.add(clusterRoleHost.getRole());
            }
        }
        final Result<List<EcmParamBase>> listResult = ecmHandleService.buildEcmParamBaseList(phyClusterId,
            roleNameList);
        if (listResult.failed()) {
            return Result.buildFrom(listResult);
        }
        List<EcmParamBase> ecmParamBaseList = listResult.getData();

        return Result
            .buildSucc(buildHostScaleParamBaseList(roleClusterHosts, pidCount, roleNameList, ecmParamBaseList));
    }

    private List<EcmParamBase> buildHostScaleParamBaseList(List<ESClusterRoleHost> roleClusterHosts, Integer pidCount,
                                                           List<String> roleNameList,
                                                           List<EcmParamBase> ecmParamBaseList) {

        List<EcmParamBase> hostScaleParamBaseList = Lists.newArrayList();
        for (String roleName : roleNameList) {
            List<String> hostnameList = Lists.newArrayList();
            for (ESClusterRoleHost clusterRoleHost : roleClusterHosts) {

                if (StringUtils.equals(roleName, clusterRoleHost.getRole())) {
                    if (AriusObjUtils.isBlank(clusterRoleHost.getHostname())) {
                        continue;
                    }
                    hostnameList.add(clusterRoleHost.getHostname());
                }
            }
            for (EcmParamBase ecmParamBase : ecmParamBaseList) {
                if (StringUtils.equals(roleName, ecmParamBase.getRoleName())) {
                    HostParamBase hostParamBase = (HostParamBase) ecmParamBase;

                    HostScaleActionParam hostScaleActionParam = ConvertUtil.obj2Obj(hostParamBase,
                        HostScaleActionParam.class);
                    hostScaleActionParam.setPidCount(pidCount);
                    hostScaleActionParam.setHostList(hostnameList);
                    hostScaleActionParam.setNodeNumber(hostnameList.size());

                    hostScaleParamBaseList.add(hostScaleActionParam);
                }
            }
        }
        return hostScaleParamBaseList;
    }

    /**
     * 从主机映射得到港口作用
     * 从db中获取物理集群角色下的端口号信息
     *
     * @param phyClusterId phy集群id
     * @return Map<String, String>   Map<角色名称,端口号> 物理集群下的角色端口map
     */
    private Map<String, String> getPortOfRoleMapFromHost(Long phyClusterId) {
        Map<String, String> rolePortMap = new HashMap<>(ESClusterNodeRoleEnum.values().length);
        for (ESClusterNodeRoleEnum param : ESClusterNodeRoleEnum.values()) {
            if (param != ESClusterNodeRoleEnum.UNKNOWN) {
                List<ClusterRoleHost> clusterRoleHosts = clusterRoleHostService.getByRoleAndClusterId(phyClusterId,
                    param.getDesc());
                // 默认采用8060端口进行es集群的搭建
                rolePortMap.put(param.getDesc(),
                    CollectionUtils.isEmpty(clusterRoleHosts) ? ClusterConstant.DEFAULT_PORT
                        : clusterRoleHosts.get(0).getPort());
            }
        }

        return rolePortMap;
    }

}