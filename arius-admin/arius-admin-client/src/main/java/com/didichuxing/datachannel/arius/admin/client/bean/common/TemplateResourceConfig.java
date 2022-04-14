package com.didichuxing.datachannel.arius.admin.client.bean.common;

import lombok.Data;

/**
 * @author d06679
 * @date 2019-06-25
 */
@Data
public class TemplateResourceConfig {

    /**
     * 文档大小的基准 单位：KB
     */
    private Double docSizeBaseline = 1.0;

    /**
     * 每个CPU的tps能力 单位：条/s
     */
    private Double tpsPerCpu       = 2300.0;

    /**
     * 每个CPU的查询能力  单位：ms
     */
    private Double queryTimePerCpu = 1000.0;

}
