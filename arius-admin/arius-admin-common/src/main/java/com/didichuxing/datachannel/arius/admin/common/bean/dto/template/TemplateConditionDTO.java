package com.didichuxing.datachannel.arius.admin.common.bean.dto.template;

import com.didichuxing.datachannel.arius.admin.common.constant.project.ProjectTemplateAuthEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by linyunan on 2021-10-12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "条件查询索引模板信息")
public class TemplateConditionDTO extends IndexTemplateDTO {
    /**
     * @see ProjectTemplateAuthEnum
     */
    @ApiModelProperty("当前项目拥有的权限（1:管理；2:读写；3:读; -1无权限）")
    private Integer      authType;

    @ApiModelProperty("所属集群")
    private List<String> clusterPhies;

    @ApiModelProperty("排序字段 block_read(禁读)、block_write(禁写")
    private String       sortTerm;
    @ApiModelProperty("模板健康度")
    private Integer health;

    @ApiModelProperty(value = "是否降序排序（默认降序）", dataType = "Boolean", required = false)
    private Boolean      orderByDesc = true;
}