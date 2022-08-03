package com.didichuxing.datachannel.arius.admin.common.bean.dto.template;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.PageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
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
@ApiModel(description = "逻辑模板信息")
public class IndexTemplateDTO extends PageDTO {

    @ApiModelProperty("模板ID")
    private Integer                   id;

    @ApiModelProperty("模板名字")
    private String                    name;

    @ApiModelProperty("索引应用ID")
    private Integer                   projectId;

    @ApiModelProperty("数据类型（0:系统 1:日志；2:上报；3:rds数据；4:离线导入数据")
    private Integer                   dataType;

    @ApiModelProperty("时间后缀,索引滚动格式")
    private String                    dateFormat;

    @ApiModelProperty("数据中心")
    private String                    dataCenter;

    @ApiModelProperty("保存天数, 单位天")
    private Integer                   expireTime;

    @ApiModelProperty("热数据保存天数, 单位天")
    private Integer                   hotTime;

   

    @ApiModelProperty("时间分区字段")
    private String                    dateField;

    @ApiModelProperty("时间字段格式")
    private String                    dateFieldFormat;

    @Deprecated
    @ApiModelProperty("主键字段：后续下线，无需使用")
    private String                    idField;

    @Deprecated
    @ApiModelProperty("routing字段：后续下线，无需使用")
    private String                    routingField;

    @ApiModelProperty("表达式")
    private String                    expression;

    @ApiModelProperty("描述")
    private String                    desc;

    @ApiModelProperty("配额")
    private Double                    quota;

    @ApiModelProperty("ingestPipeline")
    private String                    ingestPipeline;

    @ApiModelProperty("preCreateFlags")
    private Boolean                   preCreateFlags;

    @ApiModelProperty("shardNum")
    private Integer                   shardNum;

    @ApiModelProperty("disableSourceFlags，禁用索引_source标识")
    private Boolean                   disableSourceFlags;

    @ApiModelProperty("disableIndexRollover，禁用indexRollover")
    private Boolean                   disableIndexRollover;

    /**
     * 写入限流值，
     * writeRateLimit = 0 禁止写入，
     * writeRateLimit = -1 不限流，
     * writeRateLimit = 123 具体的写入tps限流值，即单台client每秒写入123条文档
     */
    @ApiModelProperty("writeRateLimit")
    private Integer                   writeRateLimit;

    /**
     * 是否禁读
     */
    @ApiModelProperty("是否禁读")
    private Boolean                   blockRead;

    /**
     * 是否禁写
     */
    @ApiModelProperty("是否禁写")
    private Boolean                   blockWrite;

    @ApiModelProperty("逻辑集群id")
    private Long                      resourceId;

    @ApiModelProperty("是否禁写")
    private Long                      checkPointDiff;

    @ApiModelProperty("是否已创建dcdr")
    private Boolean                   hasDCDR;

    @ApiModelProperty("服务等级")
    private Integer                   level;

    @ApiModelProperty("物理模板信息")
    private List<IndexTemplatePhyDTO> physicalInfos;

    @ApiModelProperty("开启服务")
    private String                    openSrv;

    @ApiModelProperty("可用磁盘容量")
    private Double                    diskSize;
}