package com.didichuxing.datachannel.arius.admin.common.bean.entity.template;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * es pipeline
 *
 * @author shizeying
 * @date 2022/08/12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ESPipeline {
    private String  cluster;
    private String  pipelineId;
    private String  dateField;
    private String  dateFieldFormat;
    private String  dateFormat;
    private Integer expireDay;
    private Integer rateLimit;
    private Integer version;
    private String  idField;
    private String  routingField;
}