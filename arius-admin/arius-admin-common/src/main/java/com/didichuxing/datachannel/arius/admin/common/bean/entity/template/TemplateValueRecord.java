package com.didichuxing.datachannel.arius.admin.common.bean.entity.template;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplateValueRecord {
    /**
     * 统计时间
     */
    private Long    timestamp;
    /**
     * 索引模板id
     */
    private Integer logicTemplateId;
    /**
     * 价值
     */
    private Integer value;
    /**
     * 访问量
     */
    private Long    accessCount;
    /**
     * 大小G
     */
    private Double  sizeG;
    /**
     * 逻辑集群
     */
    private String  logicCluster;
}
