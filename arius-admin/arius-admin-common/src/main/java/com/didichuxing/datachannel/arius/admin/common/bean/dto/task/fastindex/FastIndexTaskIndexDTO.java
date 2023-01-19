package com.didichuxing.datachannel.arius.admin.common.bean.dto.task.fastindex;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 数据迁移任务索引实体
 *
 * @author didi
 * @date 2022/10/31
 */
@ApiModel("数据迁移任务索引实体")
@Data
public class FastIndexTaskIndexDTO {
    @ApiModelProperty("原索引或模版名称")
    private String       resourceNames;
    @ApiModelProperty("索引的type")
    private List<String> indexTypes;
}
