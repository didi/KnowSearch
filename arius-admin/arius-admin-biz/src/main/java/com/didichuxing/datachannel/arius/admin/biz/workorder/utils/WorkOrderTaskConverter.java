package com.didichuxing.datachannel.arius.admin.biz.workorder.utils;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.workorder.content.ClusterOpBaseContent;
import com.didichuxing.datachannel.arius.admin.biz.workorder.content.ClusterOpIndecreaseDockerContent;
import com.didichuxing.datachannel.arius.admin.biz.workorder.content.ClusterOpNewDockerContent;
import com.didichuxing.datachannel.arius.admin.biz.workorder.content.ClusterOpNewHostContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.ESClusterRoleDocker;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.ESClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.EcmParamBase;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.elasticcloud.ElasticCloudCommonActionParam;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.elasticcloud.ElasticCloudCreateActionParam;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.elasticcloud.ElasticCloudScaleActionParam;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.host.HostsCreateActionParam;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.host.HostsParamBase;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.host.HostsScaleActionParam;
import com.didichuxing.datachannel.arius.admin.common.constant.ecm.EcmTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.ecm.EcmTask;
import com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;

import java.util.*;

import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum.*;
import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterTypeEnum.ES_DOCKER;
import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterTypeEnum.ES_HOST;

/**
 * @author zengqiao
 * @date 20/10/24
 */
public class WorkOrderTaskConverter {

    private WorkOrderTaskConverter(){}

    /**
     * 工单过来的数据转list
     */
    public static List<EcmParamBase> convert2EcmParamBaseList(ESClusterTypeEnum clusterTypeEnum,
                                                              EcmTaskTypeEnum ecmTaskTypeEnum,
                                                              ClusterOpBaseContent clusterOpBaseContent) {
        List<EcmParamBase> ecmParamBaseList = null;
        if (ES_DOCKER.equals(clusterTypeEnum)) {
            ecmParamBaseList = convert2ElasticCloudParamBaseList(clusterTypeEnum, ecmTaskTypeEnum,
                clusterOpBaseContent);
        } else if (ES_HOST.equals(clusterTypeEnum)) {
            ecmParamBaseList = convert2HostParamBaseList(ecmTaskTypeEnum, clusterOpBaseContent);
        }
        return ecmParamBaseList;
    }

    private static List<EcmParamBase> convert2ElasticCloudParamBaseList(ESClusterTypeEnum clusterTypeEnum,
                                                                        EcmTaskTypeEnum ecmTaskTypeEnum,
                                                                        ClusterOpBaseContent clusterOpBaseContent) {
        List<EcmParamBase> ecmParamBaseList = new ArrayList<>();
        if (EcmTaskTypeEnum.EXPAND.equals(ecmTaskTypeEnum) || EcmTaskTypeEnum.SHRINK.equals(ecmTaskTypeEnum)) {
            ClusterOpIndecreaseDockerContent clusterOpIndecreaseDockerContent = (ClusterOpIndecreaseDockerContent) clusterOpBaseContent;
            for (ESClusterRoleDocker esClusterRoleDocker : clusterOpIndecreaseDockerContent.getRoleClusters()) {
                ElasticCloudScaleActionParam elasticCloudScaleActionParam = new ElasticCloudScaleActionParam();
                elasticCloudScaleActionParam.setType(clusterTypeEnum.getCode());
                elasticCloudScaleActionParam.setPodNum(esClusterRoleDocker.getPodNumber());
                elasticCloudScaleActionParam.setNsTree("");
                elasticCloudScaleActionParam.setMachineRoom("");
                elasticCloudScaleActionParam.setNodeNumber(esClusterRoleDocker.getPodNumber());
                elasticCloudScaleActionParam.setRoleName(esClusterRoleDocker.getRole());
                elasticCloudScaleActionParam.setPhyClusterId(clusterOpIndecreaseDockerContent.getPhyClusterId());
                elasticCloudScaleActionParam.setPhyClusterName(clusterOpBaseContent.getPhyClusterName());
                ecmParamBaseList.add(elasticCloudScaleActionParam);
            }
        } else if (EcmTaskTypeEnum.NEW.equals(ecmTaskTypeEnum)) {
            ClusterOpNewDockerContent clusterOpNewDockerContent = (ClusterOpNewDockerContent) clusterOpBaseContent;
            for (String roleName : Arrays.asList(MASTER_NODE.getDesc(), CLIENT_NODE.getDesc(), DATA_NODE.getDesc())) {
                ElasticCloudCreateActionParam elasticCloudCreateActionParam = ConvertUtil
                    .obj2Obj(clusterOpNewDockerContent, ElasticCloudCreateActionParam.class);
                elasticCloudCreateActionParam.setPhyClusterId(ClusterConstant.INVALID_VALUE);
                elasticCloudCreateActionParam.setRoleName(roleName);
                elasticCloudCreateActionParam.setImageName("");
                ecmParamBaseList.add(elasticCloudCreateActionParam);
            }

            for (ESClusterRoleDocker esClusterRoleDocker : clusterOpNewDockerContent.getRoleClusters()) {
                ElasticCloudCreateActionParam elasticCloudCreateActionParam = null;
                if (MASTER_NODE.getDesc().equals(esClusterRoleDocker.getRole())) {
                    elasticCloudCreateActionParam = (ElasticCloudCreateActionParam) ecmParamBaseList.get(0);
                } else if (CLIENT_NODE.getDesc().equals(esClusterRoleDocker.getRole())) {
                    elasticCloudCreateActionParam = (ElasticCloudCreateActionParam) ecmParamBaseList.get(1);
                } else if (DATA_NODE.getDesc().equals(esClusterRoleDocker.getRole())) {
                    elasticCloudCreateActionParam = (ElasticCloudCreateActionParam) ecmParamBaseList.get(2);
                } else {
                    continue;
                }
                elasticCloudCreateActionParam.setMachineSpec(esClusterRoleDocker.getMachineSpec());
                elasticCloudCreateActionParam.setNodeNumber(esClusterRoleDocker.getPodNumber());
            }
        }
        return ecmParamBaseList;
    }

