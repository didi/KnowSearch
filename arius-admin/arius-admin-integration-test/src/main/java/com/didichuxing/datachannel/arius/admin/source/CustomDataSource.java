package com.didichuxing.datachannel.arius.admin.source;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterJoinDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterLogicConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterPhyConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESClusterRoleHostDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.dsl.template.DslTemplateConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.GatewayJoinQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.GatewayMetricsDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsClusterPhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsDashboardListDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsDashboardTopNDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.OpTaskDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.TemplateConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.user.AriusUserInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.workorder.WorkOrderDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.workorder.WorkOrderProcessDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterImportRuleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CustomDataSource {

    public static final int SIZE = 10;

    public static String testAdminIp;
    public static String testAdminPort;
    public static String testPhyClusterIp;
    public static Integer testPhyClusterPort;
    public static String operator;
    public static Integer appid;

    private static String generateString(Random random, int length) {
        String sources = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        char[] text = new char[length];
        for (int i = 0; i < length; i++) {
            text[i] = sources.charAt(random.nextInt(sources.length()));
        }
        return new String(text);
    }

    private static String getRandomClusterName() {
        return generateString(new Random(), 5) + "_" + generateString(new Random(), 5);
    }

    public static String getRandomString(int length) {
        return generateString(new Random(), length);
    }

    public static ClusterJoinDTO getClusterJoinDTO() {
        String clusterName = getRandomClusterName();
        ClusterJoinDTO param = new ClusterJoinDTO();
        param.setType(4);
        param.setProjectId(appid);
        param.setCluster(clusterName);
        param.setDivide(true);
        param.setEsVersion("7.6.2");
        param.setPhyClusterDesc("");
        param.setRegionRacks(new ArrayList<>());
        param.setPassword("");
        param.setResponsible(operator);
        param.setTemplateSrvs(TemplateServiceEnum.TEMPLATE_PRE_CREATE.getCode() + "");
        param.setImportRule(ESClusterImportRuleEnum.AUTO_IMPORT.getCode());
        param.setTags("{\"resourceType\":3, \"createSource\":0}");
        List<ESClusterRoleHostDTO> list = new ArrayList<>();
        for(int i = 1; i <= 3; i++) {
            ESClusterRoleHostDTO esClusterRoleHostDTO = getESRoleClusterHostDTO();
            esClusterRoleHostDTO.setCluster(clusterName);
            esClusterRoleHostDTO.setRole(i);
            list.add(esClusterRoleHostDTO);
        }
        param.setRoleClusterHosts(list);
        return param;
    }

    public static ESClusterRoleHostDTO getESRoleClusterHostDTO() {
        ESClusterRoleHostDTO hostDTO = new ESClusterRoleHostDTO();
        hostDTO.setCluster(getRandomClusterName());
        hostDTO.setIp(testPhyClusterIp);
        hostDTO.setNodeSet("");
        hostDTO.setPort(String.valueOf(testPhyClusterPort));
        hostDTO.setRole(ESClusterNodeRoleEnum.MASTER_NODE.getCode());
        hostDTO.setRoleClusterId(0L);
        hostDTO.setStatus(0);
        hostDTO.setBeCold(false);
        return hostDTO;
    }

    public static WorkOrderDTO getWorkOrderDTO(String type) {
        WorkOrderDTO workOrderDTO = new WorkOrderDTO();
        Map<String, Object> contentObj = new HashMap<>();
        workOrderDTO.setContentObj(contentObj);
        workOrderDTO.setSubmitor(operator);
        workOrderDTO.setSubmitorProjectid(appid);
        workOrderDTO.setDescription("testtest");
        workOrderDTO.setType(type);
        return workOrderDTO;
    }

    public static ClusterPhyConditionDTO getClusterPhyConditionDTO(String clusterName) {
        ClusterPhyConditionDTO clusterPhyConditionDTO = new ClusterPhyConditionDTO();
        clusterPhyConditionDTO.setCluster(clusterName);
        clusterPhyConditionDTO.setPage(1L);
        clusterPhyConditionDTO.setSize(10L);
        return clusterPhyConditionDTO;
    }

    public static ClusterLogicConditionDTO getClusterLogicConditionDTO(String clusterName) {
        ClusterLogicConditionDTO clusterLogicConditionDTO = new ClusterLogicConditionDTO();
        clusterLogicConditionDTO.setName(clusterName);
        clusterLogicConditionDTO.setPage(1L);
        clusterLogicConditionDTO.setSize(10L);
        return clusterLogicConditionDTO;
    }

    public static WorkOrderProcessDTO getWorkOrderProcessDTO(Long orderId) {
        WorkOrderProcessDTO workOrderProcessDTO = new WorkOrderProcessDTO();
        workOrderProcessDTO.setAssignee(operator);
        workOrderProcessDTO.setAssigneeAppid(appid);
        workOrderProcessDTO.setComment("testtest");
        workOrderProcessDTO.setOrderId(orderId);
        workOrderProcessDTO.setCheckAuthority(false);
        workOrderProcessDTO.setOutcome("agree");
        workOrderProcessDTO.setContentObj(new HashMap<>());
        return workOrderProcessDTO;
    }

    public static DslTemplateConditionDTO getDslTemplateConditionDTO() {
        DslTemplateConditionDTO dto = new DslTemplateConditionDTO();
        long endTime = System.currentTimeMillis();
        dto.setEndTime(endTime);
        dto.setStartTime(endTime - 1000 * 60 * 60 * 24);
        dto.setPage(1L);
        dto.setSize(10L);
        return dto;
    }

    public static GatewayJoinQueryDTO getGatewayJoinQueryDTO() {
        GatewayJoinQueryDTO dto = new GatewayJoinQueryDTO();
        long endTime = System.currentTimeMillis();
        dto.setEndTime(endTime);
        dto.setStartTime(endTime - 1000 * 60 * 60 * 24);
        return dto;
    }

    public static TemplateConditionDTO getTemplateConditionDTO() {
        TemplateConditionDTO dto = new TemplateConditionDTO();
        dto.setPage(1L);
        dto.setSize(10L);
        return dto;
    }

    public static AriusUserInfoDTO getariusUserInfoDTOFactory() {
        AriusUserInfoDTO ariusUserInfoDTO = new AriusUserInfoDTO();
        ariusUserInfoDTO.setEmail("");
        ariusUserInfoDTO.setMobile("");
        ariusUserInfoDTO.setStatus(1);
        ariusUserInfoDTO.setDomainAccount("wpk");
        ariusUserInfoDTO.setName("wpk");
        ariusUserInfoDTO.setPassword("hTw1yTAuEifG/HN82zFkHzTK1N2rQ9WCw8QuRgfITAy9aNJ7IccoFQwM11sblbhPmkKHGV+rsbO+rzenRmjwiB7bmyu8kYgNWPZuI5wXYKFeeBPbXXd2NQDM01i9oUDU8KAiN60rY83XSiEm4X2iBVKOgYlq3SEchNkodfsBWts=");
        ariusUserInfoDTO.setRole(2);
        return ariusUserInfoDTO;
    }

    public static OpTaskDTO getworkTaskDTO(){
        OpTaskDTO opTaskDTO =new OpTaskDTO();
        opTaskDTO.setTaskType(1);
        opTaskDTO.setBusinessKey("1");
        opTaskDTO.setDataCenter("1");
        opTaskDTO.setCreator(operator);
        opTaskDTO.setExpandData("1");
        opTaskDTO.setStatus("success");
        opTaskDTO.setCreateTime(new Date(System.currentTimeMillis()));
        opTaskDTO.setDeleteFlag(Boolean.FALSE);
        opTaskDTO.setId(2);
        opTaskDTO.setTitle("1");
        opTaskDTO.setUpdateTime(new Date(System.currentTimeMillis()));
        return opTaskDTO;
    }

    public static void setMetricsClusterPhyDTO(MetricsClusterPhyDTO param) {
        param.setClusterPhyName("logi-elasticsearch-7.6.0");
        param.setAggType("max");
        Long nowTime = System.currentTimeMillis();
        param.setStartTime(nowTime - 10 * 60 * 1000);
        param.setEndTime(nowTime);
    }

    public static void setGatewayMetricsDTO(GatewayMetricsDTO param) {
        Long nowTime = System.currentTimeMillis();
        param.setStartTime(nowTime - 10 * 60 * 1000);
        param.setEndTime(nowTime);
    }

    public static MetricsDashboardTopNDTO getMetricsDashboardTopNDTO() {
        MetricsDashboardTopNDTO param = new MetricsDashboardTopNDTO();
        Long nowTime = System.currentTimeMillis();
        param.setStartTime(nowTime - 10 * 60 * 1000);
        param.setEndTime(nowTime);
        param.setAggType("max");
        return param;
    }

    public static MetricsDashboardListDTO getMetricsDashboardListDTO() {
        MetricsDashboardListDTO param = new MetricsDashboardListDTO();
        param.setAggType("max");
        param.setOrderByDesc(true);
        return param;
    }

    public static <T> List<T> getRandomItemsFromList(List<T> sourceList) {
        Random random = new Random();
        int randomIndex = random.nextInt(sourceList.size()) + 1;
        Collections.shuffle(sourceList);
        return sourceList.subList(0, randomIndex);
    }

     public static List<String> getRandomTopClusterMetrics() {
        List<String> clusterMetrics = Lists.newArrayList("indexingLatency", "indexReqNum", "searchLatency", "gatewaySucPer", "gatewayFailedPer", "pendingTaskNum", "docUprushNum", "reqUprushNum", "shardNum");
        return getRandomItemsFromList(clusterMetrics);
     }

     public static List<String> getRandomTopNodeMetrics() {
        List<String> nodeMetrics = Lists.newArrayList("taskConsuming");
        return getRandomItemsFromList(nodeMetrics);
     }

     public static List<String> getRandomTopIndexMetrics() {
        List<String> indexMetrics = Lists.newArrayList("reqUprushNum", "docUprushNum");
        return getRandomItemsFromList(indexMetrics);
     }

     public static List<String> getRandomTopClusterThreadPoolQueueMetrics() {
        List<String> clusterThreadPoolQueueMetrics = Lists.newArrayList("refresh", "flush", "merge", "search", "write", "management");
        return getRandomItemsFromList(clusterThreadPoolQueueMetrics);
     }

     public static List<String> getRandomListTemplateMetrics() {
        List<String> templateMetrics = Lists.newArrayList("segmentMemSize", "segmentNum");
        return getRandomItemsFromList(templateMetrics);
     }

     public static List<String> getRandomListNodeMetrics() {
        List<String> nodeMetrics = Lists.newArrayList("dead", "largeDiskUsage", "largeHead", "largeCpuUsage", "writeRejectedNum", "searchRejectedNum", "shardNum");
        return getRandomItemsFromList(nodeMetrics);
     }

     public static List<String> getRandomListIndexMetrics() {
        List<String> indexMetrics = Lists.newArrayList("red", "singReplicate", "unassignedShard", "bigShard", "smallShard", "mappingNum", "segmentNum", "segmentMemSize");
        return getRandomItemsFromList(indexMetrics);
     }

     public static List<String> getRandomOverviewMetrics() {
        List<String> overviewMetrics = Lists.newArrayList("writeDocCount","writeTotalCost","writeResponseLen","queryTotalHitsAvgCount","readDocCount","querySearchType","queryCostAvg","queryTotalShardsAvg","queryFailedShardsAvg","dslLen");
        return getRandomItemsFromList(overviewMetrics);
     }

     public static List<String> getRandomGatewayNodeMetrics() {
        List<String> gatewayNodeMetrics = Lists.newArrayList("queryGatewayNode", "writeGatewayNode", "dslLen");
        return getRandomItemsFromList(gatewayNodeMetrics);
     }

     public static List<String> getRandomClientNodeMetrics() {
        List<String> clientNodeMetrics = Lists.newArrayList("queryClientNode", "writeClientNode", "dslLen");
        return getRandomItemsFromList(clientNodeMetrics);
     }

     public static List<String> getRandomGatewayIndexMetrics() {
        List<String> gatewayIndexMetrics = Lists.newArrayList("searchIndexCount", "searchIndexTotalCost", "writeIndexCount", "writeIndexTotalCost");
        return getRandomItemsFromList(gatewayIndexMetrics);
     }

     public static List<String> getRandomGatewayAppMetrics() {
        List<String> gatewayAppMetrics = Lists.newArrayList("queryAppSearchCost", "queryAppTotalCost", "queryAppCount");
        return getRandomItemsFromList(gatewayAppMetrics);
     }

     public static List<String> getRandomGatewayDslMetrics() {
        List<String> gatewayDslMetrics = Lists.newArrayList("queryDslTotalCost", "queryDslCount");
        return getRandomItemsFromList(gatewayDslMetrics);
     }

}