package com.didichuxing.datachannel.arius.admin.biz.task.handler.cluster;

import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant.CREATE_MASTER_NODE_MIN_NUMBER;
import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum.MASTER_NODE;
import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterTypeEnum.ES_HOST;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.workorder.utils.OpOrderTaskConverter;
import com.didichuxing.datachannel.arius.admin.biz.task.content.ClusterNewDockerContent;
import com.didichuxing.datachannel.arius.admin.biz.task.content.ClusterNewHostContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.ESClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.EcmParamBase;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.ecm.EcmTaskDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant;

import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.google.common.collect.Maps;

/**
 * @author ohushenglin_v
 * @date 2022-05-20
 */
@Service("clusterCreateTaskHandler")
public class ClusterCreateTaskHandler extends AbstractClusterTaskHandler {

    private static final String PARAM_ILLEGAL_TIPS = "集群缺少类型为%s的节点";

    @Override
    Result<Void> initHostParam(OpTask opTask) {
        ClusterNewHostContent clusterOpNewHostContent = ConvertUtil.str2ObjByJson(opTask.getExpandData(),
            ClusterNewHostContent.class);
        // 校验pid_count（单节点实例数字段）,如果为null，则设置默认值1
        if (null == clusterOpNewHostContent.getPidCount()) {
            clusterOpNewHostContent.setPidCount(ClusterConstant.DEFAULT_CLUSTER_PAID_COUNT);
        }
        if (StringUtils.isBlank(clusterOpNewHostContent.getCreator())) {
            clusterOpNewHostContent.setCreator(opTask.getCreator());
        }
        // 对于address字段进行ip和端口号的拆分
        List<ESClusterRoleHost> roleClusterHosts = clusterOpNewHostContent.getClusterRoleHosts();
        for (ESClusterRoleHost esClusterRoleHost : roleClusterHosts) {
            if (null == esClusterRoleHost.getAddress()) {
                return Result.buildFail("传入节点的address不应该为空");
            }
            // 将ip和port中hostname中拆分出来
            String[] ipAndPort = esClusterRoleHost.getAddress().split(":");
            if (ipAndPort.length < 2) {
                return Result.buildFail("传入节点的address应该满足【ip:port】格式");
            }
            esClusterRoleHost.setHostname(ipAndPort[0]);
            esClusterRoleHost.setIp(ipAndPort[0]);
            esClusterRoleHost.setPort(ipAndPort[1]);
        }
        opTask.setExpandData(JSON.toJSONString(clusterOpNewHostContent));
        return Result.buildSucc();
    }

    @Override
    Result<Void> validateHostParam(String param) {
        ClusterNewHostContent clusterOpNewHostContent = ConvertUtil.str2ObjByJson(param, ClusterNewHostContent.class);
        if (AriusObjUtils.isNull(clusterOpNewHostContent.getPhyClusterName())) {
            return Result.buildParamIllegal("物理集群名称为空");
        }

        if (clusterPhyService.isClusterExists(clusterOpNewHostContent.getPhyClusterName())) {
            return Result.buildParamIllegal("物理集群名称不能重复");
        }
        List<ESClusterRoleHost> roleClusterHosts = clusterOpNewHostContent.getClusterRoleHosts();
        if (CollectionUtils.isEmpty(roleClusterHosts)) {
            return Result.buildParamIllegal("集群角色为空");
        }
        Map<String, String> roleClusterPortMap = Maps.newHashMap();
        for (ESClusterRoleHost esClusterRoleHost : roleClusterHosts) {
            //ES同一个角色的端口号应该相同，拆解ip和port后进行校验
            String port = roleClusterPortMap.getOrDefault(esClusterRoleHost.getRole(), "");

            if (StringUtils.isNotBlank(port) && !port.equals(esClusterRoleHost.getPort())) {
                return Result.buildFail("同一个集群中同一角色的端口号应该相同");
            }
            roleClusterPortMap.put(esClusterRoleHost.getRole(), esClusterRoleHost.getPort());
        }

        Set<String> hostRoles = roleClusterHosts.stream().map(ESClusterRoleHost::getRole).collect(Collectors.toSet());
        if (!hostRoles.contains(MASTER_NODE.getDesc())) {
            return Result.buildParamIllegal(String.format(PARAM_ILLEGAL_TIPS, MASTER_NODE.getDesc()));
        }

        List<ESClusterRoleHost> masterNodes = roleClusterHosts.stream()
            .filter(r -> MASTER_NODE.getDesc().equals(r.getRole())).collect(Collectors.toList());

        if (masterNodes.size() < CREATE_MASTER_NODE_MIN_NUMBER) {
            return Result.buildParamIllegal("master-node角色ip个数要求大于等于1");
        }
        // es版本检查
        if (null == esPackageService.getByVersionAndType(clusterOpNewHostContent.getEsVersion(), ES_HOST.getCode())) {
            return Result.buildFail(ES_HOST.getDesc() + "类型版本为" + clusterOpNewHostContent.getEsVersion() + "的程序包不存在");
        }
        return Result.buildSucc();
    }

    @Override
    Result<Void> buildHostEcmTaskDTO(EcmTaskDTO ecmTaskDTO, String param, String creator) {
        // 获取并且设置新建集群工单内容中的集群创建人信息
        ClusterNewHostContent clusterOpNewHostContent = ConvertUtil.str2ObjByJson(param, ClusterNewHostContent.class);
        if (StringUtils.isBlank(clusterOpNewHostContent.getCreator())) {
            clusterOpNewHostContent.setCreator(creator);
        }
        ecmTaskDTO.setOrderType(OpTaskTypeEnum.CLUSTER_NEW.getType());

        List<EcmParamBase> ecmParamBaseList = OpOrderTaskConverter.convert2EcmParamBaseList(ESClusterTypeEnum.ES_HOST,
            OpTaskTypeEnum.CLUSTER_NEW, clusterOpNewHostContent);

        ecmTaskDTO.setEcmParamBaseList(ecmParamBaseList);

        return Result.buildSucc();
    }

    @Override
    Result<Void> buildDockerEcmTaskDTO(EcmTaskDTO ecmTaskDTO, String param, String creator) {
        List<EcmParamBase> ecmParamBaseList = OpOrderTaskConverter.convert2EcmParamBaseList(ESClusterTypeEnum.ES_DOCKER,
            OpTaskTypeEnum.CLUSTER_NEW, ConvertUtil.obj2ObjByJSON(param, ClusterNewDockerContent.class));

        ecmTaskDTO.setEcmParamBaseList(ecmParamBaseList);
        return Result.buildSucc();
    }
}
