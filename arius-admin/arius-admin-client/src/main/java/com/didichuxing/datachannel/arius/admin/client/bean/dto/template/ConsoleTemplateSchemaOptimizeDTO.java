package com.didichuxing.datachannel.arius.admin.client.bean.dto.template;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.client.bean.common.MappingOptimizeItem;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author d06679
 * @date 2019-06-13
 */
@Data
@ApiModel(description = "索引schema信息")
public class ConsoleTemplateSchemaOptimizeDTO extends BaseDTO {

    @ApiModelProperty("索引ID")
    private Integer                   logicId;

    /**
     * type
     */
    @ApiModelProperty("索引mapping优化信息")
    private List<MappingOptimizeItem> items;
}
