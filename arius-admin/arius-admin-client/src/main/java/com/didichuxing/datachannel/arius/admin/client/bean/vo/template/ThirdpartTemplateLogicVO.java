package com.didichuxing.datachannel.arius.admin.client.bean.vo.template;

import java.util.Date;

import com.didichuxing.datachannel.arius.admin.client.bean.common.QuotaUsage;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;
import com.didichuxing.datachannel.arius.admin.client.constant.template.DataTypeEnum;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author d06679
 */
@Data
@ApiModel(description = "逻辑模板信息")
public class ThirdpartTemplateLogicVO extends BaseVO {
    /**
     * logic索引id
     */
    @ApiModelProperty("模板ID")
    private Integer    id;

    /**
     * 索引模板名称
     */
    @ApiModelProperty("模板名字")
    private String     name;

    /**
     * appid
     */
    @ApiModelProperty("所属应用ID")
    private Integer    appId;

    /**
     * 用户数据类型
     * @see DataTypeEnum
     */
    @ApiModelProperty("数据类型（1:日志；2:上报；3:rds；6:离线）")
    private Integer    dataType;

    /**
     * 索引滚动格式
     */
    @ApiModelProperty("分区周期")
    private String     dateFormat;

    /**
     * 数据中心
     */
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

    /**
     * 成本部门
     */
    @ApiModelProperty("成本部门ID")
    private String     libraDepartmentId;

    /**
     * 成本部门
     */
    @ApiModelProperty("成本部门名称")
    private String     libraDepartment;

    /**
     * 责任人
     */
    @ApiModelProperty("责任人")
    private String     responsible;

    /**
     * 时间字段
     */
    @ApiModelProperty("时间字段")
    private String     dateField;

    /**
     * id地钻
     */
    @ApiModelProperty("主键字段")
    private String     idField;

    /**
     * routing字段
     */
    @ApiModelProperty("routing字段")
    private String     routingField;

    /**
     * 表达式
     */
    @ApiModelProperty("表达式")
    private String     expression;

    /**
     * 备注
     */
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
