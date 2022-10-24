package com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author: D10865
 * @description:
 * @date: Create on 2019/1/15 下午8:21
 * @modified By D10865
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "dsl慢查语句")
public class SlowDsl {
    /**
     * 慢查次数
     */
    @ApiModelProperty(value = "慢查次数")
    private Long                count;
    /**
     * 慢查详情
     */
    @ApiModelProperty(value = "慢查详情")
    private List<SlowQueryInfo> details;
}
