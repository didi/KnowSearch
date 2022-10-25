package com.didichuxing.datachannel.arius.admin.common.bean.dto.software;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
@ApiModel(description = "更新时的安装包默认分组配置")
public class PackageGroupConfigUpdateDTO {
    /**
     * 分组id
     */
    @ApiModelProperty("分组id")
    private Integer id;
    /**
     * 分组名
     */
    @ApiModelProperty("分组名")
    private String groupName;
    /**
     * 系统配置
     */
    @ApiModelProperty("系统配置")
    private String systemConfig;
    /**
     * 运行时配置
     */
    @ApiModelProperty("运行时配置")
    private String runningConfig;
    /**
     * 文件配置
     */
    @ApiModelProperty("文件配置")
    private String fileConfig;
    /**
     * 关联安装包id
     */
    @ApiModelProperty("关联安装包id")
    private Integer packageId;
}