    private static List<EcmParamBase> convert2HostParamBaseList(EcmTaskTypeEnum ecmTaskTypeEnum,
                                                                ClusterOpBaseContent clusterOpBaseContent) {
        List<EcmParamBase> ecmParamBaseList = new ArrayList<>();
        if (EcmTaskTypeEnum.NEW.equals(ecmTaskTypeEnum)) {
            ClusterOpNewHostContent clusterOpNewHostContent = (ClusterOpNewHostContent) clusterOpBaseContent;
            for (String roleName : Arrays.asList(MASTER_NODE.getDesc(), CLIENT_NODE.getDesc(), DATA_NODE.getDesc())) {
                HostsCreateActionParam hostCreateActionParam = ConvertUtil.obj2Obj(clusterOpNewHostContent,
                    HostsCreateActionParam.class);
                hostCreateActionParam.setPhyClusterId(ClusterConstant.INVALID_VALUE);
                hostCreateActionParam.setRoleName(roleName);
                hostCreateActionParam.setImageName("");
                hostCreateActionParam.setHostList(new ArrayList<>());
                hostCreateActionParam.setMasterHostList(new ArrayList<>());
                ecmParamBaseList.add(hostCreateActionParam);
            }

            for (ESClusterRoleHost esClusterRoleHost : clusterOpNewHostContent.getClusterRoleHosts()) {
                // 这三个节点的顺序一定是这个顺序
                if (AriusObjUtils.isBlank(esClusterRoleHost.getHostname())) {
                    continue;
                }
                if (MASTER_NODE.getDesc().equals(esClusterRoleHost.getRole())) {
                    HostsCreateActionParam masterCreateAction = (HostsCreateActionParam) ecmParamBaseList.get(0);
                    masterCreateAction.getHostList().add(esClusterRoleHost.getHostname());
                    masterCreateAction.setPort(esClusterRoleHost.getPort());

                } else if (CLIENT_NODE.getDesc().equals(esClusterRoleHost.getRole())) {
                    HostsCreateActionParam clientCreateAction = ((HostsCreateActionParam) ecmParamBaseList.get(1));
                    clientCreateAction.getHostList().add(esClusterRoleHost.getHostname());
                    clientCreateAction.setPort(esClusterRoleHost.getPort());

                } else if (DATA_NODE.getDesc().equals(esClusterRoleHost.getRole())) {
                    HostsCreateActionParam dataCreateAction = ((HostsCreateActionParam) ecmParamBaseList.get(2));
                    dataCreateAction.getHostList().add(esClusterRoleHost.getHostname());
                    dataCreateAction.setPort(esClusterRoleHost.getPort());
                }
            }

            List<String> masterHostList = ((HostsCreateActionParam) ecmParamBaseList.get(0)).getHostList();
            for (EcmParamBase ecmParamBase : ecmParamBaseList) {
                HostsCreateActionParam hostCreateActionParam = (HostsCreateActionParam) ecmParamBase;
                hostCreateActionParam.setMasterHostList(masterHostList);
                hostCreateActionParam.setNodeNumber(hostCreateActionParam.getHostList().size());
            }
            return ecmParamBaseList;
        }
        return new ArrayList<>();
    }

