package com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail;

import java.util.Date;
import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.constant.workorder.WorkOrderTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.arius.AriusUserInfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author fengqiongfeng
 * @date 2020/8/24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderInfoDetail {

    /**
     * 订单id
     */
    private Long                id;

    /**
     * @see WorkOrderTypeEnum
     */
    private String              type;

    /**
     * 标题
     */
    private String              title;

    /**
     * 申请人部门名称
     */
    private String              appDeptName;

    /**
     * 申请人
     */
    private AriusUserInfo       applicant;

    /**
     * 申请人appId
     */
    private Integer             applicantAppId;

    /**
     * 审批人信息
     */
    private List<AriusUserInfo> approverList;

    /**
     * 任务完成时间
     */
    private Date                finishTime;

    /**
     * 审批信息
     */
    private String              opinion;

    /**
     * 工单状态, 0:待审批, 1:通过, 2:拒绝, 3:取消
     */
    private Integer             status;

    /**
     * 备注
     */
    private String              description;

    /**
     * 申请时间
     */
    protected Date              createTime;

    /**
     * 订单详细信息
     */
    private AbstractOrderDetail detail;

}