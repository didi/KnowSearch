package com.didichuxing.datachannel.arius.admin.method.v3.normal;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.didichuxing.datachannel.arius.admin.AriusClient;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.workorder.WorkOrderDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.workorder.WorkOrderProcessDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.order.OrderTypeVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.order.AriusWorkOrderInfoSubmittedVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.order.AriusWorkOrderInfoVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.order.detail.OrderDetailBaseVO;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_NORMAL;

/**
 * @author cjm
 */
public class NormalOrderControllerMethod {

    public static final String ORDER = V3_NORMAL + "/order";

    public static Result<List<OrderTypeVO>> getOrderTypes() throws IOException {
        String path = String.format("%s/type-enums", ORDER);
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<List<OrderTypeVO>>>(){});
    }

    public static Result<AriusWorkOrderInfoSubmittedVO> submit(String type, WorkOrderDTO workOrderDTO) throws IOException {
        String path = String.format("%s/%s/submit", ORDER, type);
        return JSON.parseObject(AriusClient.put(path, workOrderDTO), new TypeReference<Result<AriusWorkOrderInfoSubmittedVO>>(){});
    }

    public static Result<Void> process(Long orderId, WorkOrderProcessDTO processDTO) throws IOException {
        String path = String.format("%s/%d", ORDER, orderId);
        return JSON.parseObject(AriusClient.put(path, processDTO), new TypeReference<Result<Void>>(){});
    }

    public static Result<Void> cancelOrder(Long orderId) throws IOException {
        String path = String.format("%s/%d", ORDER, orderId);
        return JSON.parseObject(AriusClient.delete(path), new TypeReference<Result<Void>>(){});
    }

    public static Result<OrderDetailBaseVO> getOrderDetail(Long orderId) throws IOException {
        String path = String.format("%s/%d", ORDER, orderId);
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<OrderDetailBaseVO>>(){});
    }

    public static Result<List<AriusWorkOrderInfoVO>> getOrderApplyList(Integer status) throws IOException {
        String path = String.format("%s/orders", ORDER);
        Map<String, Object> params = new HashMap<>();
        params.put("status", status);
        return JSON.parseObject(AriusClient.get(path, params), new TypeReference<Result<List<AriusWorkOrderInfoVO>>>(){});
    }

    public static Result<List<AriusWorkOrderInfoVO>> getOrderApprovalList(Integer status) throws IOException {
        String path = String.format("%s/approvals", ORDER);
        Map<String, Object> params = new HashMap<>();
        params.put("status", status);
        return JSON.parseObject(AriusClient.get(path, params), new TypeReference<Result<List<AriusWorkOrderInfoVO>>>(){});
    }
}