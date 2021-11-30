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
public class TemplatePhysicalPO extends BasePO {

    private Long    id;

    private Integer logicId;

    private String  name;

    private String  expression;

    private String  cluster;

    private String  rack;

    private Integer shard;

    private Integer shardRouting;

    private Integer version;

    private Integer role;

    private Integer status;

    private String  config;

}
