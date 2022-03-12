package com.didichuxing.datachannel.arius.admin.client.bean.vo.template;

import java.util.Date;
import java.util.List;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Label;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.app.AppTemplateAuthVO;
import com.didichuxing.datachannel.arius.admin.client.constant.template.DataTypeEnum;

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
@ApiModel(description = "模板信息（详细）")
public class IndexTemplateLogicAllVO extends BaseVO {

    @ApiModelProperty("模板ID")
    private Integer                       id;

    @ApiModelProperty("模板名字")
    private String                        name;

    @ApiModelProperty("索引所属应用ID")
    private Integer                       appId;

    @ApiModelProperty("索引所属应用名称")
    private String                       appName;

    /**
     * 用户数据类型
     * @see DataTypeEnum
     */
    @ApiModelProperty("数据类型（0:系统 1:日志；2:上报；3:rds；6:离线）")
    private Integer                       dataType;

    @ApiModelProperty("数据类型")
    private String                        dataTypeStr;

    /**
     * 索引滚动格式
     */
    @ApiModelProperty("时间后缀")
    private String                        dateFormat;

    /**
     * 数据中心
     */
    @ApiModelProperty("数据中心")
    private String                        dataCenter;

    /**
     * 数据保存时长 单位天
     */
    @ApiModelProperty("保存天数")
    private Integer                       expireTime;

    /**
     * 热数据保存时长 单位天
     */
    @ApiModelProperty("热数据保存天数")
    private Integer                       hotTime;

    @ApiModelProperty("成本部门ID")
    private String                        libraDepartmentId;

    @ApiModelProperty("成本部门名称")
    private String                        libraDepartment;

    @ApiModelProperty("责任人")
    private String                        responsible;

    @ApiModelProperty("时间字段")
    private String                        dateField;

    @ApiModelProperty("时间字段格式")
    private String                        dateFieldFormat;

    /**
     * id地钻
     */
    @ApiModelProperty("主键字段")
    private String                        idField;

    @ApiModelProperty("routing字段")
    private String                        routingField;

    @ApiModelProperty("表达式")
    private String                        expression;

    @ApiModelProperty("描述")
    private String                        desc;

    /**
     * 规格 单位台
     */
    @ApiModelProperty("配额")
    private Double                        quota;

    @ApiModelProperty("写入限流值，" +
            "writeRateLimit = 0 禁止写入；" +
            "writeRateLimit = -1 不限流；" +
            "writeRateLimit = 123 具体的写入tps限流值，即单台client每秒写入123条文档")
    private Integer writeRateLimit;

    @ApiModelProperty("创建时间")
    private Date                          createTime;

    @ApiModelProperty("修改时间")
    private Date                          updateTime;

    @ApiModelProperty("物理模板信息")
    private List<IndexTemplatePhysicalVO> physicalVOS;

    /**
     * 访问app列表
     */
    @ApiModelProperty("权限信息")
    private List<AppTemplateAuthVO>       templateAuthVOS;

    @ApiModelProperty("标签信息")
    private List<Label>                   labels;

}
