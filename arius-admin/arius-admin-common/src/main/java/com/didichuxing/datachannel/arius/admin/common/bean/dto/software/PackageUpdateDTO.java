package com.didichuxing.datachannel.arius.admin.common.bean.dto.software;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "安装包更新信息")
public class PackageUpdateDTO {
    /**
     * 安装包id
     */
    @ApiModelProperty("安装包id")
    private Integer id;
    /**
     * 描述
     */
    @ApiModelProperty("描述")
    private String describe;
    /**
     * 脚本id
     */
    @ApiModelProperty("脚本id")
    private Integer scriptId;
    /**
     * 传输文件
     */
    @ApiModelProperty("传输文件")
    private MultipartFile uploadFile;
    /**
     * 是否引擎插件
     */
    @ApiModelProperty("是否引擎插件")
    private Integer isEnginePlugin;

    /**
     * 关联的默认安装包分组配置
     */
    @ApiModelProperty("groupConfigList")
    private List<PackageGroupConfigUpdateDTO> groupConfigList;
    /**
     * 软件包类型，1-es安装包、2-gateway安装包、3-es引擎插件、4-gateway引擎插件、5-es平台插件、6-gateway平台插件
     */
    @ApiModelProperty("packageType")
    private Integer packageType;
}
