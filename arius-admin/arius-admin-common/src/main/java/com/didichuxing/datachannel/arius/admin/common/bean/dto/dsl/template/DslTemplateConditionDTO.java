package com.didichuxing.datachannel.arius.admin.common.bean.dto.dsl.template;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.PageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author cjm
 */
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(description = "DSL模版分页列表查询条件")
public class DslTemplateConditionDTO extends PageDTO {

    @ApiModelProperty(value = "应用ID", dataType = "Integer", required = false)
    private Integer  projectId;

    @ApiModelProperty(value = "排序信息（精确）", dataType = "String", required = false)
    private String  sortInfo;

    @ApiModelProperty(value = "是否逆序排序（默认逆序）", dataType = "Boolean", required = false)
    private Boolean orderByDesc = true;

    @ApiModelProperty(value = "DSL模版MD5（精确）", dataType = "String", required = false)
    private String  dslTemplateMd5;

    @ApiModelProperty(value = "查询索引（模糊）", dataType = "String", required = false)
    private String  queryIndex;

    @ApiModelProperty(value = "DSL模版最近使用时间start（时间戳ms）", dataType = "Long", required = true)
    private Long    startTime;

    @ApiModelProperty(value = "DSL模版最近使用时间end（时间戳ms）", dataType = "Long", required = true)
    private Long    endTime;
    @ApiModelProperty("是否展示元数据查询模板")
    private Boolean isShowQueryTemplateWithMetaData;
}
