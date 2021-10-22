package com.didichuxing.datachannel.arius.admin.common.bean.entity.template;

import java.util.Date;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;

import lombok.Data;

/**
 * @author d06679
 * @date 2019/3/29
 */
@Data
public class IndexTemplateConfig extends BaseEntity {

    /**
     * 主键
     */
    private Long    id;

    /**
     * 逻辑模板id
     */
    private Integer logicId;

    /**
     * 索引存储分离开关 默认关闭
     */
    private Integer isSourceSeparated;

    /**
     * 资源调整tps系数  默认1.0
     */
    private Double  adjustRackTpsFactor;

    /**
     * 资源调整  默认1.0
     */
    private Double  adjustRackShardFactor;

    /**
     * 写入动态限流开关  默认打开
     */
    private Integer dynamicLimitEnable;

    /**
     * mapping优化开关  默认关闭
     */
    private Integer mappingImproveEnable;

    /**
     * 预创建分区索引标识，0：不预先创建；1：预先创建
     */
    private Boolean preCreateFlags;

    /**
     * 禁用报错_source标识，0：不禁用；1：禁用
     */
    private Boolean disableSourceFlags;

    /**
     * 限定逻辑模板下所有物理模板shardNum
     */
    private Integer shardNum;

    /**
     * 创建时间
     */
    private Date    createTime;

    /**
     * 更新时间
     */
    private Date    updateTime;

}
