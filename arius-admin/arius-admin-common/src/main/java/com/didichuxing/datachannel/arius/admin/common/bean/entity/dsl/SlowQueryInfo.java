package com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl;

import com.alibaba.fastjson.JSON;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: D10865
 * @Description:
 * @Date: Create on 2018/10/17 下午9:51
 * @Modified By
 *
 * 慢查语句信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "dsl慢查详情")
public class SlowQueryInfo {
    /**
     * dsl查询模板MD5
     */
    @ApiModelProperty(value = "dsl查询模板MD5")
    private String            dslTemplateMd5;
    /**
     * DSL查询模板
     */
    @ApiModelProperty(value = "DSL查询模板")
    private String            dslTemplate;
    /**
     * dsl查询语句
     */
    @ApiModelProperty(value = "dsl查询语句")
    private String            dsl;
    /**
     * 查询索引
     */
    @ApiModelProperty(value = "查询索引")
    private String            indices;
    /**
     * 查询次数
     */
    @ApiModelProperty(value = "查询次数")
    private Long              count;
    /**
     * 查询耗时
     */
    @ApiModelProperty(value = "查询耗时")
    private Double            cost;
    /**
     * 慢查阈值
     */
    @ApiModelProperty(value = "慢查阈值")
    private Long              slowDslThreshold;
    /**
     * 查询原因
     */
    @ApiModelProperty(value = "查询原因")
    private String            cause;
    /**
     * 慢查原因类型
     */
    @ApiModelProperty(value = "慢查原因类型：USER_DSL/ES")
    private SlowDslReasonType slowReasonType;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

}
