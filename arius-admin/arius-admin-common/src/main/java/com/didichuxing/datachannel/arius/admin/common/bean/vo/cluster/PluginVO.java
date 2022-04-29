package com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description ="插件信息")
public class PluginVO extends BaseVO {
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

    @ApiModelProperty("上传插件类型: 0 系统默认插件, 1 ES能力插件, 2 平台能力插件")
    private Integer pDefault;

    @ApiModelProperty("是否安装: true 是, false 否")
    private Boolean installed;
}