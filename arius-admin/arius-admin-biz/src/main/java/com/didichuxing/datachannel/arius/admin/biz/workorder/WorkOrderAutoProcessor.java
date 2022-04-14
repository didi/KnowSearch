package com.didichuxing.datachannel.arius.admin.biz.workorder;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.app.AppDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.WorkOrder;
import com.didichuxing.datachannel.arius.admin.common.bean.po.order.WorkOrderPO;
import com.didichuxing.datachannel.arius.admin.common.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUser;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUser.AUTO_EXER;

/**
 * @author d06679
 * @date 2019-08-04
 */

@Component
public class WorkOrderAutoProcessor {

    private static final ILog      LOGGER = LogFactory.getLog(WorkOrderAutoProcessor.class);

    @Autowired
    private WorkOrderManager       workOrderManager;

    @Autowired
    private HandleFactory          handleFactory;

    public void process() {
        List<WorkOrderPO> workOrderPOs = workOrderManager.getWaitApprovalList(AUTO_EXER.getDesc());
        if (CollectionUtils.isEmpty(workOrderPOs)) {
            LOGGER.info("class=WorkOrderAutoProcessor||method=process||msg=no work order");
            return;
        }

        for (WorkOrderPO orderPO : workOrderPOs) {
            try {

                if (orderPO.getApproverAppId() == null) {
                    LOGGER.warn("class=WorkOrderAutoProcessor||method=process||orderPO={}||msg=SUBMITOR_APP_ID miss", orderPO);
                    continue;
                }

                if (orderPO.getType() == null) {
                    LOGGER.warn("class=WorkOrderAutoProcessor||method=process||orderPO={}||msg=WORK_ORDER_TYPE miss", orderPO);
                    continue;
                }

                if (orderPO.getApplicant() == null) {
                    LOGGER.warn("class=WorkOrderAutoProcessor||method=process||orderPO={}||msg=SUBMITOR miss", orderPO);
                    continue;
                }

                WorkOrder workOrder = new WorkOrder();
                workOrder.setSubmitorAppid(orderPO.getApproverAppId());
                workOrder.setType(orderPO.getType());
                workOrder.setSubmitor(AUTO_EXER.getDesc());

                workOrder.setContentObj(JSON.parseObject(orderPO.getExtensions(), AppDTO.class));

                WorkOrderHandler handler = (WorkOrderHandler) handleFactory.getByHandlerNamePer(workOrder.getType());

                handleWorkOrder(orderPO, workOrder, handler);

            } catch (Exception e) {
                LOGGER.error("class=WorkOrderAutoProcessor||method=process||errMsg={}||orderPO={}||errMsg={}",
                    e.getMessage(), orderPO, e);
            }
        }
    }

    private void handleWorkOrder(WorkOrderPO orderPO, WorkOrder workOrder, WorkOrderHandler handler) throws AdminOperateException {
        if (handler.canAutoReview(workOrder)) {
            if (ResultType.SUCCESS.getCode() == handler.processAgree(workOrder, AriusUser.SYSTEM.getDesc(), "")
                .getCode()) {
                LOGGER.info("class=WorkOrderAutoProcessor||method=process||orderPO={}||msg=process succ", orderPO);
            } else {
                LOGGER.warn("class=WorkOrderAutoProcessor||method=process||orderPO={}||msg=process fail", orderPO);
            }
        } else {
            LOGGER.info("class=WorkOrderAutoProcessor||method=process||orderPO={}||msg=can not auto process", orderPO);
        }
    }
}
