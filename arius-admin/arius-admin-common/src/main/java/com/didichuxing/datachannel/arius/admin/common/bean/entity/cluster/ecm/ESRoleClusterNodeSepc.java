package com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * es集群节点规格套餐信息
 */
@Data
@EqualsAndHashCode(of = { "role", "spec" }, callSuper = false)
public class ESRoleClusterNodeSepc extends BaseEntity {

    /**
     * 节点角色
     */
    private String role;

    /**
     * 节点规格
     */
    private String spec;
}
