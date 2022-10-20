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
@ApiModel(description = "索引force merge")
public class IndexForceMergeDTO extends BaseDTO {

    @ApiModelProperty("max_num_segments")
    private Integer               maxNumSegments;

    @ApiModelProperty("only_expunge_deletes")
    private Boolean               onlyExpungeDeletes;

    @ApiModelProperty("index 列表")
    private List<IndexCatCellDTO> indices;
}
