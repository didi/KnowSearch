package com.didichuxing.datachannel.arius.admin.client.bean.dto.template;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.PageDTO;

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
@ApiModel(description = "逻辑模板信息")
public class IndexTemplateLogicDTO extends PageDTO {

    @ApiModelProperty("模板ID")
    private Integer                        id;

    @ApiModelProperty("模板名字")
    private String                         name;

    @ApiModelProperty("索引应用ID")
    private Integer                        appId;

    @ApiModelProperty("数据类型（0:系统 1:日志；2:上报；3:rds数据；4:离线导入数据")
    private Integer                        dataType;

    @ApiModelProperty("时间后缀,索引滚动格式")
    private String                         dateFormat;

    @ApiModelProperty("数据中心")
    private String                         dataCenter;

    @ApiModelProperty("保存天数, 单位天")
    private Integer                        expireTime;

    @ApiModelProperty("热数据保存天数, 单位天")
    private Integer                        hotTime;

    @ApiModelProperty("成本部门ID")
    private String                         libraDepartmentId;

    @ApiModelProperty("成本部门名称")
    private String                         libraDepartment;

    @ApiModelProperty("责任人")
    private String                         responsible;

    @ApiModelProperty("时间分区字段")
    private String                         dateField;

    @ApiModelProperty("时间字段格式")
    private String                         dateFieldFormat;

    @ApiModelProperty("主键字段")
    private String                         idField;

    @ApiModelProperty("routing字段")
    private String                         routingField;

    @ApiModelProperty("表达式")
    private String                         expression;

    @ApiModelProperty("描述")
    private String                         desc;

    @ApiModelProperty("配额")
    private Double                         quota;

    @ApiModelProperty("ingestPipeline")
    private String                         ingestPipeline;

    @ApiModelProperty("preCreateFlags")
    private Boolean                        preCreateFlags;

    @ApiModelProperty("shardNum")
    private Integer                        shardNum;

    @ApiModelProperty("disableSourceFlags，禁用索引_source标识")
    private Boolean                        disableSourceFlags;

    /**
     * 写入限流值，
     * writeRateLimit = 0 禁止写入，
     * writeRateLimit = -1 不限流，
     * writeRateLimit = 123 具体的写入tps限流值，即单台client每秒写入123条文档
     */
    @ApiModelProperty("writeRateLimit")
    private Integer                         writeRateLimit;

    @ApiModelProperty("物理模板信息")
    private List<IndexTemplatePhysicalDTO> physicalInfos;
}
