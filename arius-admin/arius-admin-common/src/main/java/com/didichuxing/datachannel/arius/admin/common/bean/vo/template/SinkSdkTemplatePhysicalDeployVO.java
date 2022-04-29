package com.didichuxing.datachannel.arius.admin.common.bean.vo.template;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 * @date 2019/3/29
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "模板信息")
public class SinkSdkTemplatePhysicalDeployVO {

    @ApiModelProperty("模板ID")
    private Long    physicalId;

    @ApiModelProperty("模板名字")
    private String  templateName;

    @ApiModelProperty("shardRouting")
    private Integer shardRouting;

    @ApiModelProperty("所在集群")
    private String  cluster;

    @ApiModelProperty("是否是默认写")
    private Boolean defaultWriterFlags;

    @ApiModelProperty("组ID")
    private String groupId;
}
