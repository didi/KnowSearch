package com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail;

import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.ESClusterRoleHost;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author fengqiongfeng
 * @date 2020/8/24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClusterOpIndecreaseHostOrderDetail extends BaseClusterHostOrderDetail {

    /**
     * 物理集群id
     */
    private Long                    phyClusterId;
    /**
     * 2:扩容 3：缩容
     */
    private Integer                 operationType;
    /**
     * 集群角色 对应的原始的主机列表
     */
    private List<ESClusterRoleHost> originRoleClusterHosts;
}