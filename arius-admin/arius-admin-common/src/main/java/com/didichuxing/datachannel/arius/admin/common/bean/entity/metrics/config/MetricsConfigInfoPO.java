package com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.config;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.MetricsTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 标准配置信息
 *
 * @author shizeying
 * @date 2022/05/24
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetricsConfigInfoPO extends BaseEntity {

    /**
     * 用户名
     */
    private String userName;

    /**
     * 一级目录下的指标配置类型,如集群看板，网关看板
     * @see MetricsTypeEnum
     */
    private String       firstMetricsType;

    /**
     * 二级目录下的指标配置类型,如集群看板下的总览指标类型
     * @see MetricsTypeEnum
     */
    private String       secondMetricsType;

    /**
     * 二级目录指标配置下具体的配置列表,如cpu利用率
     */
    private List<String> metricsTypes;
}