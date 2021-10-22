package com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail;

import lombok.Data;

/**
 * @author fengqiongfeng
 * @date 2020/8/24
 */
@Data
public class AppCreateOrderDetail extends AbstractOrderDetail {
    /**
     * appId
     */
    private Integer appId;
    /**
     * app名称
     */
    private String name;
    /**
     * 部门id
     */
    private String departmentId;
    /**
     * 责任人
     */
    private String responsible;
    /**
     * 部门
     */
    private String department;

}