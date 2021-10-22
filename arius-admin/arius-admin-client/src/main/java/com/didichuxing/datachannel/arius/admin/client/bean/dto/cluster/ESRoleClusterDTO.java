package com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;
import lombok.Data;


/**
 * <p>
 * ES集群表对应各角色主机列表
 * </p>
 *
 * @author didi
 * @since 2020-08-24
 */
@Data
public class ESRoleClusterDTO extends BaseDTO {
    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * elastic_cluster外键ID
     */
    private Long elasticClusterId;

    /**
     * role集群名称
     */
    private String roleClusterName;

    /**
     * 集群角色(masternode/datanode/clientnode)
     */
    private String role;

    /**
     * pod数量
     */
    private Integer podNumber;

    /**
     * 单机实例数
     */
    private Integer pidCount;

    /**
     * 机器规格
     */
    private String machineSpec;

    /**
     * ES版本
     */
    private String esVersion;

    /**
     * 配置包ID
     */
    private Integer cfgId;

    /**
     * 插件包ID列表
     *
     */
    private String plugIds;

    /**
     * 标记删除
     */
    private Boolean deleteFlag;


}

