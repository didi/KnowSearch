package com.didichuxing.datachannel.arius.admin.common.bean.po.template;

import com.didichuxing.datachannel.arius.admin.client.constant.template.DataTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.DigitResponsible;

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
public class TemplateLogicPO extends BasePO implements DigitResponsible {

    private Integer id;

    /**
     * 索引模板名称
     */
    private String  name;

    /**
     * appid
     */
    private Integer appId;

    /**
     * 用户数据类型
     * @see DataTypeEnum
     */
    private Integer dataType;

    /**
     * 索引滚动格式
     */
    private String  dateFormat;

    /**
     * 数据中心
     */
    private String  dataCenter;

    /**
     * 数据保存时长 单位天
     */
    private Integer expireTime;

    /**
     * 热数据保存时长 单位天
     */
    private Integer hotTime;

    /**
     * 成本部门
     */
    private String  libraDepartmentId;

    /**
     * 成本部门
     */
    private String  libraDepartment;

    /**
     * 责任人，id列表，英文逗号分隔
     */
    private String  responsible;

    /**
     * 时间字段
     */
    private String  dateField;

    /**
     * 时间字段的格式
     */
    private String  dateFieldFormat;

    /**
     * id地钻
     */
    private String  idField;

    /**
     * routing字段
     */
    private String  routingField;

    /**
     * 表达式
     */
    private String  expression;

    /**
     * 备注
     */
    private String  desc;

    /**
     * 规格 单位台
     */
    private Double  quota;

    /**
     * 写入限流值，
     * writeRateLimit = 0 禁止写入，
     * writeRateLimit = -1 不限流，
     * writeRateLimit = 123 具体的写入tps限流值，即单台client每秒写入123条文档
     */
    private Integer writeRateLimit;

    /**
     * 是否禁止读
     */
    private Boolean blockRead;

    /**
     * 是否禁止写
     */
    private Boolean blockWrite;

    /**
     * pipeline
     */
    private String  ingestPipeline;

    /**
     * 逻辑集群id
     */
    private Long    resourceId;

    /**
     * 服务等级
     */
    private Integer level;

    /*
     * dcdr位点差
     */
    private Long    checkPointDiff;

    /**
     * 是否有创建dcdr链路
     */
    private Boolean hasDCDR;
}
