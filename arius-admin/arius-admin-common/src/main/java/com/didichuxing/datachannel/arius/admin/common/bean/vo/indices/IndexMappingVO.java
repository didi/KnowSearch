package com.didichuxing.datachannel.arius.admin.common.bean.vo.indices;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;
import com.didichuxing.datachannel.arius.admin.common.mapping.Field;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lyn
 * @date 2021/09/28
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "IndexMappingVO", description = "索引mapping信息")
public class IndexMappingVO extends BaseVO {

    @ApiModelProperty("索引名称")
    private String            indexName;

    @ApiModelProperty("索引mapping列表信息")
    private List<Field>       fields;

    @ApiModelProperty("索引mapping信息")
    private String mappings;
}
