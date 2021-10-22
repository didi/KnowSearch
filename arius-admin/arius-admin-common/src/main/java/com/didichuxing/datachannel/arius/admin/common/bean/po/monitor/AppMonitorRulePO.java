package com.didichuxing.datachannel.arius.admin.common.bean.po.monitor;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;
import lombok.Data;

@Data
public class AppMonitorRulePO extends BasePO {
    private Long id;

    private String strategyName;

    private Long strategyId;

    private Integer appId;

    private String operator;
}
