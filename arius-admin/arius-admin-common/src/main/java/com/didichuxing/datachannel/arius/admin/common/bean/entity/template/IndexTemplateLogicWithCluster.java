package com.didichuxing.datachannel.arius.admin.common.bean.entity.template;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterLogic;

import lombok.Data;

/**
 * @author d06679
 * @date 2019/3/29
 */
@Data
public class IndexTemplateLogicWithCluster extends IndexTemplateLogic {
    /**
     * 逻辑集群信息
     * todo：zqr 确认这里的逻辑
     */
    private List<ESClusterLogic> logicClusters;
}
