package com.didichuxing.datachannel.arius.admin.common.bean.dto.template.srv;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chengxiang
 * @date 2022/5/24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "模板扩缩容参数")
public class TemplateAdjustShardDTO extends BaseTemplateSrvOpenDTO {

    @ApiModelProperty("模板Id")
    private Integer templateId;

    @ApiModelProperty("调整后的shard数量")
    private Integer shardNum;
}
