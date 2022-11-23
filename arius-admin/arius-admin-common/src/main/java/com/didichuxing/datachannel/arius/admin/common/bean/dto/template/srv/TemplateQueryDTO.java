package com.didichuxing.datachannel.arius.admin.common.bean.dto.template.srv;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chengxiang
 * @date 2022/5/18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "模板分页查询条件")
public class TemplateQueryDTO extends IndexTemplateDTO {

    @ApiModelProperty("所属物理集群")
    private String cluster;
    @ApiModelProperty("模板健康度")
    private Integer health;
    @ApiModelProperty("是否具备 dcdr")
    private Boolean hasDCDR;
    @ApiModelProperty("排序字段 id、（主从位点差）check_point_diff、（模板健康度）health")
    private String  sortTerm;
    @ApiModelProperty(value = "是否降序排序（默认降序）", dataType = "Boolean", required = false)
    private Boolean orderByDesc = true;
    @ApiModelProperty("是否展示元数据模板")
    private Boolean showMetadata;
}