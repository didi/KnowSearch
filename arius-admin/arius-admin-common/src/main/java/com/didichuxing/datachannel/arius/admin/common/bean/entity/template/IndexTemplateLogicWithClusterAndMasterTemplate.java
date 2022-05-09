package com.didichuxing.datachannel.arius.admin.common.bean.entity.template;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;

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
public class IndexTemplateLogicWithClusterAndMasterTemplate extends IndexTemplateInfo {

    /**
     * Master物理模板
     */
    private IndexTemplatePhyInfo masterTemplate;

    /**
     * 逻辑集群
     */
    private ClusterLogic logicCluster;

}
