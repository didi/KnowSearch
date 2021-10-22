package com.didichuxing.datachannel.arius.admin.biz.workorder;

import static com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant.ARIUS_COMMON_GROUP;
import static com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUser.AUTO_EXER;

import com.didichuxing.datachannel.arius.admin.common.component.HandleFactory;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.app.AppDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.WorkOrder;
import com.didichuxing.datachannel.arius.admin.common.bean.po.order.WorkOrderPO;
import com.didichuxing.datachannel.arius.admin.common.component.BaseExtendFactory;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUser;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;

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

    @Autowired
    private AriusConfigInfoService ariusConfigInfoService;

    public void process() {
        List<WorkOrderPO> workOrderPOs = workOrderManager.getWaitApprovalList(AUTO_EXER.getDesc());
        if (CollectionUtils.isEmpty(workOrderPOs)) {
            LOGGER.info("method=process||msg=no work order");
            return;
        }

        Set<String> blackListWOTypeSet = ariusConfigInfoService.stringSettingSplit2Set(ARIUS_COMMON_GROUP,
            "arius.wo.auto.process.black.types", "", ",");

        for (WorkOrderPO orderPO : workOrderPOs) {
            try {

                if (orderPO.getApproverAppId() == null) {
                    LOGGER.warn("method=process||orderPO={}||msg=SUBMITOR_APP_ID miss", orderPO);
                    continue;
                }

                if (orderPO.getType() == null) {
                    LOGGER.warn("method=process||orderPO={}||msg=WORK_ORDER_TYPE miss", orderPO);
                    continue;
                }

                if (orderPO.getApplicant() == null) {
                    LOGGER.warn("method=process||orderPO={}||msg=SUBMITOR miss", orderPO);
                    continue;
                }

                WorkOrder workOrder = new WorkOrder();
                workOrder.setSubmitorAppid(orderPO.getApproverAppId());
                workOrder.setType(orderPO.getType());
                workOrder.setSubmitor(AUTO_EXER.getDesc());

                if (blackListWOTypeSet.contains(workOrder.getType())) {
                    LOGGER.warn("method=process||woType={}||msg=black-list", workOrder.getType());
                    continue;
                }

                workOrder.setContentObj(JSONObject.parseObject(orderPO.getExtensions(), AppDTO.class));

                WorkOrderHandler handler = (WorkOrderHandler) handleFactory.getByHandlerNamePer(workOrder.getType());

                if (handler.canAutoReview(workOrder)) {
                    if (ResultType.SUCCESS.getCode() == handler.processAgree(workOrder, AriusUser.SYSTEM.getDesc(), "")
                        .getCode()) {
                        LOGGER.info("method=process||orderPO={}||msg=process succ", orderPO);
                    } else {
                        LOGGER.warn("method=process||orderPO={}||msg=process fail", orderPO);
                    }
                } else {
                    LOGGER.info("method=process||orderPO={}||msg=can not auto process", orderPO);
                }
            } catch (Exception e) {
                LOGGER.error("class=WorkOrderAutoProcessor||method=process||errMsg={}||orderPO={}||errMsg={}",
                    e.getMessage(), orderPO, e);
            }
        }
    }
}
