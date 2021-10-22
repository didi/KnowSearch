package com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.clusterOpRestart;

import com.didichuxing.datachannel.arius.admin.client.constant.esconfig.EsConfigActionEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.esconfig.ESConfig;
import lombok.Data;

import java.util.List;

/**
 * @author lyn
 * @date 2021-01-21
 */
@Data
public class ClusterOpConfigRestartOrderDetail extends ClusterOpRestartOrderDetail {
    /**
     * Es配置操作: 1.新增 2.编辑 3.删除
     * @see EsConfigActionEnum
     */
    private Integer                 actionType;

    /**
     * 新增Es配置
     */
    private List<ESConfig>          newEsConfigs;

    /**
     * 原始Es配置
     */
    private List<ESConfig>          originalConfigs;
}
