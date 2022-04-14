package com.didichuxing.datachannel.arius.admin.biz.workorder.content;

import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.ESClusterRoleHost;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ClusterOpHostContent extends ClusterOpBaseContent {
    /**
     * 集群角色 对应主机列表
     */
    private List<ESClusterRoleHost> roleClusterHosts;
}
