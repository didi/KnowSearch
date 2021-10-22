package com.didichuxing.datachannel.arius.admin.client.bean.common;

import lombok.Data;

/**
 * 索引模板价值分
 * @author wangshu
 * @date 2020/09/09
 */
@Data
public class IndexTemplateValue{

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
