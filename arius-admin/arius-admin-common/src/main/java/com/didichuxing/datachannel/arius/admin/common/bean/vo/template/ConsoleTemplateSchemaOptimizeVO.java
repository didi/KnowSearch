package com.didichuxing.datachannel.arius.admin.common.bean.vo.template;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.common.MappingOptimizeItem;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 * @date 2019/3/29
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "索引schema信息")
@Deprecated
public class ConsoleTemplateSchemaOptimizeVO extends BaseVO {

    @ApiModelProperty("索引ID")
    private Integer                   id;

    @ApiModelProperty("索引名字")
    private String                    name;

    @ApiModelProperty("索引mapping优化信息")
    private List<MappingOptimizeItem> mappingOptimizeItems;

}