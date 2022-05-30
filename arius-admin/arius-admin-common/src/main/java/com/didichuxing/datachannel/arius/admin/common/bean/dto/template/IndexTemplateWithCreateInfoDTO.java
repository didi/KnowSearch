package com.didichuxing.datachannel.arius.admin.common.bean.dto.template;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author chengxiang
 * @date 2022/5/26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "创建逻辑模板DTO")
public class IndexTemplateWithCreateInfoDTO extends IndexTemplateDTO {

    @ApiModelProperty("是否分区")
    private Boolean cyclicalRoll;

    @ApiModelProperty("mapping")
    private String mapping;

    @ApiModelProperty("settings信息")
    private String settings;

}
