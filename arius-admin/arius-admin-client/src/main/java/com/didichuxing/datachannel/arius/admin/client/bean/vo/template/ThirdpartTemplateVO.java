package com.didichuxing.datachannel.arius.admin.client.bean.vo.template;

import java.util.Date;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;
import com.didichuxing.datachannel.arius.admin.client.constant.template.DataTypeEnum;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 包含物理和逻辑信息
 * @author d06679
 */
@Data
@ApiModel(description = "模板信息")
public class ThirdpartTemplateVO extends BaseVO {

    /******************************* 物理信息 ********************************/

    @ApiModelProperty("模板ID")
    private Long    id;

    @ApiModelProperty("逻辑模板ID")
    private Integer logicId;

    @ApiModelProperty("模板名字")
    private String  name;

    @ApiModelProperty("所属物理集群")
    private String  cluster;

    @ApiModelProperty("rack")
    private String  rack;

    @ApiModelProperty("shard")
    private Integer shard;

    @ApiModelProperty("版本")
    private Integer version;

    @ApiModelProperty("角色(1:主；2:从)")
    private Integer role;

    @ApiModelProperty("状态(1:常规；-1:索引删除中；-2:删除)")
    private Integer status;

    @ApiModelProperty("配置")
    private String  config;

    /******************************* 逻辑信息 ********************************/

    /**
     * appid
     */
    @ApiModelProperty("所属应用ID")
    private Integer appId;

    /**
     * 用户数据类型
     * @see DataTypeEnum
     */
    @ApiModelProperty("数据类型（1:日志；2:上报；3:rds；6:离线）")
    private Integer dataType;

    /**
     * 索引滚动格式
     */
    @ApiModelProperty("分区周期")
    private String  dateFormat;

    /**
     * 数据中心
     */
    @ApiModelProperty("数据中心")
    private String  dataCenter;

    /**
     * 数据保存时长 单位天
     */
    @ApiModelProperty("保存天数")
    private Integer expireTime;

    /**
     * 成本部门
     */
    @ApiModelProperty("成本部门ID")
    private String  libraDepartmentId;

    /**
     * 成本部门
     */
    @ApiModelProperty("成本部门名称")
    private String  libraDepartment;

    /**
     * 责任人
     */
    @ApiModelProperty("责任人")
    private String  responsible;

    /**
     * 时间字段
     */
    @ApiModelProperty("时间字段")
    private String  dateField;

    /**
     * id地钻
     */
    @ApiModelProperty("主键字段")
    private String  idField;

    /**
     * routing字段
     */
    @ApiModelProperty("routing字段")
    private String  routingField;

    /**
     * 表达式
     */
    @ApiModelProperty("表达式")
    private String  expression;

    /**
     * 备注
     */
    @ApiModelProperty("描述")
    private String  desc;

    /**
     * 规格 单位台
     */
    @ApiModelProperty("配额")
    private Double  quota;

    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    private Date    createTime;

}
