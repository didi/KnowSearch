package com.didichuxing.datachannel.arius.admin.biz.workorder.content;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模板扩缩容
 * @author d06679
 * @date 2019/5/7
 */
@Data
@NoArgsConstructor
public class TemplateIndecreaseContent extends BaseContent {

    private Integer id;

    /**
     * 索引模板名称
     */
    private String  name;

    /**
     * 数据保存时长 单位天
     */
    private Integer expireTime;

    /**
     * 规格 单位台
     */
    private Double  quota;

    /**
     * 热数据保存天数 单位是天
     */
    private Integer hotTime;

    /**
     * 实际的磁盘消耗
     */
    private Double  actualDiskG;

    /**
     * 实际的CPU消耗
     */
    private Double  actualCpuCount;

    /**
     * Quota的磁盘消耗
     */
    private Double  quotaDiskG;

    /**
     * Quota的CPU消耗
     */
    private Double  quotaCpuCount;

    /**************************************** 期望容量信息 ****************************************************/
    /**
     * 期望数据保存时长 单位天
     */
    private Integer expectExpireTime;

    /**
     * 数据总量 单位台
     */
    private Double  expectQuota;

    /**
     * 期望热数据保存天数 单位天
     */
    private Integer expectHotTime;

    /**************************************** 管理员操作 ****************************************************/
    /**
     * 是否跳过容量规划，强制扩容 1:强制 0:不强制
     */
    private Integer force = 0;
}
