package com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;


@ApiModel(value = "ESPluginDTO", description = "插件包DTO")
@Data
public class ESPluginDTO extends BaseDTO {
    /**
     * ID主键自增
     */
    @ApiModelProperty("ID主键自增")
    private Long id;

    /**
     * 插件名
     */
    @ApiModelProperty("插件名")
    private String name;

    /**
     * 物理集群Id
     */
    @ApiModelProperty("物理集群Id")
    private String physicClusterId;

    /**
     * 插件版本
     */
    @ApiModelProperty("插件版本")
    private String version;

    /**
     * 插件存储地址
     */
    @ApiModelProperty("插件存储地址")
    private String url;

    /**
     * 插件文件md5
     */
    @ApiModelProperty("插件文件md5")
    private String md5;

    /**
     * 插件描述
     */
    @ApiModelProperty("插件描述")
    private String desc;

    /**
     * 插件创建人
     */
    @ApiModelProperty("插件创建人")
    private String creator;

    /**
     * 是否为系统默认： 0 否  1 是
     */
    @ApiModelProperty("是否为系统默认： 0 否  1 是")
    private Boolean pDefault;

    /**
     * 上传的文件名
     */
    @ApiModelProperty("上传的文件名")
    private String fileName;

    /**
     * 上传的文件
     */
    @ApiModelProperty("上传的文件")
    private MultipartFile uploadFile;

}
