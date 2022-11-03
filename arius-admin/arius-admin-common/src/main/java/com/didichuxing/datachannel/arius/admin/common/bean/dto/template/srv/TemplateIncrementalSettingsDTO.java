package com.didichuxing.datachannel.arius.admin.common.bean.dto.template.srv;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @Authoer: zyl
 * @Date: 2022/10/13
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "模版增量settings实体")
public class TemplateIncrementalSettingsDTO {

    @ApiModelProperty("要修改的settings的模版id列表")
    private List<Integer> templateIdList;

    /**
     * "index.translog.durability" : "async" ? "request"
     * "index.priority" : "10"、"5"、"0"
     */
    @ApiModelProperty("要修改的settings")
    Map<String, String> incrementalSettings;
}
