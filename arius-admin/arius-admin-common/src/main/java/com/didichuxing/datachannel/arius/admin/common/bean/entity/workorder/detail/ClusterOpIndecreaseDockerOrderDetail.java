package com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail;

import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.ESClusterRoleDocker;
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
public class ClusterOpIndecreaseDockerOrderDetail extends AbstractOrderDetail {

    /**
     * type 3:docker 4:host
     */
    private Integer                   type;

    /**
     * 物理集群id
     */
    private Long                      phyClusterId;
    /**
     * 物理集群名称
     */
    private String                    phyClusterName;

    /**
     * 2:扩容 3：缩容
     */
    private Integer                   operationType;

    /**
     * 集群角色列表
     */
    private List<ESClusterRoleDocker> roleClusters;
}