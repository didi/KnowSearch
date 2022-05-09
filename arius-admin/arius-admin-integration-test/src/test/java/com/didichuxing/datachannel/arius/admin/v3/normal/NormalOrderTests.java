package com.didichuxing.datachannel.arius.admin.v3.normal;

import com.didichuxing.datachannel.arius.admin.base.BasePhyClusterInfoTest;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.workorder.WorkOrderDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.workorder.WorkOrderProcessDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.order.OrderTypeVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.order.AriusWorkOrderInfoSubmittedVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.order.AriusWorkOrderInfoVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.order.detail.OrderDetailBaseVO;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ResourceLogicTypeEnum;
import com.didichuxing.datachannel.arius.admin.method.v3.normal.NormalOrderControllerMethod;
import com.didichuxing.datachannel.arius.admin.source.CustomDataSource;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wuxuan
 * @Date 2022/3/31
 */
public class NormalOrderTests extends BasePhyClusterInfoTest {

    /**
     *以用户申请逻辑集群的工单作为测试用例。
     */

    @Test
    public void testGetOrderTypes() throws IOException{
        Result<List<OrderTypeVO>> result= NormalOrderControllerMethod.getOrderTypes();
        Assert.assertTrue(result.success());
    }

    @Test
    public void testSubmit() throws IOException{
        // 用户点击申请逻辑集群，发起工单申请
        String type = "logicClusterCreate";
        WorkOrderDTO workOrderDTO = CustomDataSource.getWorkOrderDTO(type);
        Map<String, Object> contentObj = new HashMap<>();
        contentObj.put("name", phyClusterInfo.getPhyClusterName()+"1");
        contentObj.put("dataNodeSpec", "16-64Gi-3072g");
        contentObj.put("dataNodeNu", 3);
        contentObj.put("responsible", "admin");
        contentObj.put("type", ResourceLogicTypeEnum.PRIVATE.getCode());
        workOrderDTO.setContentObj(contentObj);
        Result<AriusWorkOrderInfoSubmittedVO> result=NormalOrderControllerMethod.submit(type,workOrderDTO);
        Assert.assertTrue(result.success());
    }

    @Test
    public void testProcess() throws IOException{ ;
        // 用户点击申请逻辑集群，发起工单申请
        String type = "logicClusterCreate";
        WorkOrderDTO workOrderDTO = CustomDataSource.getWorkOrderDTO(type);
        Map<String, Object> contentObj = new HashMap<>();
        contentObj.put("name", phyClusterInfo.getPhyClusterName()+"2");
        contentObj.put("dataNodeSpec", "16-64Gi-3072g");
        contentObj.put("dataNodeNu", 3);
        contentObj.put("responsible", "admin");
        contentObj.put("type", ResourceLogicTypeEnum.PRIVATE.getCode());
        workOrderDTO.setContentObj(contentObj);
        Result<AriusWorkOrderInfoSubmittedVO> result=NormalOrderControllerMethod.submit(type,workOrderDTO);
        Assertions.assertTrue(result.success());

        // 通过工单
        Long orderId = result.getData().getId();
        WorkOrderProcessDTO workOrderProcessDTO = CustomDataSource.getWorkOrderProcessDTO(orderId);
        Result<Void> result2 = NormalOrderControllerMethod.process(orderId, workOrderProcessDTO);
        Assertions.assertTrue(result2.success());
    }

    @Test
    public void testCancelOrder() throws IOException{
        // 用户点击申请逻辑集群，发起工单申请
        String type = "logicClusterCreate";
        WorkOrderDTO workOrderDTO = CustomDataSource.getWorkOrderDTO(type);
        Map<String, Object> contentObj = new HashMap<>();
        contentObj.put("name", phyClusterInfo.getPhyClusterName()+"3");
        contentObj.put("dataNodeSpec", "16-64Gi-3072g");
        contentObj.put("dataNodeNu", 3);
        contentObj.put("responsible", "admin");
        contentObj.put("type", ResourceLogicTypeEnum.PRIVATE.getCode());
        workOrderDTO.setContentObj(contentObj);
        Result<AriusWorkOrderInfoSubmittedVO> result=NormalOrderControllerMethod.submit(type,workOrderDTO);
        Assertions.assertTrue(result.success());

        // 取消工单
        Long orderId = result.getData().getId();
        Result<Void> result2=NormalOrderControllerMethod.cancelOrder(orderId);
        Assertions.assertTrue(result.success());
    }

    @Test
    public void testGetOrderDetail() throws IOException{
        // 用户点击申请逻辑集群，发起工单申请
        String type = "logicClusterCreate";
        WorkOrderDTO workOrderDTO = CustomDataSource.getWorkOrderDTO(type);
        Map<String, Object> contentObj = new HashMap<>();
        contentObj.put("name", phyClusterInfo.getPhyClusterName()+"4");
        contentObj.put("dataNodeSpec", "16-64Gi-3072g");
        contentObj.put("dataNodeNu", 3);
        contentObj.put("responsible", "admin");
        contentObj.put("type", ResourceLogicTypeEnum.PRIVATE.getCode());
        workOrderDTO.setContentObj(contentObj);
        Result<AriusWorkOrderInfoSubmittedVO> result=NormalOrderControllerMethod.submit(type,workOrderDTO);
        Assertions.assertTrue(result.success());

        // 获得工单细节
        Long orderId = result.getData().getId();
        Result<OrderDetailBaseVO> result2=NormalOrderControllerMethod.getOrderDetail(orderId);
        Assertions.assertTrue(result.success());
    }

    @Test
    public void testGetOrderApplyList() throws IOException{
        Result<List<AriusWorkOrderInfoVO>> result=NormalOrderControllerMethod.getOrderApplyList(0);
        Assertions.assertTrue(result.success());
    }

    @Test
    public void testGetOrderApprovalList() throws IOException{
        Result<List<AriusWorkOrderInfoVO>> result=NormalOrderControllerMethod.getOrderApprovalList(1);
        Assertions.assertTrue(result.success());
    }
}