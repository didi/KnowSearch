package com.didichuxing.datachannel.arius.admin.common.bean.po.template;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;

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
public class TemplateConfigPO extends BasePO {

    private Long    id;

    private Integer logicId;

    private Integer isSourceSeparated;

    private Double adjustTpsFactor;

    private Double adjustShardFactor;

    private Integer dynamicLimitEnable;

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
     * indexRollover功能，0：不禁用；1：禁用
     */
    private Boolean disableIndexRollover;
}
