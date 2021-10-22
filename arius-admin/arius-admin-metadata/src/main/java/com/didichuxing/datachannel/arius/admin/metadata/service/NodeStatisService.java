package com.didichuxing.datachannel.arius.admin.metadata.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.client.bean.common.RackMetaMetric;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.po.stats.NodeRackStatisPO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats.AriusStatsNodeInfoESDAO;

@Service
public class NodeStatisService {

    @Autowired
    private AriusStatsNodeInfoESDAO ariusStatsNodeInfoEsDao;

    public Result<List<RackMetaMetric>> getRackStatis(String cluster, Collection<String> racks){
        List<String> rackList = new ArrayList<>(racks);
        List<NodeRackStatisPO> nodeRackStatisPOS = ariusStatsNodeInfoEsDao.getRackStatis(cluster, rackList);

        return Result.buildSucc(ConvertUtil.list2List(nodeRackStatisPOS, RackMetaMetric.class));
    }
}
