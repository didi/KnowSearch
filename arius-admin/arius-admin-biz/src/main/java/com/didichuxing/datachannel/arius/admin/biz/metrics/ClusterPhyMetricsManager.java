package com.didichuxing.datachannel.arius.admin.biz.metrics;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.metrics.MetricsClusterPhyIndicesDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.metrics.MetricsClusterPhyDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.metrics.MetricsClusterPhyNodeDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.metrics.MetricsConfigInfoDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics.cluster.ESClusterOverviewMetricsVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics.linechart.VariousLineChartMetricsVO;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyTypeMetricsEnum;

/**
 * Created by linyunan on 2021-07-30
 *
 *  * 物理集群指标看板业务类
 *  * 1. 查询集群维度指标数据
 *  * 2. 查询集群节点维度指标数据
 *  * 3. 查询集群索引维度指标数据
 */
public interface ClusterPhyMetricsManager {

    /**
     * 获取一级指标类型列表 key:type value:code
     * @param type 类型
     * @see ClusterPhyTypeMetricsEnum
     */
    List<String> getMetricsCode2TypeMap(String type);

    /**
     * 获取物理集群全局维度指标信息
     */
    Result<ESClusterOverviewMetricsVO> getOverviewMetrics(MetricsClusterPhyDTO metricsClusterPhyDTO, Integer appId,
                                                             String domainAccount);
    /**
     * 获取物理集群节点维度指标信息
     */
    Result<List<VariousLineChartMetricsVO>> getClusterPhyNodesMetrics(MetricsClusterPhyNodeDTO param, Integer appId,
                                                                    String domainAccount);

    /**
     * 获取物理集群索引维度指标信息
     */
    Result<List<VariousLineChartMetricsVO>> getClusterPhyIndicesMetrics(MetricsClusterPhyIndicesDTO param,
                                                                        Integer appId, String domainAccount);
    /**
     * 获取物理集群中的索引列表
     */
    Result<List<String>> getClusterPhyIndexName(String clusterPhyName, Integer appId);

    /**
     * 获取账号下已配置指标类型
     */
    List<String> getDomainAccountConfigMetrics(MetricsConfigInfoDTO param, String domainAccount);

    /**
     * 更新账号下已配置的指标类型
     */
    Result<Integer> updateDomainAccountConfigMetrics(MetricsConfigInfoDTO param, String domainAccount);
}
