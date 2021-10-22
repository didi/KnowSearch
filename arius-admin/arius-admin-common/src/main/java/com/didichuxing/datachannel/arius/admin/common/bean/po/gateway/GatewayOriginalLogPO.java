package com.didichuxing.datachannel.arius.admin.common.bean.po.gateway;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: D10865
 * @description:
 * @date: Create on 2019/2/26 下午9:09
 * @modified By D10865
 *
 * gateway访问日志
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GatewayOriginalLogPO {
    /**
     * 请求id
     */
    private String requestId;
    /**
     * 请求阶段标识
     */
    private String dltag;
    /**
     * 应用id
     */
    private Integer appid;
    /**
     * 查询的索引
     */
    private String indices;
    /**
     * 日志时间
     */
    private String logTime;

    // 忽略其他字段
}
