package com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.quickcommand;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 详细介绍类情况.
 *
 * @ClassName indicesVO
 * @Author gyp
 * @Date 2022/6/1
 * @Version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndicesVO {
    @ApiModelProperty("模板列表数量")
    private String health;
    @ApiModelProperty("模板列表数量")
    private String status;
    @ApiModelProperty("模板列表数量")
    private String index;
    @ApiModelProperty("模板列表数量")
    private String uuid;
    @ApiModelProperty("模板列表数量")
    private String pri;
    @ApiModelProperty("模板列表数量")
    private String rep;
}