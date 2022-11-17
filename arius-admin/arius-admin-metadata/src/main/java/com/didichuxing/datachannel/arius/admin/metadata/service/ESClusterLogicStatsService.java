package com.didichuxing.datachannel.arius.admin.metadata.service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.didichuxing.datachannel.arius.admin.common.bean.po.stats.ClusterLogicStatsPO;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats.AriusStatsNodeInfoESDAO;
import com.didiglobal.knowframework.elasticsearch.client.response.cluster.ESClusterHealthResponse;
import com.didiglobal.knowframework.elasticsearch.client.response.indices.clusterindex.IndexStatusResult;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Service
public class ESClusterLogicStatsService {
    protected static final ILog logger                        = LogFactory
        .getLog(ESClusterLogicStatsService.class);
    private static final String                                 STR_GREEN                     = "green";
    private final Cache<String, ESClusterHealthResponse>        phyClusterHealthCache         = CacheBuilder
        .newBuilder().expireAfterWrite(2, TimeUnit.MINUTES).maximumSize(10000).build();
    private final Cache<String, Map<String, IndexStatusResult>> templateIndicesHealthMapCache = CacheBuilder
        .newBuilder().expireAfterWrite(2, TimeUnit.MINUTES).maximumSize(10000).build();
    @Autowired
    private ESClusterService                                    esClusterService;
    @Autowired
    private IndexTemplatePhyService                             indexTemplatePhyService;
    @Autowired
    private AriusStatsNodeInfoESDAO                             ariusStatsNodeInfoEsDao;
    @Autowired
    private ClusterLogicService                                 clusterLogicService;
    @Autowired
    private ClusterPhyService                                   clusterPhyService;
    @Autowired
    private ClusterRegionService                                clusterRegionService;

    public ClusterLogicStatsPO getLogicClusterStats(Long logicClusterId, boolean isJob) {
        return new ClusterLogicStatsPO();
    }

}