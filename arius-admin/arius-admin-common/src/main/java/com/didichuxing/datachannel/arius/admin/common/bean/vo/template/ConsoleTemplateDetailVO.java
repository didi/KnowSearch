package com.didichuxing.datachannel.arius.admin.common.bean.vo.template;

import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterResourceTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ResourceLogicLevelEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
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
@ApiModel(description = "索引详细信息")
public class ConsoleTemplateDetailVO extends BaseTemplateVO {

    @ApiModelProperty("所属应用名称")
    private String       appName;

    @ApiModelProperty("集群名称")
    private String       cluster;

    /**
     * @see ClusterResourceTypeEnum
     */
    @ApiModelProperty("集群类型")
    private Integer      clusterType;

    /**
     * @see ResourceLogicLevelEnum
     */
    @ApiModelProperty("集群服务等级")
    private Integer      clusterLevel;

    @ApiModelProperty("索引分区列表，按着先后顺序排列")
    private List<String> indices;

    @ApiModelProperty("是否分区")
    private Boolean      cyclicalRoll;

    @ApiModelProperty("模板价值")
    private Integer      value;

    @ApiModelProperty("能够编辑")
    private Boolean      editable;

    @ApiModelProperty("服务等级")
    private Integer      level;

    @ApiModelProperty("是否开启indexRollover能力")
    private Boolean      disableIndexRollover;

    @ApiModelProperty("恢复优先级")
    private Integer      recoveryPriorityLevel;
}