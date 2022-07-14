package com.didichuxing.datachannel.arius.admin.biz.metrics;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsClusterPhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsConfigInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MultiMetricsClusterPhyNodeDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.other.cluster.ESClusterTaskDetailVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.top.VariousLineChartMetricsVO;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyTypeMetricsEnum;

/**
 * @author Created by linyunan on 2021-07-30
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
     * @return {@code List<String>}
     */
    List<String> getMetricsCode2TypeMap(String type);

    /**
     * 获取指定类型的指标
     * @param userName 账号
     * @param projectId projectId
     * @param param 物理集群指标
     * @param metricsTypeEnum 指标处理器类型
     * @return result
     */
    <T> Result<T> getClusterMetricsByMetricsType(MetricsClusterPhyDTO param, Integer projectId, String userName,
                                                 ClusterPhyTypeMetricsEnum metricsTypeEnum);

    /**
     * 获取物理集群多个节点的指标信息
     * @param param 物理集群指标
     * @param projectId projectId
     * @param userName 账号
     * @param metricsTypeEnum 指标处理器类型
     * @return result
     */
    Result<List<VariousLineChartMetricsVO>> getMultiClusterMetrics(MultiMetricsClusterPhyNodeDTO param,
                                                                   Integer projectId, String userName,
                                                                   ClusterPhyTypeMetricsEnum metricsTypeEnum);

    /**
     * 获取用户配置指标
     *
     @param param 入参
     @param userName 用户名
     
     @return {@code List<String>}
     */
    List<String> getUserNameConfigMetrics(MetricsConfigInfoDTO param, String userName);

    /**
     * 更新账号下已配置的指标类型
     @param param 入参
     @param userName 用户名
     @return {@code Result<Integer>}
     */
    Result<Integer> updateUserNameConfigMetrics(MetricsConfigInfoDTO param, String userName);

    /**
     * 获取物理集群中的索引列表
     @param clusterPhyName 集群phy名称
     @param node 节点
     @param startTime 开始时间
     @param endTime 结束时间
     @param projectId 应用程序id
     @return {@code Result<List<ESClusterTaskDetailVO>>}
     */
    Result<List<ESClusterTaskDetailVO>> getClusterPhyTaskDetail(String clusterPhyName, String node, String startTime,
                                                                String endTime, Integer projectId);

}