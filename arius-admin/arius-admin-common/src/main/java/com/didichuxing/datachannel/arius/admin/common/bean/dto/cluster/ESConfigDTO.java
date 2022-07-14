package com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "配置包DTO")
public class ESConfigDTO extends BaseDTO {

    @ApiModelProperty("主键")
    private Long    id;

    @ApiModelProperty("集群id")
    private Long    clusterId;

    @ApiModelProperty("配置文件名称")
    private String  typeName;

    @ApiModelProperty("组件名称")
    private String  enginName;

    @ApiModelProperty("配置内容")
    private String  configData;

    @ApiModelProperty("配置描述")
    private String  desc;

    @ApiModelProperty("配置tag")
    private String  versionTag;

    @ApiModelProperty("配置版本")
    private Integer versionConfig;

    @ApiModelProperty("是否在使用")
    private Integer selected;
}
