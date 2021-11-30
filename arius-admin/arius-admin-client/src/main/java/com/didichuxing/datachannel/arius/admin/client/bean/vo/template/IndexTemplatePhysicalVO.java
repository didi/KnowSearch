package com.didichuxing.datachannel.arius.admin.client.bean.vo.template;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;

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
@ApiModel(description = "物理模板信息")
public class IndexTemplatePhysicalVO extends BaseVO {

    @ApiModelProperty("模板ID")
    private Long    id;

    @ApiModelProperty("逻辑模板id")
    private Integer logicId;

    @ApiModelProperty("逻辑模板id")
    private String  logicName;

    @ApiModelProperty("模板名称")
    private String  name;

    @ApiModelProperty("表达式")
    private String  expression;

    @ApiModelProperty("物理集群名字")
    private String  cluster;

    @ApiModelProperty("rack")
    private String  rack;

    @ApiModelProperty("shard")
    private Integer shard;

    @ApiModelProperty("shardRouting")
    private Integer shardRouting;

    @ApiModelProperty("版本")
    private Integer version;

    @ApiModelProperty("角色(1:主；2:从)")
    private Integer role;

    @ApiModelProperty("状态(1:常规；-1:索引删除中；-2:删除)")
    private Integer status;

    @ApiModelProperty("配置信息")
    private String  config;

    @ApiModelProperty("描述信息")
    private String  memo;

}
