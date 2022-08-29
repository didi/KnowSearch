package com.didichuxing.datachannel.arius.admin.core.service.es.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.DashBoardMetricThresholdDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.MovingShardMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.ShardMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.shard.Segment;
import com.didichuxing.datachannel.arius.admin.common.bean.po.shard.SegmentPO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.quickcommand.ShardAssignmenNodeVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.quickcommand.ShardAssignmentDescriptionVO;
import com.didichuxing.datachannel.arius.admin.common.util.AriusUnitUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.SizeUtil;
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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant.*;
import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.ESHttpRequestContent.*;
import static com.didichuxing.datachannel.arius.admin.common.util.AriusUnitUtil.SIZE;

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
    private int                 ONE                          = 1;
    
    
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
        long configBigShard = SizeUtil.getUnitSize(ariusConfigInfoService.doubleSetting(ARIUS_COMMON_GROUP,BIG_SHARD_THRESHOLD,BIG_SHARD)+"g");
        return shardsMetrics.stream().filter(s->filterBigShard(configBigShard,s)).sorted(Comparator.comparing(s->SizeUtil.getUnitSize(s.getStore())))
                .collect(Collectors.toList());
    }



    @Override
    public List<ShardMetrics> syncGetSmallShards(String clusterName) {
        List<ShardMetrics> shardsMetrics = getShardMetrics(clusterName);
        long configSmallShard = getConfigSmallShard();
        Map<String, List<ShardMetrics>> indexAndShardMetricsMap =  ConvertUtil.list2MapOfList(
                shardsMetrics, ShardMetrics::getIndex, shardMetrics -> shardMetrics);
        return shardsMetrics.stream().filter(s->filterSmallShard(configSmallShard,s,indexAndShardMetricsMap)).collect(Collectors.toList());
    }

    @Override
    public Tuple</*大shard列表*/List<ShardMetrics>, /*小shard列表*/List<ShardMetrics>> syncGetBigAndSmallShards(String clusterName) {
        long configBigShard = getConfigBigShard();
        long configSmallShard = getConfigSmallShard();
        List<ShardMetrics> shardsMetrics = getShardMetrics(clusterName);
        Tuple<List<ShardMetrics>, List<ShardMetrics>> tuple = new Tuple<>();
        Map<String, List<ShardMetrics>> indexAndShardMetricsMap =  ConvertUtil.list2MapOfList(
                shardsMetrics, ShardMetrics::getIndex, shardMetrics -> shardMetrics);
        tuple.setV1(shardsMetrics.stream().filter(s->filterBigShard(configBigShard,s)).collect(Collectors.toList()));
        tuple.setV2(shardsMetrics.stream().filter(s->filterSmallShard(configSmallShard,s,indexAndShardMetricsMap)).collect(Collectors.toList()));
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

    private boolean filterBigShard( long configBigShard,ShardMetrics shardMetrics) {
        if (null == shardMetrics) { return false;}

        String store = shardMetrics.getStore();
        if (null == store) { return false;}
        return  configBigShard<=SizeUtil.getUnitSize(store);
    }

    private boolean filterSmallShard(long configSmallValue,ShardMetrics shardMetrics,Map<String, List<ShardMetrics>> indexAndShardMetricsMap) {
        if (null == shardMetrics) {
            return false;
        }
        String store = shardMetrics.getStore();
        if (null == store) {
            return false;
        }
        int shardNum = indexAndShardMetricsMap.get(shardMetrics.getIndex()).size();
        return  shardNum>ONE&&SizeUtil.getUnitSize(store)<=configSmallValue;
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

    /**
     * 获取配置的大shard列表
     * @return
     */
    private long getConfigBigShard() {
        return getConfigOrDefaultValue(INDEX_SHARD_BIG_THRESHOLD,DASHBOARD_INDEX_SHARD_BIG_THRESHOLD_DEFAULT_VALUE,SIZE);
    }

    /**
     * 获取配置的小shard列表
     * @return
     */
    private long getConfigSmallShard() {
        return getConfigOrDefaultValue(INDEX_SHARD_SMALL_THRESHOLD,DASHBOARD_INDEX_SHARD_SMALL_THRESHOLD_DEFAULT_VALUE,SIZE);
    }

    /**
     * 获取dashboard配置值
     * catch:获取和转换都发生错误后，使用系统配置的默认配置项
     * @param valueName    配置名称
     * @param defaultValue 默认值
     * @return
     */
    private long getConfigOrDefaultValue(String valueName,String defaultValue,String unitStyle){
        DashBoardMetricThresholdDTO configThreshold = null;
        try {
            String configValue = ariusConfigInfoService.stringSetting(ARIUS_DASHBOARD_THRESHOLD_GROUP, valueName, defaultValue);
            if (StringUtils.isNotBlank(configValue)) {
                configThreshold = JSONObject.parseObject(configValue, DashBoardMetricThresholdDTO.class);
            }
        } catch (Exception e) {
            //获取和转换都发生错误后，使用系统配置的默认配置项
            LOGGER.warn("class=ESShardServiceImpl||method=getConfigOrDefaultValue||name={}||msg=JSON format error!",
                    valueName);
            configThreshold = JSONObject.parseObject(defaultValue, DashBoardMetricThresholdDTO.class);

        }
        return AriusUnitUtil.unitChange(configThreshold.getValue().longValue(),configThreshold.getUnit(),unitStyle);
    }
}