    /**
     * DB中的handle data数据转map
     */
    public static Map<String, EcmParamBase> convert2EcmParamBaseMap(EcmTask ecmTask) {
        List<EcmParamBase> ecmParamBaseList = null;
        if (ES_DOCKER.getCode() == ecmTask.getType()) {
            ecmParamBaseList = convert2ElasticCloudParamBaseList(ecmTask);
        } else if (ES_HOST.getCode() == ecmTask.getType()) {
            ecmParamBaseList = convert2HostParamBaseList(ecmTask);
        }
        if (AriusObjUtils.isNull(ecmParamBaseList)) {
            return new HashMap<>();
        }

        Map<String, EcmParamBase> roleNameEcmParamBaseMap = new HashMap<>();
        for (EcmParamBase ecmParamBase : ecmParamBaseList) {
            roleNameEcmParamBaseMap.put(ecmParamBase.getRoleName(), ecmParamBase);
        }
        return roleNameEcmParamBaseMap;
    }

    public static List<EcmParamBase> convert2EcmParamBaseList(EcmTask ecmTask) {
        if (ESClusterTypeEnum.ES_DOCKER.getCode() == ecmTask.getType()) {
            return convert2ElasticCloudParamBaseList(ecmTask);
        } else if (ESClusterTypeEnum.ES_HOST.getCode() == ecmTask.getType()) {
            return convert2HostParamBaseList(ecmTask);
        }
        return new ArrayList<>();
    }

    private static List<EcmParamBase> convert2ElasticCloudParamBaseList(EcmTask ecmTask) {
        List<EcmParamBase> ecmParamBaseList;
        if (EcmTaskTypeEnum.EXPAND.getCode() == ecmTask.getOrderType()
            || EcmTaskTypeEnum.SHRINK.getCode() == ecmTask.getOrderType()) {
            ecmParamBaseList = new ArrayList<>(
                JSON.parseArray(ecmTask.getHandleData(), ElasticCloudScaleActionParam.class));
        } else if (EcmTaskTypeEnum.NEW.getCode() == ecmTask.getOrderType()) {
            ecmParamBaseList = new ArrayList<>(
                ConvertUtil.str2ObjArrayByJson(ecmTask.getHandleData(), ElasticCloudCreateActionParam.class));
        } else {
            ecmParamBaseList = new ArrayList<>(
                JSON.parseArray(ecmTask.getHandleData(), ElasticCloudCommonActionParam.class));
        }
        return ecmParamBaseList;
    }

    private static List<EcmParamBase> convert2HostParamBaseList(EcmTask ecmTask) {
        List<EcmParamBase> ecmParamBaseList = null;
        if (EcmTaskTypeEnum.NEW.getCode() == ecmTask.getOrderType()) {
            ecmParamBaseList = new ArrayList<>(
                ConvertUtil.str2ObjArrayByJson(ecmTask.getHandleData(), HostsCreateActionParam.class));
        } else if (EcmTaskTypeEnum.EXPAND.getCode() == ecmTask.getOrderType()
                   || EcmTaskTypeEnum.SHRINK.getCode() == ecmTask.getOrderType()) {
            ecmParamBaseList = new ArrayList<>(
                JSON.parseArray(ecmTask.getHandleData(), HostsScaleActionParam.class));
        } else {
            ecmParamBaseList = new ArrayList<>(JSON.parseArray(ecmTask.getHandleData(), HostsParamBase.class));
        }
        return ecmParamBaseList;
    }
}