package com.didichuxing.datachannel.arius.admin.common.bean.po.template;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author d06679
 * @date 2019/3/29
 */
@Data
public class TemplateConfigPO extends BasePO {

    private Long    id;

    private Integer logicId;

    private Integer isSourceSeparated;

    private Double  adjustRackTpsFactor;

    private Double  adjustRackShardFactor;

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
}
