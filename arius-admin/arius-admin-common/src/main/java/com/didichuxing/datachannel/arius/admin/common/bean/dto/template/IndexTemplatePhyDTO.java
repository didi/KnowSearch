package com.didichuxing.datachannel.arius.admin.common.bean.dto.template;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chengxiang
 * @date 2022/5/10
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "物理模板信息")
public class IndexTemplatePhyDTO extends BaseDTO {

    @ApiModelProperty("模板ID")
    private Long                           id;

    @ApiModelProperty("逻辑模板id")
    private Integer                        logicId;

    @ApiModelProperty("模板名称")
    private String                         name;

    @ApiModelProperty("表达式")
    private String                         expression;

    @ApiModelProperty("物理集群名字")
    private String                         cluster;

    @ApiModelProperty("资源是否是默认物理索引")
    private Boolean                        defaultWriterFlags;

    @ApiModelProperty("组ID")
    private String                         groupId;

    @ApiModelProperty("rack")
    private String                         rack;

    @ApiModelProperty("shard")
    private Integer                        shard;

    @ApiModelProperty("shardRouting")
    private Integer                        shardRouting;

    @ApiModelProperty("版本")
    private Integer                        version;

    @ApiModelProperty("角色(1:主；2:从)")
    private Integer                        role;

    @ApiModelProperty("状态(1:常规；-1:索引删除中；-2:删除)")
    private Integer                        status;

    @ApiModelProperty("配置信息")
    private String                         config;

    /**
     * {
     *     "type": {
     *         "dynamic_templates": [
     *             {
     *                 "key1": {}
     *             }
     *         ],
     *         "properties": {
     *             "key3": {
     *                 "type": "key4"
     *             }
     *         }
     *     }
     * }
     */
    @ApiModelProperty("mapping信息")
    private String mappings;
    /**
     *
     * {
     *     "index.number_of_replicas": 0,
     *     "index.translog.durability": "request",
     *     "analysis": {
     *         "analyzer": {
     *             "my_custom_analyzer": {
     *                  ...
     *             }
     *         },
     *         "tokenizer": {
     *         }...
     *     }
     * }
     */
    @ApiModelProperty("索引模板自定义settings")
    private String settings;
    
    @ApiModelProperty("逻辑集群ID")
    private Long                           resourceId;

    /**
     * 写入限流值，
     * writeRateLimit = 0 禁止写入，
     * writeRateLimit = -1 不限流，
     * writeRateLimit = 123 具体的写入tps限流值，即单台client每秒写入123条文档
     */
    @ApiModelProperty("写入限流值")
    private Integer                        writeRateLimit;

    //todo: delete this annoying field
    @ApiModelProperty("物理模板列表")
    private List<IndexTemplatePhyDTO> physicalInfos;

}
