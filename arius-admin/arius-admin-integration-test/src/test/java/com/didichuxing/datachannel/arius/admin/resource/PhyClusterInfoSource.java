package com.didichuxing.datachannel.arius.admin.resource;

/**
 * @author cjm
 */
public class PhyClusterInfoSource {
    //
    //    @Data
    //    public static class PhyClusterInfo {
    //        private String phyClusterName;
    //        private Long phyClusterId;
    //
    //        private ConsoleClusterPhyVO consoleClusterPhyVO;
    //        // 接入物理集群时的请求体
    //        private ClusterJoinDTO clusterJoinDTO;
    //    }
    //
    //    /**
    //     * 接入物理集群
    //     */
    //    public static PhyClusterInfo phyClusterJoin() throws IOException {
    //        PhyClusterInfo phyClusterInfo = new PhyClusterInfo();
    //        // 增加物理集群，不带有逻辑集群
    //        phyClusterInfo.clusterJoinDTO = CustomDataSource.getClusterJoinDTO();
    //        // Tuple<Long, String> 逻辑集群Id, 物理集群名称
    //        Result<Tuple<Long, String>> result = ESPhyClusterControllerMethod.clusterJoin(phyClusterInfo.clusterJoinDTO);
    //        Assertions.assertTrue(result.success());
    //
    //        phyClusterInfo.phyClusterName = phyClusterInfo.clusterJoinDTO.getCluster();
    //
    //        // 获取刚创建的物理集群id，先分页获取物理集群，根据名字查询
    //        ClusterPhyConditionDTO dto = CustomDataSource.getClusterPhyConditionDTO(phyClusterInfo.clusterJoinDTO.getCluster());
    //        PaginationResult<ConsoleClusterPhyVO> result2 = ESPhyClusterControllerMethod.pageGetConsoleClusterPhyVOS(dto);
    //        List<ConsoleClusterPhyVO> data = result2.getData().getBizData();
    //        for(ConsoleClusterPhyVO clusterPhyVO : data) {
    //            if(clusterPhyVO.getCluster().equals(phyClusterInfo.clusterJoinDTO.getCluster())) {
    //                phyClusterInfo.phyClusterId = Long.valueOf(clusterPhyVO.getId());
    //                phyClusterInfo.consoleClusterPhyVO = clusterPhyVO;
    //                break;
    //            }
    //        }
    //        return phyClusterInfo;
    //    }
    //
    //    /**
    //     * 删除物理集群
    //     */
    //    public static void phyClusterRemove(String phyClusterName, Long phyClusterId) throws IOException {
    //        // 删除物理集群
    //        // 提交工单
    //        WorkOrderDTO workOrderDTO = CustomDataSource.getWorkOrderDTO("clusterDelete");
    //        Map<String, Object> contentObj = new HashMap<>();
    //        contentObj.put("phyClusterName", phyClusterName);
    //        contentObj.put("id", phyClusterId);
    //        workOrderDTO.setContentObj(contentObj);
    //        Result<AriusWorkOrderInfoSubmittedVO> result = NormalOrderControllerMethod.submit("clusterDelete", workOrderDTO);
    //
    //        Assertions.assertTrue(result.success());
    //
    //        // 通过工单，删除物理集群
    //        Long orderId = result.getData().getId();
    //        WorkOrderProcessDTO workOrderProcessDTO = CustomDataSource.getWorkOrderProcessDTO(orderId);
    //        Result<Void> result2 = NormalOrderControllerMethod.process(orderId, workOrderProcessDTO);
    //        Assertions.assertTrue(result2.success());
    //    }
    //
    //    /**
    //     * 分配集群，给用户申请的逻辑集群，分配当前物理集群
    //     */
    //    public static void distributeCluster(String phyClusterName, String logicClusterName) throws IOException {
    //        // 物理集群新增 region
    //
    //        // 获取物理集群的空闲 region
    //        Result<List<ClusterRegionVO>> result =
    //                ESPhyClusterRegionControllerMethod.listPhyClusterRegions(phyClusterName, ClusterResourceTypeEnum.PRIVATE.getCode());
    //        Assertions.assertTrue(result.success());
    //        Long regionId = null;
    //        for(ClusterRegionVO clusterRegionVO : result.getData()) {
    //            if(AriusObjUtils.isBlack(clusterRegionVO.getLogicClusterIds()) || "-1".equals(clusterRegionVO.getLogicClusterIds())) {
    //                // 如果该 region 没有绑定逻辑集群
    //                regionId = clusterRegionVO.getId();
    //                break;
    //            }
    //        }
    //        if(regionId == null) {
    //            // 如果没有 region，则新增
    //            PhyClusterRegionInfoSource.PhyClusterRegionInfo phyClusterRegionInfo = new PhyClusterRegionInfoSource.PhyClusterRegionInfo();
    //            regionId = phyClusterRegionInfo.getRegionId();
    //        }
    //        Assertions.assertNotNull(regionId);
    //
    //        String operateType = "logicClusterCreate";
    //        WorkOrderDTO workOrderDTO = CustomDataSource.getWorkOrderDTO(operateType);
    //        List<ClusterRegionDTO> clusterRegionDTOs = new ArrayList<>();
    //        ClusterRegionDTO clusterRegionDTO = new ClusterRegionDTO();
    //        clusterRegionDTO.setPhyClusterName(phyClusterName);
    //        clusterRegionDTO.setId(regionId);
    //        clusterRegionDTOs.add(clusterRegionDTO);
    //
    //        Map<String, Object> contentObj = new HashMap<>();
    //        contentObj.put("clusterRegionDTOS", clusterRegionDTOs);
    //        contentObj.put("dataCenter", "cn");
    //        contentObj.put("dataNodeSpec", "");
    //        contentObj.put("dataNodeNu", 0);
    //        contentObj.put("configJson", "");
    //        contentObj.put("id", 0);
    //        contentObj.put("libraDepartment", "");
    //        contentObj.put("quota", 0);
    //        contentObj.put("name", logicClusterName);
    //        contentObj.put("type", ClusterResourceTypeEnum.PRIVATE.getCode());
    //        workOrderDTO.setContentObj(contentObj);
    //
    //        // 提交工单
    //        Result<AriusWorkOrderInfoSubmittedVO> result2 = NormalOrderControllerMethod.submit(operateType, workOrderDTO);
    //        Assertions.assertTrue(result2.success());
    //
    //        // 审批通过工单
    //        Long orderId = result2.getData().getId();
    //        WorkOrderProcessDTO processDTO = CustomDataSource.getWorkOrderProcessDTO(orderId);
    //        Result<Void> result3 = NormalOrderControllerMethod.process(orderId, processDTO);
    //        Assertions.assertTrue(result3.success());
    //    }
}