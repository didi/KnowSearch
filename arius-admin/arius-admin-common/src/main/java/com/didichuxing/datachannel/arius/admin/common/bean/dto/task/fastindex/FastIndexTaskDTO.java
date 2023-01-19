package com.didichuxing.datachannel.arius.admin.common.bean.dto.task.fastindex;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 数据迁移子任务实体
 *
 * @author didi
 * @date 2022/10/31
 */
@ApiModel("数据迁移子任务实体")
@Data
public class FastIndexTaskDTO {
    @ApiModelProperty("原索引名称（all2one时为多个）")
    private List<FastIndexTaskIndexDTO> sourceIndexList;
    @ApiModelProperty("原模版ID")
    private Integer                     sourceTemplateId;
    @ApiModelProperty("目标索引或模版")
    private String                      targetName;
    @ApiModelProperty("mappings")
    private String                      mappings;
    @ApiModelProperty("settings")
    private String                      settings;
}
