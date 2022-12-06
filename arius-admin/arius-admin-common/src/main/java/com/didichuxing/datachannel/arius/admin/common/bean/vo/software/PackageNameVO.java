package com.didichuxing.datachannel.arius.admin.common.bean.vo.software;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "安装包名称VO")
public class PackageNameVO {
    /**
     * 安装包id
     */
    @ApiModelProperty("安装包id")
    private Integer id;
    /**
     * 名称
     */
    @ApiModelProperty("名称")
    private String name;
}
