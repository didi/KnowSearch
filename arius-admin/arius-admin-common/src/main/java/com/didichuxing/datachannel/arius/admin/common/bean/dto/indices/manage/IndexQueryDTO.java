package com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.manage;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;
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
@ApiModel(description = "索引分页查询条件")
public class IndexQueryDTO extends BaseDTO {

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("所属集群")
    private String cluster;

    @ApiModelProperty("健康状态")
    private String health;
}
