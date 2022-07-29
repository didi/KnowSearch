package com.didichuxing.datachannel.arius.admin.core.service.es;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.setting.ESClusterGetSettingsAllResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterStatsResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterTaskStatsResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterThreadStats;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.quickcommand.PendingTaskAnalysisVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.quickcommand.TaskMissionAnalysisVO;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterConnectionStatus;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterHealthEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didiglobal.logi.elasticsearch.client.response.cluster.ESClusterHealthResponse;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodes.ClusterNodeInfo;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodessetting.ClusterNodeSettings;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author d06679
 * @date 2019/5/8
 */
public interface ESClusterService {

    /**
     * 关闭集群re balance
     * @param cluster 集群
     * @param retryCount 重试次数
     * @return result
     * @throws ESOperateException
     */
    boolean syncCloseReBalance(String cluster, Integer retryCount) throws ESOperateException;

    /**
     * 打开集群re balance
     * @param cluster 集群
     * @param esVersion 版本
     * @return result
     * @throws ESOperateException
     */
    boolean syncOpenReBalance(String cluster, String esVersion) throws ESOperateException;

    /**
     * 配置远端集群
     * @param cluster 集群
     * @param remoteCluster 远端集群
     * @param tcpAddresses tcp地址
     * @param retryCount 重试次数
     * @return true/false
     * @throws ESOperateException
     */
    boolean syncPutRemoteCluster(String cluster, String remoteCluster, List<String> tcpAddresses,
                                 Integer retryCount) throws ESOperateException;

    /**
     * 判断配置否存在
     * @param cluster 集群
     * @param settingFlatName  setting名字
     * @return true/false
     */
    boolean hasSettingExist(String cluster, String settingFlatName);

    /**
     * 获取物理集群下各个节点的插件名称列表
     * @param cluster
     * @return map
     */
    Map<String, List<String>> syncGetNode2PluginsMap(String cluster);

    /**
     * 获取某个集群内索引别名到索引名称的映射
     * @param cluster
     * @return
     */
    Map<String/*alias*/, Set<String>> syncGetAliasMap(String cluster);

    /**
     * 获取 某个es 集群client存活率 0 ~ 100
     *
     * @param cluster
     * @param password
     * @param clientAddresses 地址用逗号分隔: ip:port,ip:port
     * @return
     */
    int syncGetClientAlivePercent(String cluster, String password, String clientAddresses);

    /**
     * 判断es client是否存活
     *
     * @param cluster
     * @param password 认证信息
     * @param clientAddress 单个地址
     * @return
     */
    boolean judgeClientAlive(String cluster, String password, String clientAddress);

    /**
     * 获取集群状态信息
     *
     * @param clusterName
     * @return
     */
    ESClusterHealthResponse syncGetClusterHealth(String clusterName);

    /**
     * 获取集群task信息
     *
     * @param clusterName
     * @return
     */
    List<ESClusterTaskStatsResponse> syncGetClusterTaskStats(String clusterName);

    /**
     * 获取集群健康度
     *
     * @param clusterName
     * @return
     */
    ClusterHealthEnum syncGetClusterHealthEnum(String clusterName);

    /**
     * 获取集群状态信息
     */
    ESClusterStatsResponse syncGetClusterStats(String clusterName);

    /**
     * 获取集群配置
     * @param cluster 集群名称
     * @return response
     */
    ESClusterGetSettingsAllResponse syncGetClusterSetting(String cluster);

    /**
     * 获取集群ip上的segment数目
     * @param clusterName 物理集群名称
     * @return Map<String, Integer> String表示的是实例所在的ip值，Integer表示该ip上的总的segment数目
     */
    Map<String, Integer> synGetSegmentsOfIpByCluster(String clusterName);

    /**
     * 集群的持久化操作
     * @param cluster 集群
     * @param configMap 配置
     * @return true/false
     */
    boolean syncPutPersistentConfig(String cluster, Map<String, Object> configMap);

    /**
     * 获取集群节点中的attributes可配置的信息
     * @param cluster 物理集群名称
     * @return 集群下所有节点的attribute的并集信息列表
     */
    Set<String> syncGetAllNodesAttributes(String cluster);

    /**
     * 获取全量集群节点Setting配置; key ——> 节点uuid ,value ——> ClusterNodeInfo
     */
    Map<String, ClusterNodeInfo> syncGetAllSettingsByCluster(String cluster);

    /**
     * 获取部分集群节点Setting配置; key ——> 节点uuid ,value ——> ClusterNodeSettings
     */
    Map<String, ClusterNodeSettings> syncGetPartOfSettingsByCluster(String cluster);

    /**
     * 获取运行集群的es版本号
     * @param cluster 物理集群名称
     * @return 物理集群es版本号
     */
    String synGetESVersionByCluster(String cluster);

    /**
     * 检测是否为同一个集群
     * @param password
     * @param addresses
     * @return
     */
    Result<Void> checkSameCluster(String password, List<String> addresses);

    /**
     * 获取运行的es版本号
     * @param addresses 地址
     * @param password 集群认证信息
     * @return
     */
    String synGetESVersionByHttpAddress(String addresses, String password);

    /**
     * 检测集群账户信息
     * @param addresses
     * @param password
     * @return True 正确连接集群， False 无法连接集群
     */
    ClusterConnectionStatus checkClusterPassword(String addresses, String password);

    /**
     * 获取集群线程池相关信息
     * @param cluster
     */
    ESClusterThreadStats syncGetThreadStatsByCluster(String cluster);

    @Deprecated
    ESClusterHealthResponse syncGetClusterHealthAtIndicesLevel(String phyClusterName);

    /**
     * pending task分析
     * @param cluster
     * @return
     */
    List<PendingTaskAnalysisVO> syncPendingTaskAnalysis(String cluster);

    /**
     * task任务分析
     * @param cluster
     * @return
     */
    List<TaskMissionAnalysisVO> syncTaskMissionAnalysis(String cluster);

    /**
     * 热点线程分析
     * @param cluster
     * @return
     */
    String syncHotThreadAnalysis(String cluster);

    /**
     * 异常shard分配重试
     * @param cluster
     * @return
     */
    boolean syncAbnormalShardAllocationRetry(String cluster);

    /**
     * 清除fielddata内存
     * @param cluster
     * @return
     */
    boolean syncClearFieldDataMemory(String cluster);
}
