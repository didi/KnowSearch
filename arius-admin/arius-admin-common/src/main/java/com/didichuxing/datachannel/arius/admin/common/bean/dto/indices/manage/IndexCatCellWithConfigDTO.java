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
@ApiModel(value = "索引带有配置信息")
public class IndexCatCellWithConfigDTO extends IndexCatCellDTO {

    @ApiModelProperty("mapping")
    private String mapping;

    @ApiModelProperty("setting")
    private String setting;

    @ApiModelProperty("alias")
    private String alias;

    @ApiModelProperty("target index")
    private String targetIndex;

    @ApiModelProperty("extra")
    private String extra;
}
