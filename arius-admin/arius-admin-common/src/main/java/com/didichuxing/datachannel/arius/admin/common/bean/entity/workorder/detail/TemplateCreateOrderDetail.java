package com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.constant.template.DataType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateCreateOrderDetail extends AbstractOrderDetail {
    /**************************************** 基本信息 ****************************************************/

    /**
     * 索引模板名称
     */
    private String       name;

    /**
     * 数据中心
     */
    private String       dataCenter;

    /**
     * 用户数据类型
     * @see DataType
     */
    private Integer      dataType;

   

  

    /**
     * 备注
     */
    private String       desc;

    /**
     * 数据保存时长 单位天
     */
    private Integer      expireTime;

    /**
     * 热数据保存天数, 单位天
     */
    private Integer      hotTime;

    /**
     * 数据总量 单位G
     */
    private Double       diskQuota;

    /**
     * 周期性滚动  1 滚动   0 不滚动
     */
    private Boolean      cyclicalRoll;

    /**************************************** Schema信息 ****************************************************/

    /**
     * 时间字段
     */
    private String       dateField;
    /**
     * 时间字段格式
     */
    private String       dateFieldFormat;
    /**
     * 索引mapping信息
     */
    private String       mapping;
    /**
     * id字段
     */
    private String       idField;

    /**
     * routing字段
     */
    private String       routingField;

    /**************************************** 部署信息 ****************************************************/

    /**
     * 物理集群名称
     */
    private List<String> clusterPhyNameList;

    /**
     * 逻辑集群名称
     */
    private String       clusterLogicName;

    /**
     * 预创建索引标识
     */
    private Boolean      preCreateFlags;

    /**
     * shard数量
     */
    private Integer      shardNum;

    /**
     * 禁用索引_source标识
     */
    private Boolean      disableSourceFlags;

    /**
     * 禁用rollover标识
     */
    private Boolean      disableIndexRollover;

    /**
     * 服务等级
     */
    private Integer      level;
}