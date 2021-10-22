package com.didichuxing.datachannel.arius.admin.client.bean.vo.template;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.client.constant.resource.ResourceLogicLevelEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ResourceLogicTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author d06679
 * @date 2019/3/29
 */
@Data
@ApiModel(description = "索引详细信息")
public class ConsoleTemplateDetailVO extends BaseTemplateVO {

    /**
     * 应用名称
     */
    @ApiModelProperty("所属应用名称")
    private String       appName;

    /**
     * 集群名称
     */
    @ApiModelProperty("集群")
    private String       cluster;

    /**
     * 集群类型
     * @see ResourceLogicTypeEnum
     */
    @ApiModelProperty("集群类型")
    private Integer      clusterType;

    /**
     * 集群服务等级
     * @see ResourceLogicLevelEnum
     */
    @ApiModelProperty("集群服务等级")
    private Integer       clusterLevel;

    /**
     * 模板对应的索引列表，按着时间排序
     */
    @ApiModelProperty("索引分区列表，按着先后顺序排列")
    private List<String> indices;

    /**
     * 是否分区
     */
    @ApiModelProperty("是否分区")
    private Boolean      cyclicalRoll;

    /**
     * 模板价值
     */
    @ApiModelProperty("模板价值")
    private Integer      value;

    /**
     * 能够编辑
     */
    @ApiModelProperty("能够编辑")
    private Boolean      editable;

}
