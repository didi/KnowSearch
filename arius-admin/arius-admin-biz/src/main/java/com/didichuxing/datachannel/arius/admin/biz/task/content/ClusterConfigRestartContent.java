package com.didichuxing.datachannel.arius.admin.biz.task.content;

import com.didichuxing.datachannel.arius.admin.common.constant.esconfig.EsConfigActionEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.esconfig.ESConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author lyn
 * @date 2021-01-21
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ClusterConfigRestartContent extends ClusterRestartContent {
    /**
     * Es配置操作: 1.新增 2.编辑 3.删除
     * @see EsConfigActionEnum
     */
    private Integer        actionType;

    /**
     * 新增Es配置
     */
    private List<ESConfig> newEsConfigs;

    /**
     * 原始配置
     */
    private List<ESConfig> originalConfigs;
}