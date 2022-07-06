package com.didichuxing.datachannel.arius.admin.common.bean.vo.template;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;
import com.didichuxing.datachannel.arius.admin.common.constant.template.DataTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 包含物理和逻辑信息
 * @author d06679
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
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

    @ApiModelProperty("所属应用ID")
    private Integer projectId;

    /**
     * 用户数据类型
     * @see DataTypeEnum
     */
    @ApiModelProperty("数据类型（0:系统 1:日志；2:上报；3:rds；4:离线）")
    private Integer dataType;

    /**
     * 索引滚动格式
     */
    @ApiModelProperty("分区周期")
    private String  dateFormat;

    @ApiModelProperty("数据中心")
    private String  dataCenter;

    /**
     * 数据保存时长 单位天
     */
    @ApiModelProperty("保存天数")
    private Integer expireTime;

    @ApiModelProperty("成本部门ID")
    private String  libraDepartmentId;

    @ApiModelProperty("成本部门名称")
    private String  libraDepartment;

    @ApiModelProperty("责任人：后续下线无需使用")
    @Deprecated
    private String  responsible;

    @ApiModelProperty("时间字段")
    private String  dateField;

    /**
     * id地钻
     */
    @ApiModelProperty("主键字段")
    private String  idField;

    @ApiModelProperty("routing字段")
    private String  routingField;

    @ApiModelProperty("表达式")
    private String  expression;

    @ApiModelProperty("描述")
    private String  desc;

    /**
     * 规格 单位台
     */
    @ApiModelProperty("配额")
    private Double  quota;

    @ApiModelProperty("创建时间")
    private Date    createTime;

}