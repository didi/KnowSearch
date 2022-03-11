package com.didichuxing.datachannel.arius.admin.source;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.*;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.dsl.template.DslTemplateConditionDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.metrics.GatewayJoinQueryDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.TemplateConditionDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.workorder.WorkOrderDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.workorder.WorkOrderProcessDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterNodeRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;

import java.util.*;

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
        param.setAppId(appid);
        param.setCluster(clusterName);
        param.setDivide(true);
        param.setEsVersion("7.6.2");
        param.setPhyClusterDesc("");
        param.setRegionRacks(new ArrayList<>());
        param.setPassword("");
        param.setResponsible(operator);
        param.setTemplateSrvs(TemplateServiceEnum.TEMPLATE_PRE_CREATE.getCode() + "");
        List<ESRoleClusterHostDTO> list = new ArrayList<>();
        for(int i = 1; i <= 3; i++) {
            ESRoleClusterHostDTO esRoleClusterHostDTO = getESRoleClusterHostDTO();
            esRoleClusterHostDTO.setCluster(clusterName);
            esRoleClusterHostDTO.setRole(i);
            list.add(esRoleClusterHostDTO);
        }
        param.setRoleClusterHosts(list);
        return param;
    }

    public static ESRoleClusterHostDTO getESRoleClusterHostDTO() {
        ESRoleClusterHostDTO hostDTO = new ESRoleClusterHostDTO();
        hostDTO.setCluster(getRandomClusterName());
        hostDTO.setIp(testPhyClusterIp);
        hostDTO.setNodeSet("");
        hostDTO.setPort(String.valueOf(testPhyClusterPort));
        hostDTO.setRack("");
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
        workOrderDTO.setSubmitorAppid(appid);
        workOrderDTO.setDescription("testtest");
        workOrderDTO.setType(type);
        return workOrderDTO;
    }

    public static ClusterPhyConditionDTO getClusterPhyConditionDTO(String clusterName) {
        ClusterPhyConditionDTO clusterPhyConditionDTO = new ClusterPhyConditionDTO();
        clusterPhyConditionDTO.setCluster(clusterName);
        clusterPhyConditionDTO.setFrom(0L);
        clusterPhyConditionDTO.setSize(10L);
        return clusterPhyConditionDTO;
    }

    public static ClusterLogicConditionDTO getClusterLogicConditionDTO(String clusterName) {
        ClusterLogicConditionDTO clusterLogicConditionDTO = new ClusterLogicConditionDTO();
        clusterLogicConditionDTO.setName(clusterName);
        clusterLogicConditionDTO.setFrom(0L);
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
        dto.setFrom(0L);
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
        dto.setFrom(0L);
        dto.setSize(10L);
        return dto;
    }
}
