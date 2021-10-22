package com.didichuxing.datachannel.arius.admin.common.bean.po.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author: D10865
 * @description:
 * @date: Create on 2019/2/27 下午2:14
 * @modified By D10865
 *
 * 索引维度访问次数
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel(value = "IndexNameQueryAvgRatePO", description = "索引访问统计结果")
public class IndexNameQueryAvgRatePO {

    /**
     * 索引名称
     */
    @ApiModelProperty("索引名称")
    private String indexName;
    /**
     * 访问次数
     */
    @ApiModelProperty("访问频率")
    private Double queryTotalRate;
    /**
     * 统计日期
     */
    @ApiModelProperty("统计日期")
    private String date;

}
