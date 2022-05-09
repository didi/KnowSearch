package com.didichuxing.datachannel.arius.admin.biz.workorder.handler;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.constant.workorder.WorkOrderTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.arius.AriusUserInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.WorkOrder;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.AbstractOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.QueryDslLimitEditOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.po.order.AriusWorkOrderInfoPO;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.biz.workorder.BaseWorkOrderHandler;
import com.didichuxing.datachannel.arius.admin.biz.workorder.content.QueryDslLimitEditContent;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.DslQueryLimit;
import com.didichuxing.datachannel.arius.admin.metadata.service.DslStatisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author d06679
 * @date 2019/4/29
 */
@Service("queryDslLimitEditHandler")
public class QueryDslLimitEditHandler extends BaseWorkOrderHandler {

    @Autowired
    private DslStatisService dslStatisService;

    /**
     * 工单是否自动审批
     *
     * @param workOrder 工单类型
     * @return result
     */
    @Override
    public boolean canAutoReview(WorkOrder workOrder) {
        return false;
    }

    @Override
    public AbstractOrderDetail getOrderDetail(String extensions) {
        QueryDslLimitEditContent content = JSON.parseObject(extensions, QueryDslLimitEditContent.class);

        return ConvertUtil.obj2Obj(content, QueryDslLimitEditOrderDetail.class);
    }

    @Override
    public List<AriusUserInfo> getApproverList(AbstractOrderDetail detail) {
        return getRDOrOPList();
    }

    @Override
    public Result<Void> checkAuthority(AriusWorkOrderInfoPO orderPO, String userName) {
        if (isRDOrOP(userName)) {
            return Result.buildSucc();
        }
        return Result.buildFail(ResultType.OPERATE_FORBIDDEN_ERROR.getMessage());
    }

    /**************************************** protected method ******************************************/

    /**
     * 验证用户提供的参数
     *
     * @param workOrder 工单
     * @return result
     */
    @Override
    protected Result<Void> validateConsoleParam(WorkOrder workOrder) {
        QueryDslLimitEditContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            QueryDslLimitEditContent.class);

        if (AriusObjUtils.isNull(content.getDslTemplateMd5())) {
            return Result.buildParamIllegal("查询模板为空");
        }

        if (AriusObjUtils.isNull(content.getQueryLimit())) {
            return Result.buildParamIllegal("查询模板限流值为空");
        }

        return Result.buildSucc();
    }

    @Override
    protected String getTitle(WorkOrder workOrder) {
        QueryDslLimitEditContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            QueryDslLimitEditContent.class);

        WorkOrderTypeEnum workOrderTypeEnum = WorkOrderTypeEnum.valueOfName(workOrder.getType());
        if (workOrderTypeEnum == null) {
            return "";
        }
        return content.getDslTemplateMd5() + workOrderTypeEnum.getMessage();
    }

    /**
     * 验证用户是否有该工单权限
     *
     * @param workOrder 工单内容
     * @return result
     */
    @Override
    protected Result<Void> validateConsoleAuth(WorkOrder workOrder) {
        return Result.buildSucc();
    }

    /**
     * 验证平台参数
     *
     * @param workOrder 工单内容
     * @return result
     */
    @Override
    protected Result<Void> validateParam(WorkOrder workOrder) {
        return Result.buildSucc();
    }

    /**
     * 处理工单
     *
     * @param workOrder 工单
     * @return result
     */
    @Override
    protected Result<Void> doProcessAgree(WorkOrder workOrder, String approver) {
        QueryDslLimitEditContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            QueryDslLimitEditContent.class);

        DslQueryLimit dslQueryLimit = new DslQueryLimit();
        dslQueryLimit.setAppid(workOrder.getSubmitorAppid());
        dslQueryLimit.setQueryLimit(content.getQueryLimit());
        dslQueryLimit.setDslTemplateMd5(content.getDslTemplateMd5());

        List<DslQueryLimit> dslQueryLimitList = new ArrayList<>();
        dslQueryLimitList.add(dslQueryLimit);

        Result<Boolean> result = dslStatisService.batchUpdateQueryLimit(dslQueryLimitList, approver);

        if (result.failed()) {
            return Result.buildFail("模版扩缩容失败！");
        }

        return Result.buildFrom(result);
    }
}