package com.didichuxing.datachannel.arius.admin.core.service.es.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.MovingShardMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.ShardMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.shard.Segment;
import com.didichuxing.datachannel.arius.admin.common.bean.po.shard.SegmentPO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.quickcommand.ShardAssignmenNodeVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.quickcommand.ShardAssignmentDescriptionVO;
import com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESShardService;
import com.didichuxing.datachannel.arius.admin.persistence.es.cluster.ESShardDAO;
import com.didiglobal.logi.elasticsearch.client.gateway.direct.DirectResponse;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.rest.RestStatus;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterPhyMetricsConstant.BIG_SHARD;
import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.ESHttpRequestContent.*;

/**
 * Created by linyunan on 3/22/22
 */
@Service
public class ESShardServiceImpl implements ESShardService {

    private static final ILog LOGGER = LogFactory.getLog(ESShardServiceImpl.class);
    
    private String              shard                        = "shard";
    private String              index                        = "index";
    private String              primary                      = "primary";
    private String              current_state                = "current_state";
    private String              node_allocation_decisions    = "node_allocation_decisions";
    private String              node_name                    = "node_name";
    private String              decider                      = "decider";
    private String              deciders                     = "deciders";
    private String              explanation                  = "explanation";
    
    
    @Autowired
    private ESShardDAO esShardDAO;

    @Autowired
    private AriusConfigInfoService ariusConfigInfoService;

    @Override
    public List<MovingShardMetrics> syncGetMovingShards(String clusterName) {
        DirectResponse directResponse = esShardDAO.getDirectResponse(clusterName, "Get", GET_MOVING_SHARD);

        List<MovingShardMetrics> movingShardsMetrics = Lists.newArrayList();
        if (directResponse.getRestStatus() == RestStatus.OK
                && StringUtils.isNoneBlank(directResponse.getResponseContent())) {

            movingShardsMetrics = ConvertUtil.str2ObjArrayByJson(directResponse.getResponseContent(),
                    MovingShardMetrics.class);

        }
        return movingShardsMetrics;
    }

    @Override
    public List<ShardMetrics> syncGetBigShards(String clusterName) {
        List<ShardMetrics> shardsMetrics = getShardMetrics(clusterName);
        return shardsMetrics.stream().filter(this::filterBigShard).collect(Collectors.toList());
    }

    @Override
    public List<ShardMetrics> syncGetSmallShards(String clusterName) {
        List<ShardMetrics> shardsMetrics = getShardMetrics(clusterName);
        return shardsMetrics.stream().filter(this::filterSmallShard).collect(Collectors.toList());
    }

    @Override
    public Tuple</*大shard列表*/List<ShardMetrics>, /*小shard列表*/List<ShardMetrics>> syncGetBigAndSmallShards(String clusterName) {
        List<ShardMetrics> shardsMetrics = getShardMetrics(clusterName);
        Tuple<List<ShardMetrics>, List<ShardMetrics>> tuple = new Tuple<>();
        tuple.setV1(shardsMetrics.stream().filter(this::filterBigShard).collect(Collectors.toList()));
        tuple.setV2(shardsMetrics.stream().filter(this::filterSmallShard).collect(Collectors.toList()));
        return tuple;
    }

    @Override
    public List<Segment> syncGetSegments(String clusterName) {
        String segmentsPartInfoRequestContent = getSegmentsPartInfoRequestContent();
        List<SegmentPO> segmentPOS = esShardDAO.commonGet(clusterName, segmentsPartInfoRequestContent, SegmentPO.class);
        return ConvertUtil.list2List(segmentPOS, Segment.class);
    }

    @Override
    public List<Segment> syncGetSegmentsCountInfo(String clusterName) {
        String segmentsCountContent = getSegmentsCountContent();
        List<SegmentPO> segmentPOS = esShardDAO.commonGet(clusterName, segmentsCountContent, SegmentPO.class);
        return ConvertUtil.list2List(segmentPOS, Segment.class);
    }


    @Override
    public ShardAssignmentDescriptionVO syncShardAssignmentDescription(String cluster) {
        String response = esShardDAO.shardAssignment(cluster);
        if (null!=response){
            return buildShardAssignment(JSONObject.parseObject(response));
        }else {
            return null;
        }
    }


    /*********************************************private******************************************/


    @NotNull
    private List<ShardMetrics> getShardMetrics(String clusterName) {
        String shardsRequestContent = getShardsAllInfoRequestContent("20s");
        return esShardDAO.commonGet(clusterName, shardsRequestContent, ShardMetrics.class);
    }

    private boolean filterBigShard(ShardMetrics shardMetrics) {
        if (null == shardMetrics) { return false;}

        String store = shardMetrics.getStore();
        if (null == store) { return false;}
        double value = Double.valueOf(store.substring(0, store.length() - 2));

        double bigShardThreshold = ariusConfigInfoService.doubleSetting(AriusConfigConstant.ARIUS_COMMON_GROUP,
                AriusConfigConstant.BIG_SHARD_THRESHOLD, BIG_SHARD);

        if (store.endsWith("tb")) {
            value *= 1024;
            return bigShardThreshold <= value;
        }else if (store.endsWith("gb")){
            return bigShardThreshold <= value;
        }else {
            return false;
        }
    }

    private boolean filterSmallShard(ShardMetrics shardMetrics) {
        if (null == shardMetrics) { return false;}
        String store = shardMetrics.getStore();
        if (null == store) { return false;}
        return !store.endsWith("tb") && !store.endsWith("gb");
    }

    private ShardAssignmentDescriptionVO buildShardAssignment(JSONObject responseJson) {
        ShardAssignmentDescriptionVO descriptionVO = new ShardAssignmentDescriptionVO();
        descriptionVO.setShard((Integer) responseJson.get(shard));
        descriptionVO.setIndex((String) responseJson.get(index));
        descriptionVO.setPrimary((Boolean) responseJson.get(primary));
        descriptionVO.setCurrentState((String) responseJson.get(current_state));
        JSONArray decisionsArray = responseJson.getJSONArray(node_allocation_decisions);
        List<ShardAssignmenNodeVO> decisions = new ArrayList<>();
        for (int i = 0; i < decisionsArray.size(); i++) {
            ShardAssignmenNodeVO decisionMap = new ShardAssignmenNodeVO();
            JSONObject decisionObject = decisionsArray.getJSONObject(i);
            decisionMap.setNodeName((String) decisionObject.get(node_name));
            JSONArray decidersJson = decisionObject.getJSONArray(deciders);
            JSONObject deciderJson = (JSONObject) decidersJson.get(0);
            decisionMap.setNodeDecide((String) deciderJson.get(decider));
            decisionMap.setExplanation((String) deciderJson.get(explanation));
            decisions.add(decisionMap);
        }
        descriptionVO.setDecisions(decisions);
        return descriptionVO;
    }

    private String buildSortType(Boolean orderByDesc) {
        String sortType = "desc";
        if (orderByDesc == null) {
            return sortType;
        }

        if (orderByDesc) {
            return sortType;
        }

        return "asc";
    }
}