package com.didichuxing.datachannel.arius.admin.common.bean.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 索引模板价值分
 * @author wangshu
 * @date 2020/09/09
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndexTemplateValue implements Serializable {

    private static final long serialVersionUID = 1905122041950251207L;

    /**
     * 索引模板id
     */
    private Integer           logicTemplateId;

    /**
     * 价值
     */
    private Integer           value;

    /**
     * 访问量
     */
    private Long              accessCount;

    /**
     * 大小G
     */
    private Double            sizeG;

    /**
     * 逻辑集群
     */
    private String            logicCluster;

}
