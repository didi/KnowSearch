package com.didichuxing.datachannel.arius.admin.common.bean.entity.template;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterLogic;

import lombok.Data;

/**
 * @author d06679
 * @date 2019/3/29
 */
@Data
public class IndexTemplateLogicWithClusterAndMasterTemplate extends IndexTemplateLogic {

    /**
     * Master物理模板
     */
    private IndexTemplatePhy masterTemplate;

    /**
     * 逻辑集群
     */
    private ESClusterLogic logicCluster;

}
