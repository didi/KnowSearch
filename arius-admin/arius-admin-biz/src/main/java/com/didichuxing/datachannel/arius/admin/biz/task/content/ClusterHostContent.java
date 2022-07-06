package com.didichuxing.datachannel.arius.admin.biz.task.content;

import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.ESClusterRoleHost;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author ohushenglin_v
 * @date 2022-05-24
 */
@Data
@NoArgsConstructor
public class ClusterHostContent extends ClusterBaseContent {
    /**
     * 集群角色 对应主机列表
     */
    private List<ESClusterRoleHost> clusterRoleHosts;
}
