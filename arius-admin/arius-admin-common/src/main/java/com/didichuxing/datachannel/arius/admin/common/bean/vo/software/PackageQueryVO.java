package com.didichuxing.datachannel.arius.admin.common.bean.vo.software;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "查询安装包VO")
public class PackageQueryVO extends BaseVO {
    /**
     * 安装包id
     */
    @ApiModelProperty("安装包id")
    private Integer id;
    /**
     * 安装包名字
     */
    @ApiModelProperty("安装包名字")
    private String name;
    /**
     * 地址
     */
    @ApiModelProperty("地址")
    private String url;
    /**
     * 版本
     */
    @ApiModelProperty("版本")
    private String version;
    /**
     * 描述
     */
    @ApiModelProperty("描述")
    private String describe;
    /**
     * 是否引擎插件
     */
    @ApiModelProperty("是否引擎插件")
    private Integer isEnginePlugin;
    /**
     * 脚本id
     */
    @ApiModelProperty("脚本id")
    private Integer scriptId;
    /**
     * 软件包类型
     */
    @ApiModelProperty("软件包类型")
    private Integer packageType;
    /**
     * 关联的默认安装包分组配置
     */
    @ApiModelProperty("关联的默认安装包分组配置")
    private List<PackageGroupConfigQueryVO> groupConfigList;
}
