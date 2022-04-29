package com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail;

import com.didichuxing.datachannel.arius.admin.common.constant.app.AppClusterLogicAuthEnum;
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
public class LogicClusterAuthOrderDetail extends AbstractOrderDetail {
    /**
     * 逻辑模板id
     */
    private Long    logicClusterId;

    /**
     * 逻辑模板名字
     */
    private String  logicClusterName;

    /**
     * 权限类型
     * @see AppClusterLogicAuthEnum
     */
    private Integer authCode;

    /**
     * 申请说明
     */
    private String  memo;

    /**
     * 责任人
     */
    private String  responsible;
}