package com.didichuxing.datachannel.arius.admin.common.bean.vo.software;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "查询列表安装包VO")
public class PackagePageVO extends BaseVO {
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
     * 版本
     */
    @ApiModelProperty("版本")
    private String version;
    /**
     * 创建者
     */
    @ApiModelProperty("创建者")
    private String creator;
    /**
     * 是否正在使用
     */
    @ApiModelProperty("是否正在使用")
    private Boolean isUsing;
    /**
     * 描述
     */
    @ApiModelProperty("描述")
    private String describe;
    /**
     * 文件地址
     */
    @ApiModelProperty("文件地址")
    private String url;
    /**
     * 软件包类型
     */
    @ApiModelProperty("软件包类型")
    private Integer packageType;
}
