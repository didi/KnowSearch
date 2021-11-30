package com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = { "role", "spec" }, callSuper = false)
public class RoleClusterNodeSepc extends BaseEntity {

    /**
     * 节点角色
     */
    private String role;

    /**
     * 节点规格
     */
    private String spec;
}
