package com.didichuxing.datachannel.arius.admin.resource;

/**
 * @author cjm
 */
public class LogicClusterInfoSource {
    //
    //    @Data
    //    public static class LogicClusterInfo {
    //        private String logicClusterName;
    //        private Long logicClusterId;
    //        private Integer type = ClusterResourceTypeEnum.PRIVATE.getCode();
    //    }
    //
    //    /**
    //     * 申请逻辑集群
    //     */
    //    public static LogicClusterInfo applyLogicCluster(String phyClusterName, String logicClusterName) throws IOException {
    //        LogicClusterInfo logicClusterInfo = new LogicClusterInfo();
    //        logicClusterInfo.logicClusterName = logicClusterName;
    //        // 用户点击申请逻辑集群，发起工单申请
    //        String type = "logicClusterCreate";
    //        WorkOrderDTO workOrderDTO = CustomDataSource.getWorkOrderDTO(type);
    //        Map<String, Object> contentObj = new HashMap<>();
    //        contentObj.put("name", logicClusterName);
    //        contentObj.put("dataNodeSpec", "16-64Gi-3072g");
    //        contentObj.put("dataNodeNu", 3);
    //        contentObj.put("responsible", "admin");
    //        contentObj.put("type", ClusterResourceTypeEnum.PRIVATE.getCode());
    //        workOrderDTO.setContentObj(contentObj);
    //        Result<AriusWorkOrderInfoSubmittedVO> result = NormalOrderControllerMethod.submit(type, workOrderDTO);
    //        Assertions.assertTrue(result.success());
    //
    //        // 通过工单
    //        Long orderId = result.getData().getId();
    //        WorkOrderProcessDTO processDTO = CustomDataSource.getWorkOrderProcessDTO(orderId);
    //        Result<Void> result2 = NormalOrderControllerMethod.process(orderId, processDTO);
    //        Assertions.assertTrue(result2.success());
    //
    //        // 运维人员点击分配集群
    //        PhyClusterInfoSource.distributeCluster(phyClusterName, logicClusterName);
    //
    //        // 获取刚申请的逻辑集群 id
    //        ClusterLogicConditionDTO condition = CustomDataSource.getClusterLogicConditionDTO(logicClusterName);
    //        PaginationResult<ClusterLogicVO> result3 = ESLogicClusterOpV3ControllerMethod.pageGetConsoleClusterVOS(condition);
    //        Assertions.assertTrue(result3.success());
    //        Assertions.assertFalse(result3.getData().getBizData().isEmpty());
    //        logicClusterInfo.logicClusterId = result3.getData().getBizData().get(0).getId();
    //        return logicClusterInfo;
    //    }
}