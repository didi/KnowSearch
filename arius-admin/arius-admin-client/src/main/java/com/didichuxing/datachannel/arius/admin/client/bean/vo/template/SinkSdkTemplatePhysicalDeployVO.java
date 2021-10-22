package com.didichuxing.datachannel.arius.admin.client.bean.vo.template;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author d06679
 * @date 2019/3/29
 */
@Data
@ApiModel(description = "模板信息")
public class SinkSdkTemplatePhysicalDeployVO {

    /**
     * 模板ID
     */
    @ApiModelProperty("模板ID")
    private Long    physicalId;

    /**
     * 模板名字
     */
    @ApiModelProperty("模板名字")
    private String  templateName;

    /**
     * shardRouting
     */
    @ApiModelProperty("shardRouting")
    private Integer shardRouting;

    /**
     * 所在集群
     */
    @ApiModelProperty("所在集群")
    private String  cluster;

    @ApiModelProperty("是否是默认写")
    private Boolean defaultWriterFlags;

    @ApiModelProperty("组ID")
    private String groupId;
}
