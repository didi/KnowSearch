package com.didichuxing.datachannel.arius.admin.biz.workorder.content;

import com.didichuxing.datachannel.arius.admin.common.constant.template.DataTypeEnum;

/**
 * @author chengxiang
 * @date 2022/5/26
 */
public class NewTemplateCreateContent extends BaseContent {

    /**************************************** 基本信息 ****************************************************/

    /**
     * 索引模板名称
     */
    private String name;

    /**
     * 数据中心
     */
    private String dataCenter;

    /**
     * 用户数据类型
     *
     * @see DataTypeEnum
     */
    private Integer dataType;

    /**
     * 责任人
     */
    private String responsible;

    /**
     * 备注
     */
    private String desc;

    /**
     * 逻辑集群id
     */
    private Long resourceId;

    /**
     * 数据保存时长 单位天
     */
    private Integer expireTime;

    /**
     * 数据总量 单位G
     */
    private Double diskQuota;

    /**
     * 周期性滚动  1 滚动   0 不滚动
     */
    private Boolean cyclicalRoll;

    /**************************************** Schema信息 ****************************************************/

    /**
     * 时间字段
     */
    private String dateField;
    /**
     * 时间字段格式
     */
    private String dateFieldFormat;

    /**
     * mapping信息
     */
    private String mapping;

    /**
     * settings信息
     */
    private String settings;

    /**
     * 模板服务等级
     */
    private Integer level;
}