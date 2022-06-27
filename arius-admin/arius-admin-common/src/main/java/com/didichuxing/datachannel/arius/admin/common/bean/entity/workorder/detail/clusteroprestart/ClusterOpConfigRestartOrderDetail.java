package com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.clusteroprestart;

import com.didichuxing.datachannel.arius.admin.common.constant.esconfig.EsConfigActionEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.esconfig.ESConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author lyn
 * @date 2021-01-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
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