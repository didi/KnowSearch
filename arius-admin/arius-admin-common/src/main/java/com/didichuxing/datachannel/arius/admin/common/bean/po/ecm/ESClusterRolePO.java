package com.didichuxing.datachannel.arius.admin.common.bean.po.ecm;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ES集群对应角色集群
 * @author chengxiang
 * @date 2022/5/9
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ESClusterRolePO extends BasePO {
    private static final long serialVersionUID = 1L;

    private Long              id;

    /**
     * elastic_cluster外键ID
     */
    private Long              elasticClusterId;

    /**
     * role集群名称
     */
    private String            roleClusterName;

    /**
     * 集群角色(master-node/data-node/client-node)
     */
    private String            role;

    /**
     * pod(节点)数量
     */
    private Integer           podNumber;

    /**
     * 节点版本
     */
    private String            esVersion;

    /**
     * 单机实例数
     */
    private Integer           pidCount;

    /**
     * 机器规格
     */
    private String            machineSpec;

    /**
     * 配置包ID
     */
    private Integer           cfgId;

    /**
     * 标记删除
     */
    private Boolean           deleteFlag;
}
