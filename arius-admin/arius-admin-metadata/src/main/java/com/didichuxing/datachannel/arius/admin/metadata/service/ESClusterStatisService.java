package com.didichuxing.datachannel.arius.admin.metadata.service;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterLogicRackInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterLogicWithRack;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.po.query.AppIdTemplateAccessCountPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.stats.NodeRackStatisPO;
import com.didichuxing.datachannel.arius.admin.common.constant.ClusterHealthStatus;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ESClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didichuxing.datachannel.arius.admin.common.bean.po.stats.AriusClusterStatisPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.stats.ClusterLogicStatisPO;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.TemplateLogicService;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.app.AppIdTemplateAccessESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats.AriusStatsClusterInfoESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats.AriusStatsNodeInfoESDAO;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.cluster.ESClusterHealthResponse;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ESClusterStatisService {

    private final ILog                 LOGGER = LogFactory.getLog(TemplateLabelService.class);

    @Autowired
    private ESClusterService           esClusterService;

    @Autowired
    private ESClusterLogicService      esClusterLogicService;

    @Autowired
    private TemplateLogicService       templateLogicService;

    @Autowired
    private AppIdTemplateAccessESDAO   accessCountEsDao;

    @Autowired
    private AriusStatsNodeInfoESDAO    ariusStatsNodeInfoEsDao;

    @Autowired
    private AriusStatsClusterInfoESDAO ariusStatsClusterInfoEsDao;

    public ClusterLogicStatisPO getPhyClusterStatisticsInfo(String clusterName){
        long endTime   = System.currentTimeMillis();
        long startTime = endTime - 30 * 60 * 1000L;
        AriusClusterStatisPO ariusClusterStatisPO  = ariusStatsClusterInfoEsDao.getMaxClusterStatisByRange(clusterName, startTime, endTime);
        ESClusterHealthResponse clusterHealth      = esClusterService.getClusterHealth(clusterName);

        ClusterLogicStatisPO phyClusterRacksStatics = new ClusterLogicStatisPO();
        phyClusterRacksStatics.setName(clusterName);
        phyClusterRacksStatics.setStatus((null == clusterHealth) ? "red" : clusterHealth.getStatus());

        if(null == ariusClusterStatisPO){
            return phyClusterRacksStatics;
        }

        phyClusterRacksStatics.setDocNu(ariusClusterStatisPO.getTotalDocNu());
        phyClusterRacksStatics.setIndexNu((long)ariusClusterStatisPO.getTotalIndicesNu());
        phyClusterRacksStatics.setDocNu(ariusClusterStatisPO.getTotalDocNu());
        phyClusterRacksStatics.setTotalDisk(ariusClusterStatisPO.getTotalStoreSize());
        phyClusterRacksStatics.setUsedDisk(ariusClusterStatisPO.getStoreSize());
        phyClusterRacksStatics.setFreeDisk(ariusClusterStatisPO.getFreeStoreSize());

        return phyClusterRacksStatics;
    }

    public ClusterLogicStatisPO getLogicClusterStatisticsInfo(Long logicClusterId){
        ESClusterLogicWithRack logicCluster = esClusterLogicService.getLogicClusterWithRackInfoById(logicClusterId);
        if(null == logicCluster){return null;}

        ClusterLogicStatisPO statisPO = new ClusterLogicStatisPO();
        statisPO.setId(logicClusterId);
        statisPO.setName(logicCluster.getName());

        List<ESClusterLogicRackInfo> items = new ArrayList<>(logicCluster.getItems());
        Map<String/*phyClusterName*/, List<String>> phyCluterRackMap     = logicItem2Map(items);
        Map<String, Tuple<ESClusterHealthResponse, List<NodeRackStatisPO>>> phyCluterRackMapInfo = new ConcurrentHashMap<>();

        phyCluterRackMap.keySet().parallelStream().forEach( cluster -> {
            List<String> rack = phyCluterRackMap.get(cluster);
            List<NodeRackStatisPO>  nodeRackStatisPOS     = ariusStatsNodeInfoEsDao.getRackStatis(cluster, rack);
            ESClusterHealthResponse clusterHealthResponse = esClusterService.getClusterHealth(cluster);

            if(!EnvUtil.isOnline()){
                LOGGER.info("class=ClusterLogicService||method=getLogicClusterStatisticsInfo||cluster={}||rack={}||ret={}",
                        cluster, rack, JSON.toJSONString(nodeRackStatisPOS));
            }

            phyCluterRackMapInfo.put(cluster, new Tuple<>(clusterHealthResponse, nodeRackStatisPOS));
        } );

        byte clusterStatus = ClusterHealthStatus.fromString("green").value();

        for(String cluster : phyCluterRackMapInfo.keySet()){
            List<NodeRackStatisPO>   nodeRackStatis         = phyCluterRackMapInfo.get(cluster).v2();
            ESClusterHealthResponse  clusterHealthResponse  = phyCluterRackMapInfo.get(cluster).v1();

            if(null != clusterHealthResponse){
                //如果逻辑集群映射了多个物理集群，那么取状态最大(最坏)的那个状态作为逻辑集群的状态
                byte status   = ClusterHealthStatus.fromString(clusterHealthResponse.getStatus()).value();
                clusterStatus = (status > clusterStatus) ? status : clusterStatus;

                statisPO.setNumberPendingTasks(statisPO.getNumberPendingTasks() + clusterHealthResponse.getNumberOfPendingTasks());
                statisPO.setUnAssignedShards(statisPO.getUnAssignedShards() + clusterHealthResponse.getUnassignedShards());
            }

            statisPO.setNumberDataNodes(logicCluster.getItems().size() * 2);

            if(CollectionUtils.isNotEmpty(nodeRackStatis)){
                for (NodeRackStatisPO nodeStatis : nodeRackStatis){
                    statisPO.setIndexNu(statisPO.getIndexNu() + nodeStatis.getIndexNu());
                    statisPO.setDocNu(statisPO.getDocNu() + nodeStatis.getDocNu());
                    statisPO.setTotalDisk(statisPO.getTotalDisk() + nodeStatis.getTotalDiskG());
                    statisPO.setFreeDisk(statisPO.getFreeDisk() + nodeStatis.getDiskFreeG());
                    statisPO.setUsedDisk(statisPO.getTotalDisk() - statisPO.getFreeDisk());
                }
            }
        }

        String strClusterStatus = "green";
        if(clusterStatus == ClusterHealthStatus.GREEN.value()){
            strClusterStatus = "green";
        }else if(clusterStatus == ClusterHealthStatus.YELLOW.value()){
            strClusterStatus = "yellow";
        }else if(clusterStatus == ClusterHealthStatus.RED.value()){
            strClusterStatus = "red";
        }

        statisPO.setStatus(strClusterStatus);
        statisPO.setStatusType(clusterStatus);
        return statisPO;
    }

    public List<Integer> getLogicClusterAccessInfo(Long logicClusterId, int days){
        List<Integer> appids = new ArrayList<>();

        List<IndexTemplateLogic> templateLogics = templateLogicService.getLogicClusterTemplates(logicClusterId);

        for(IndexTemplateLogic indexTemplate : templateLogics){
            List<AppIdTemplateAccessCountPO> accessCountPos = accessCountEsDao.getAccessAppidsInfoByTemplateId(indexTemplate.getId(), days);
            if(CollectionUtils.isNotEmpty(accessCountPos)){
                accessCountPos.forEach(a -> appids.add(a.getAppId()));
            }
        }

        return appids;
    }

    /*************************************************** private method ***************************************************/
    private Map<String, List<String>> logicItem2Map(List<ESClusterLogicRackInfo> items){
        Map<String, List<String>> phyCluterRackMap = new HashMap<>();
        items.forEach(i -> {
            List<String> racks = phyCluterRackMap.get(i.getPhyClusterName());
            if(CollectionUtils.isEmpty(racks)){
                List<String> rackList = new ArrayList<>();
                rackList.add(i.getRack());
                phyCluterRackMap.put(i.getPhyClusterName(), rackList);
            }else {
                racks.add(i.getRack());
            }
        });

        return phyCluterRackMap;
    }
}
