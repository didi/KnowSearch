package com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.other.cluster;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Created by linyunan on 2021-08-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("大索引, 大于10亿文档数的索引")
public class BigIndexMetricsVO implements Serializable {

    @ApiModelProperty("索引名称")
    private String                  indexName;

    @ApiModelProperty("归属节点信息")
    private List<IndexBelongNodeVO> belongNodeInfo;

    @ApiModelProperty("文档数")
    private Long                 docsCount;
}
