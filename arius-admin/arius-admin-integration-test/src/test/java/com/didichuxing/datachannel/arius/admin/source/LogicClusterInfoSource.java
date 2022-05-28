package com.didichuxing.datachannel.arius.admin.source;

import com.didichuxing.datachannel.arius.admin.biz.workorder.content.LogicClusterDeleteContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterLogicConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.workorder.WorkOrderDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.workorder.WorkOrderProcessDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterRegionVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ConsoleClusterVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.order.AriusWorkOrderInfoSubmittedVO;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterResourceTypeEnum;
import com.didichuxing.datachannel.arius.admin.method.v3.op.cluster.logic.ESLogicClusterOpV3ControllerMethod;
import com.didichuxing.datachannel.arius.admin.method.v3.op.cluster.logic.ESLogicClusterRegionControllerMethod;
import com.didichuxing.datachannel.arius.admin.method.v3.normal.NormalOrderControllerMethod;
import lombok.Data;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author cjm
 */
public class LogicClusterInfoSource {

    @Data
    public static class LogicClusterInfo {
        private String logicClusterName;
        private Long logicClusterId;
        private Integer type = ClusterResourceTypeEnum.PRIVATE.getCode();
    }

    /**
     * 申请逻辑集群
     */
    public static LogicClusterInfo applyLogicCluster(String phyClusterName, String logicClusterName) throws IOException {
        LogicClusterInfo logicClusterInfo = new LogicClusterInfo();
        logicClusterInfo.logicClusterName = logicClusterName;
        // 用户点击申请逻辑集群，发起工单申请
        String type = "logicClusterCreate";
        WorkOrderDTO workOrderDTO = CustomDataSource.getWorkOrderDTO(type);
        Map<String, Object> contentObj = new HashMap<>();
        contentObj.put("name", logicClusterName);
        contentObj.put("dataNodeSpec", "16-64Gi-3072g");
        contentObj.put("dataNodeNu", 3);
        contentObj.put("responsible", "admin");
        contentObj.put("type", ClusterResourceTypeEnum.PRIVATE.getCode());
        workOrderDTO.setContentObj(contentObj);
        Result<AriusWorkOrderInfoSubmittedVO> result = NormalOrderControllerMethod.submit(type, workOrderDTO);
        Assertions.assertTrue(result.success());

        // 通过工单
        Long orderId = result.getData().getId();
        WorkOrderProcessDTO processDTO = CustomDataSource.getWorkOrderProcessDTO(orderId);
        Result<Void> result2 = NormalOrderControllerMethod.process(orderId, processDTO);
        Assertions.assertTrue(result2.success());

        // 运维人员点击分配集群
        PhyClusterInfoSource.distributeCluster(phyClusterName, logicClusterName);

        // 获取刚申请的逻辑集群 id
        ClusterLogicConditionDTO condition = CustomDataSource.getClusterLogicConditionDTO(logicClusterName);
        PaginationResult<ConsoleClusterVO> result3 = ESLogicClusterOpV3ControllerMethod.pageGetConsoleClusterVOS(condition);
        Assertions.assertTrue(result3.success());
        Assertions.assertFalse(result3.getData().getBizData().isEmpty());
        logicClusterInfo.logicClusterId = result3.getData().getBizData().get(0).getId();
        return logicClusterInfo;
    }

    /**
     * 删除逻辑集群
     */
    public static void removeLogicCluster(String logicClusterName, Long logicClusterId) throws IOException {
        // 解绑 region
        // 逻辑集群节点映射解绑
        Result<List<ClusterRegionVO>> result = ESLogicClusterRegionControllerMethod.listLogicClusterRegions(logicClusterId);
        Assertions.assertTrue(result.success());
        for (ClusterRegionVO clusterRegionVO : result.getData()) {
            ESLogicClusterRegionControllerMethod.cancelBindingLogicClusterRegion(clusterRegionVO.getId(), logicClusterId);
        }

        // 删除逻辑集群工单申请
        WorkOrderDTO workOrderDTO = CustomDataSource.getWorkOrderDTO("logicClusterDelete");
        LogicClusterDeleteContent logicClusterDeleteContent = new LogicClusterDeleteContent();
        logicClusterDeleteContent.setId(logicClusterId);
        logicClusterDeleteContent.setName(logicClusterName);
        logicClusterDeleteContent.setResponsible(CustomDataSource.operator);
        logicClusterDeleteContent.setType(ClusterResourceTypeEnum.PRIVATE.getCode());
        workOrderDTO.setContentObj(logicClusterDeleteContent);
        workOrderDTO.setType("logicClusterDelete");
        workOrderDTO.setSubmitorProjectid(1);
        Result<AriusWorkOrderInfoSubmittedVO> result2 = NormalOrderControllerMethod.submit("logicClusterDelete", workOrderDTO);
        Assertions.assertTrue(result2.success());

        // 通过工单，删除逻辑集群
        WorkOrderProcessDTO processDTO = CustomDataSource.getWorkOrderProcessDTO(result2.getData().getId());
        Result<Void> result3 = NormalOrderControllerMethod.process(result2.getData().getId(), processDTO);
        Assertions.assertTrue(result3.success());
    }
}