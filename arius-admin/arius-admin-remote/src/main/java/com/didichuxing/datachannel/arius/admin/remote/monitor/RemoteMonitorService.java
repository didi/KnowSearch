package com.didichuxing.datachannel.arius.admin.remote.monitor;

import com.didichuxing.datachannel.arius.admin.client.bean.common.OdinData;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.Alert;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.Metric;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.MetricSinkPoint;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.NotifyGroup;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.Silence;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.Strategy;
import com.didichuxing.datachannel.arius.admin.remote.monitor.odin.bean.OdinCluster;
import com.didichuxing.datachannel.arius.admin.remote.monitor.odin.bean.OdinTreeNode;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author zengqiao
 * @date 20/4/30
 */
public interface RemoteMonitorService {
    /**
     * 向Odin指定ns发送数据
     * @param odinDatas 需要发送的数据
     * @return true/false
     */
    boolean sendData(Collection<OdinData> odinDatas);

    /**
     *
     * @param odinService
     * @return
     */
    List<OdinCluster> getOdinChildren(String odinService);

    /**
     * 根据odin服务节点去获取对应的服务部署的机房信息
     * 注意：该接口有缓存，如果需要获取实时机房信息，不能调用该接口
     * @param odinServicePath
     * @return
     */
    Map<String /* machineRoom */, Set<String> /* clusters */> getMachineRoomsByOdin(String odinServicePath, List<String> clusterInfoPOS);

    /**
     * 实时根据odinServicePath去获取该服务下的机房信息
     * @param odinServicePath
     * @return
     */
    Map<String /* machineRoom */, Set<String> /* clusters */> getMachineRoomsRealTimeByOdin(String odinServicePath, List<String> clusterInfoPOS);

    /**
     *
     * @param odinClusters
     * @return
     */
    List<OdinCluster> getOdinCluster(Set<String> odinClusters);

    /**
     * 监控策略的增删改查
     */
    Integer createStrategy(Strategy strategy);

    Boolean deleteStrategyById(Long strategyId);

    Integer modifyStrategy(Strategy strategy);

    List<Strategy> getStrategies();

    Strategy getStrategyById(Long strategyId);

    /**
     * 告警的查
     */
    List<Alert> getAlerts(Long strategyId, Long startTime, Long endTime);

    Alert getAlertById(Long alertId);

    /**
     * 屏蔽的增删改查
     */
    Boolean createSilence(Silence silence);

    Boolean releaseSilence(Long silenceId);

    Boolean modifySilence(Silence silence);

    List<Silence> getSilences(Long strategyId);

    Silence getSilenceById(Long silenceId);

    /**
     * 指标的上报和查询
     */
    Boolean sinkMetrics(List<MetricSinkPoint> pointList);

    Metric getMetrics(String metric, Long startTime, Long endTime, Integer step, Properties tags);

    /**
     * 告警组
     */
    List<NotifyGroup> getNotifyGroups();


    /**
     * 获取用户可选Odin节点
     * @param username 用户名
     * @return OdinTreeNode
     */
    OdinTreeNode getOdinTreeNode(String username);

    /**
     * 创建odin 树节点
     * @param ns 树节点
     * @param category = service
     * @param usn
     * @param level 水平等级
     * @return Result
     */
    Result createTreeNode(String ns, String category, String usn, int level);

    /**
     * 创建odin 树节点
     * @param ns 树节点
     * @param category = service
     * @param usn
     * @param level 水平等级
     * @return Result
     */
    Result deleteTreeNode(String ns, String category,String usn, int level);
}