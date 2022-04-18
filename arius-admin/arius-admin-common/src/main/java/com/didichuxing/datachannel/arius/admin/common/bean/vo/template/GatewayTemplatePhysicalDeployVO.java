package com.didichuxing.datachannel.arius.admin.common.bean.vo.template;

import java.util.Map;
import java.util.Set;

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
public class GatewayTemplatePhysicalDeployVO {

    /**
     * 模板名字
     */
    @ApiModelProperty("模板名字")
    private String templateName;

    /**
     * 所在集群
     */
    @ApiModelProperty("所在集群")
    private String cluster;

    /**
     * 是否是默认写索引标识
     */
    @ApiModelProperty("defaultWriterFlags")
    private Boolean defaultWriterFlags;

    /**
     * 组ID
     */
    @ApiModelProperty("groupId")
    private String groupId;

    /**
     * rack
     */
    @ApiModelProperty("rack")
    private String rack;

    /**
     * shard个数
     */
    @ApiModelProperty("shard个数")
    private Integer shardNum;

    /**
     * kafka topic
     */
    @ApiModelProperty("kafka-topic")
    private String topic;

    /**
     * 能够查询的app列表
     */
    @ApiModelProperty("能够查询的app列表")
    private Set<Integer> accessApps;

    /**
     * 用于索引多type改造   是否启用索引名称映射 0 禁用 1 启用
     */
    @ApiModelProperty("能够查询的app列表")
    private Boolean mappingIndexNameEnable;

    /**
     * 多type索引type名称到单type索引模板名称的映射
     */
    @ApiModelProperty("type名称到索引的映射")
    private Map<String/*typeName*/, String/*templateName*/> typeIndexMapping;
}
