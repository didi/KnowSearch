package com.didichuxing.datachannel.arius.admin.common.bean.entity.template;

import com.didichuxing.datachannel.arius.admin.client.constant.template.DataTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.StringResponsible;

import lombok.Data;

/**
 * @author d06679
 * @date 2019/3/29
 */
@Data
public class IndexTemplateLogic extends BaseEntity implements StringResponsible {

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
     * 副本保存时长 单位天
     */
    private Integer replicaTime;

    /**
     * 成本部门
     */
    private String  libraDepartmentId;

    /**
     * 成本部门
     */
    private String  libraDepartment;

    /**
     * 责任人
     */
    private String  responsible;

    /**
     * 时间字段
     */
    private String  dateField;

    /**
     * 时间字段的格式
     */
    private String dateFieldFormat;

    /**
     * id字段
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
     * 规格 单位台 每台的资源量就是DOCKER类型的规格的资源；与物理部署的模板无关
     */
    private Double  quota;

    /**
     * pipeline
     */
    private String  ingestPipeline;
}
