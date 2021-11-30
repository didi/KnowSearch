package com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description ="插件信息")
public class ESPluginVO extends BaseVO {
    @ApiModelProperty("ID主键自增")
    private Long id;

    @ApiModelProperty("插件名")
    private String name;

    @ApiModelProperty("插件版本")
    private String version;

    @ApiModelProperty("插件存储地址")
    private String url;

    @ApiModelProperty("插件文件md5")
    private String md5;

    @ApiModelProperty("插件描述")
    private String desc;

    @ApiModelProperty("插件创建人")
    private String creator;

    @ApiModelProperty("是否为系统默认： 0 否  1 是")
    private Boolean pDefault;

    @ApiModelProperty("是否安装: true 是, false 否")
    private Boolean installed;
}