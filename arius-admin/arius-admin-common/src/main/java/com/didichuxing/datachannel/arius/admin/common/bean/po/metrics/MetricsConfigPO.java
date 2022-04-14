package com.didichuxing.datachannel.arius.admin.common.bean.po.metrics;

import com.baomidou.mybatisplus.annotation.TableName;
import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("metrics_config")
public class MetricsConfigPO extends BasePO {
    /**
     * id自增
     */
    private Integer id;

    /**
     * 用户的账号
     */
    private String domainAccount;

    /**
     * 指标看板的配置
     */
    private String metricInfo;
}
