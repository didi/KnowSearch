package com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 * @date 2019/4/29
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrder extends BaseEntity {
    /**
     * id
     */
    private Long  id;

    /**
     * 工单类型
     */
    private String  type;

    /**
     * 工单标题
     */
    private String  title;

    /**
     * 结构化的工单内容 json
     */
    private Object  contentObj;

    /**
     * 提交人
     */
    private String  submitor;

    /**
     * 提交人projectid
     */
    private Integer submitorProjectId;


    /**
     * 数据中心
     */
    private String  dataCenter;

    /**
     * 审批信息
     */
    private String opinion;

    /**
     * 申请原因
     */
    private String description;
}