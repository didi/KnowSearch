package com.didichuxing.datachannel.arius.admin.common.bean.vo.template;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;
import com.didichuxing.datachannel.arius.admin.common.constant.template.DataTypeEnum;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模板基本信息
 *
 * @author wangshu
 * @date 2020/09/24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseTemplateVO extends BaseVO {

    @ApiModelProperty("索引ID")
    private Integer id;

    @ApiModelProperty("索引名称")
    private String name;

    /**
     * 用户数据类型
     *
     * @see DataTypeEnum
     */
    @ApiModelProperty("数据类型（0:系统 1:日志；2:用户上报；3:RDS数据；4：离线导入数据）")
    private Integer dataType;

    /**
     * 索引滚动格式
     */
    @ApiModelProperty("时间后缀")
    private String dateFormat;

    @ApiModelProperty("数据中心")
    private String dataCenter;

    /**
     * 数据保存时长 单位天
     */
    @ApiModelProperty("保存天数")
    private Integer expireTime;

    /**
     * 热数据保存时长 单位天
     */
    @ApiModelProperty("热数据保存天数")
    private Integer hotTime;

    @ApiModelProperty("成本部门ID")
    private String libraDepartmentId;

    @ApiModelProperty("成本部门名称")
    private String libraDepartment;

    @ApiModelProperty("索引应用ID")
    private Integer projectId;

    @ApiModelProperty("责任人")
    private String responsible;

    @ApiModelProperty("时间字段")
    private String dateField;

    @ApiModelProperty("时间字段格式")
    private String dateFieldFormat;

    /**
     * id地钻
     */
    @ApiModelProperty("主键字段")
    private String idField;

    @ApiModelProperty("routing字段")
    private String routingField;

    @ApiModelProperty("表达式")
    private String expression;

    @ApiModelProperty("描述")
    private String desc;

    /**
     * 规格 单位台
     */
    @ApiModelProperty("配额")
    private Double quota;

    @ApiModelProperty("写入限流值，" +
            "writeRateLimit = 0 禁止写入；" +
            "writeRateLimit = -1 不限流；" +
            "writeRateLimit = 123 具体的写入tps限流值，即单台client每秒写入123条文档")
    private Integer writeRateLimit;

    /**
     * 是否禁读
     */
    @ApiModelProperty("是否禁读")
    private Boolean blockRead;

    /**
     * 是否禁写
     */
    @ApiModelProperty("是否禁写")
    private Boolean blockWrite;
}