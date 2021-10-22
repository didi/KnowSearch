package com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "配置包DTO")
public class ESConfigDTO extends BaseDTO  {

    /**
     * ID主键自增
     */
    @ApiModelProperty("主键")
    private Long    id;

    /**
     * 集群id
     */
    @ApiModelProperty("集群id")
    private Long    clusterId;

    /**
     * 配置文件名称
     */
    @ApiModelProperty("配置文件名称")
    private String  typeName;

    /**
     * 角色名称
     */
    @ApiModelProperty("组件名称")
    private String  enginName;

    /**
     * 配置内容
     */
    @ApiModelProperty("配置内容")
    private String  configData;

    /**
     * 配置描述
     */
    @ApiModelProperty("配置描述")
    private String  desc;

    /**
     * 配置tag
     */
    @ApiModelProperty("配置tag")
    private String  versionTag;

    /**
     * 配置版本
     */
    @ApiModelProperty("配置版本")
    private Integer  versionConfig;

    /**
     * 是否在使用
     */
    @ApiModelProperty("是否在使用")
    private Integer  selected;
}
