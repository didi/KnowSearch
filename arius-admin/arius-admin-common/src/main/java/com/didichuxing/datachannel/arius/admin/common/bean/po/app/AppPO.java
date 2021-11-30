package com.didichuxing.datachannel.arius.admin.common.bean.po.app;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.DigitResponsible;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 *
 * @author d06679
 * @date 2019/3/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppPO extends BasePO implements DigitResponsible {

    /**
     * 主键
     */
    private Integer id;

    /**
     * 应用名称
     */
    private String  name;

    /**
     * 是否是超管
     */
    private Integer isRoot;

    /**
     * 验证码
     */
    private String  verifyCode;

    /**
     * 部门id
     */
    private String  departmentId;

    /**
     * 部门名称
     */
    private String  department;

    /**
     * 责任人id列表，英文逗号分隔
     */
    private String  responsible;

    /**
     * 备注
     */
    private String  memo;

    /**
     * 删除标志
     */
    private Integer isActive;

    /**
     * 查询限流值
     */
    private Integer queryThreshold;

    /**
     * 租户查询集群
     */
    private String  cluster;

    /**
     * 查询模式
     */
    private Integer searchType;

    /**
     * 数据中心
     */
    private String  dataCenter;


    /******************** 即将废弃字段 ***********************/

    private String  ip;

    private String  indexExp;

}
