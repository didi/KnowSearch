package com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "ESPluginDTO", description = "插件包DTO")
public class ESPluginDTO extends BaseDTO {

    @ApiModelProperty("ID主键自增")
    private Long id;

    @ApiModelProperty("插件名")
    private String name;

    @ApiModelProperty("物理集群Id")
    private String physicClusterId;

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

    @ApiModelProperty("上传的文件名")
    private String fileName;

    @ApiModelProperty("上传的文件")
    private MultipartFile uploadFile;

}
