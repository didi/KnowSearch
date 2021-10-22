package com.didichuxing.datachannel.arius.admin.client.bean.vo.template;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;
import com.didichuxing.datachannel.arius.admin.client.constant.template.DataTypeEnum;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 模板基本信息
 *
 * @author wangshu
 * @date 2020/09/24
 */
@Data
public class BaseTemplateVO extends BaseVO {

    @ApiModelProperty("索引ID")
    private Integer id;

    /**
     * 索引模板名称
     */
    @ApiModelProperty("索引名称")
    private String name;

    /**
     * 用户数据类型
     *
     * @see DataTypeEnum
     */
    @ApiModelProperty("数据类型（1:日志；2:用户上报；3:RDS；6：离线导入）")
    private Integer dataType;

    /**
     * 索引滚动格式
     */
    @ApiModelProperty("时间后缀")
    private String dateFormat;

    /**
     * 数据中心
     */
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

    /**
     * 成本部门
     */
    @ApiModelProperty("成本部门ID")
    private String libraDepartmentId;

    /**
     * 成本部门
     */
    @ApiModelProperty("成本部门名称")
    private String libraDepartment;

    /**
     * App ID
     */
    @ApiModelProperty("索引应用ID")
    private Integer appId;

    /**
     * 责任人
     */
    @ApiModelProperty("责任人")
    private String responsible;

    /**
     * 时间字段
     */
    @ApiModelProperty("时间字段")
    private String dateField;

    /**
     * 时间字段的格式
     */
    @ApiModelProperty("时间字段格式")
    private String dateFieldFormat;

    /**
     * id地钻
     */
    @ApiModelProperty("主键字段")
    private String idField;

    /**
     * routing字段
     */
    @ApiModelProperty("routing字段")
    private String routingField;

    /**
     * 表达式
     */
    @ApiModelProperty("表达式")
    private String expression;

    /**
     * 备注
     */
    @ApiModelProperty("描述")
    private String desc;

    /**
     * 规格 单位台
     */
    @ApiModelProperty("配额")
    private Double quota;
}
