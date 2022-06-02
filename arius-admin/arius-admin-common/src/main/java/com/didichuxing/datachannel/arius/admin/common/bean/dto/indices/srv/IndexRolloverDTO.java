package com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.srv;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndexCatCellDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author chengxiang
 * @date 2022/6/2
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "索引rollover")
public class IndexRolloverDTO extends BaseDTO {

    @ApiModelProperty("max age")
    private String maxAge;

    @ApiModelProperty("max docs")
    private Integer maxDocs;

    @ApiModelProperty("max size")
    private Integer maxSize;

    @ApiModelProperty("index 列表")
    private List<IndexCatCellDTO> indices;
}
