package com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.manage;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndexCatCellDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chengxiang
 * @date 2022/5/31
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "索引创建信息")
public class IndexCatCellWithConfigDTO extends IndexCatCellDTO {

    @ApiModelProperty("mapping")
    private String mapping;

    @ApiModelProperty("setting")
    private String setting;

    @ApiModelProperty("alias")
    private String alias;
}
