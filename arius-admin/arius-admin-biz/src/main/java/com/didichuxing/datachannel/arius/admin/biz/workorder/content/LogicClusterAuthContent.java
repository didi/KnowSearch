package com.didichuxing.datachannel.arius.admin.biz.workorder.content;

import com.didichuxing.datachannel.arius.admin.client.constant.app.AppClusterLogicAuthEnum;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LogicClusterAuthContent extends BaseContent {
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
}
