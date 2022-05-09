package com.didichuxing.datachannel.arius.admin.common.bean.vo.template;

import java.util.Date;

import com.didichuxing.datachannel.arius.admin.common.bean.common.QuotaUsage;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;
import com.didichuxing.datachannel.arius.admin.common.constant.template.DataTypeEnum;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "逻辑模板信息")
public class ThirdpartTemplateLogicVO extends BaseVO {

    @ApiModelProperty("模板ID")
    private Integer    id;

    @ApiModelProperty("模板名字")
    private String     name;

    @ApiModelProperty("所属应用ID")
    private Integer    appId;

    /**
     * 用户数据类型
     * @see DataTypeEnum
     */
    @ApiModelProperty("数据类型（0:系统 1:日志；2:上报；3:rds；6:离线）")
    private Integer    dataType;

    /**
     * 索引滚动格式
     */
    @ApiModelProperty("分区周期")
    private String     dateFormat;

    @ApiModelProperty("数据中心")
    private String     dataCenter;

    /**
     * 数据保存时长 单位天
     */
    @ApiModelProperty("保存天数")
    private Integer    expireTime;

    /**
     * 热数据保存时长 单位天
     */
    @ApiModelProperty("热数据保存天数")
    private Integer    hotTime;

    @ApiModelProperty("成本部门ID")
    private String     libraDepartmentId;

    @ApiModelProperty("成本部门名称")
    private String     libraDepartment;

    @ApiModelProperty("责任人")
    private String     responsible;

    @ApiModelProperty("时间字段")
    private String     dateField;

    /**
     * id地钻
     */
    @ApiModelProperty("主键字段")
    private String     idField;

    @ApiModelProperty("routing字段")
    private String     routingField;

    @ApiModelProperty("表达式")
    private String     expression;

    @ApiModelProperty("描述")
    private String     desc;

    /**
     * 规格 单位台
     */
    @ApiModelProperty("配额")
    private Double     quota;

    /**
     * quota磁盘利用率
     */
    @ApiModelProperty("配额使用情况")
    private QuotaUsage quotaUsage;

    /**
     * 创建时间
     */
    private Date       createTime;
}
