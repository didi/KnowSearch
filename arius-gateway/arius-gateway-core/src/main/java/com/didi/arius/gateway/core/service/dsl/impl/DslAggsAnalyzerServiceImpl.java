package com.didi.arius.gateway.core.service.dsl.impl;

import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.enums.AggsTypeEnum;
import com.didi.arius.gateway.common.exception.AggsParseException;
import com.didi.arius.gateway.common.metadata.*;
import com.didi.arius.gateway.common.utils.Convert;
import com.didi.arius.gateway.common.utils.Regex;
import com.didi.arius.gateway.core.component.QueryConfig;
import com.didi.arius.gateway.core.service.MetricsService;
import com.didi.arius.gateway.core.service.arius.DslTemplateService;
import com.didi.arius.gateway.core.service.arius.IndexTemplateService;
import com.didi.arius.gateway.core.service.dsl.DslAggsAnalyzerService;
import com.didi.arius.gateway.core.service.dsl.aggregations.AggsType;
import com.didi.arius.gateway.core.service.dsl.aggregations.AggsTypes;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author fitz
 * @date 2021/5/25 7:26 下午
 */
@Service
public class DslAggsAnalyzerServiceImpl implements DslAggsAnalyzerService {
    protected static final Logger logger = LoggerFactory.getLogger(DslAggsAnalyzerServiceImpl.class);
    private static final Logger statLogger = LoggerFactory.getLogger(QueryConsts.STAT_LOGGER);

    @Autowired
    private MetricsService metricsService;

    @Autowired
    private DslTemplateService dslTemplateService;

    @Autowired
    private IndexTemplateService templateService;

    @Autowired
    private QueryConfig queryConfig;

    @Autowired
    private AggsTypes aggsTypes;

    @Override
    public boolean analyze(BaseContext baseContext, BytesReference source, String[] indices, String cluster) {
        try {
            // nothing to parse...
            if (source == null || source.length() == 0) {
                return true;
            }

            String strSource = XContentHelper.convertToJson(source, false);
            JsonParser parser = new JsonParser();
            JsonObject jsonSource = parser.parse(strSource).getAsJsonObject();

            JsonObject aggsObject = getAggsObject(jsonSource);

            if (aggsObject == null) {
                // no aggs field
                return true;
            }

            metricsService.incrQueryAggs(baseContext.getAppid());

            Map<String, FieldInfo> mergedMappings = mergeMappings(indices, cluster);

            if (mergedMappings.size() == 0) {
                // no mapping fields
                return true;
            }

            AggsAnalyzerContext context = new AggsAnalyzerContext();
            AggsBukcetInfo aggsBukcetInfo;
            try {
                aggsBukcetInfo = checkAggs(aggsObject, 0, mergedMappings, context);
            } catch (Throwable e) {
                logger.warn("analyzeAggs unexcept exception", Convert.logExceptionStack(e));
                aggsBukcetInfo = AggsBukcetInfo.createSingleBucket();
            }

            statLogger.info(QueryConsts.DLFLAG_PREFIX + "aggs_detail||requestId={}||aggsLevel={}||aggsBukcetInfo={}", baseContext.getRequestId(), context.getMaxLevel(), aggsBukcetInfo);

            if (aggsBukcetInfo.getMemUsed() > queryConfig.getMaxAggsMemUsed()) {
                throw new AggsParseException(String.format("aggregation buckets memory too large(%d) > %d", aggsBukcetInfo.getMemUsed() , queryConfig.getMaxAggsMemUsed()));
            }

            if (baseContext.getDslTemplateKey() != null) {
                DSLTemplate dslTemplate = dslTemplateService.getDSLTemplate(baseContext.getDslTemplateKey());
                if (dslTemplate != null && dslTemplate.getTotalShardsAvg() > 0) {
                    // 存在历史模板，则根据历史查询情况计算内存开销
                    int bucket = (int) Math.min(aggsBukcetInfo.getBucketNumber(), dslTemplate.getTotalHitsAvg() / dslTemplate.getTotalShardsAvg());
                    if (bucket > queryConfig.getMaxAggsBuckets()) {
                        throw new AggsParseException(String.format("aggregation buckets number too large(%d) > %d", bucket , queryConfig.getMaxAggsBuckets()));
                    }

                    if (bucket * dslTemplate.getTotalShardsAvg() > queryConfig.getMaxAggsBuckets() * QueryConsts.AGGS_DEFAULT_SHARD_NUMBER) {
                        throw new AggsParseException(String.format("aggregation total buckets number too large(%d) > %d", (int)(bucket * dslTemplate.getTotalShardsAvg()) , queryConfig.getMaxAggsBuckets() * QueryConsts.AGGS_DEFAULT_SHARD_NUMBER));
                    }

                    if (aggsBukcetInfo.getMemUsed() * dslTemplate.getTotalShardsAvg() > queryConfig.getMaxAggsMemUsed() * QueryConsts.AGGS_DEFAULT_SHARD_NUMBER) {
                        throw new AggsParseException(String.format("aggregation total buckets memory too large(%d) > %d", (long)(aggsBukcetInfo.getMemUsed() * dslTemplate.getTotalShardsAvg()) , queryConfig.getMaxAggsMemUsed() * QueryConsts.AGGS_DEFAULT_SHARD_NUMBER));
                    }

                    return true;
                }
            }

            //不存在历史模板，则直接计算内存开销
            if (aggsBukcetInfo.getBucketNumber() > queryConfig.getMaxAggsBuckets()) {
                throw new AggsParseException(String.format("aggregation buckets number too large(%d) > %d", aggsBukcetInfo.getBucketNumber() , queryConfig.getMaxAggsBuckets()));
            }

        } catch (IOException e) {
            logger.warn("analyzeAggs convertToJson exception", Convert.logExceptionStack(e));
        } catch (JsonParseException e) {
            logger.warn("analyzeAggs json parse exception", Convert.logExceptionStack(e));
        }

        return true;
    }

    @Override
    public boolean analyzeAggs(BaseContext baseContext, BytesReference source, String[] indices) {
        try {
            if (baseContext.getAppDetail().isAggrAnalyzeEnable()) {
                return analyze(baseContext, source, indices, baseContext.getCluster());
            } else {
                return true;
            }
        } catch (Throwable e) {
            if (e instanceof AggsParseException) {
                logger.warn(QueryConsts.DLFLAG_PREFIX + "anaylizeAggs_forbidden||appid={}||requestId={}||dslTemplateKey={}", baseContext.getAppid(), baseContext.getRequestId(), baseContext.getDslTemplateKey());
            }

            if (false == queryConfig.isCheckForbidden()) {
                throw e;
            } else {
                return true;
            }
        }
    }

    @Override
    public AggsBukcetInfo checkAggs(JsonObject aggsObject, int level, Map<String, FieldInfo> mergedMappings, AggsAnalyzerContext context) {
        AggsBukcetInfo totalAggsBukcetInfo = new AggsBukcetInfo();

        if (level > context.getMaxLevel()) {
            context.setMaxLevel(level);
        }

        for (Map.Entry<String, JsonElement> entry : aggsObject.entrySet()) {
            JsonObject aggsItem = entry.getValue().getAsJsonObject();
            AggsBukcetInfo currentAggsBukcetInfo = checkBucketAggs(aggsItem, level+1, mergedMappings, context);

            if (currentAggsBukcetInfo.isLastBucket()) {
                totalAggsBukcetInfo.setBucketNumber(totalAggsBukcetInfo.getBucketNumber() + currentAggsBukcetInfo.getLastBucketNumber());
            } else {
                totalAggsBukcetInfo.setBucketNumber(totalAggsBukcetInfo.getBucketNumber() + currentAggsBukcetInfo.getBucketNumber());
            }

            totalAggsBukcetInfo.setMemUsed(totalAggsBukcetInfo.getMemUsed() + currentAggsBukcetInfo.getMemUsed());
        }

        return totalAggsBukcetInfo;
    }

    @Override
    public Map<String, FieldInfo> mergeMappings(String[] indices, String cluster) {
        Map<String, FieldInfo> mergedMappings = new HashMap<>();
        Map<String, Map<String, TemplateInfo>> templateAliasesMap    = templateService.getTemplateAliasMap();
        Map<String, Map<String, TemplateInfo>> templateExpressionMap = templateService.getTemplateExpressionMap();

        Map<String, TemplateInfo> aliasMap = templateAliasesMap.get(cluster);
        Map<String, TemplateInfo> expressionMap = templateExpressionMap.get(cluster);
        if (aliasMap == null) {
            logger.warn("getLargeFields alias map is null, cluster={}", cluster);
        } else if (expressionMap == null) {
            logger.warn("getLargeFields expression map is null, cluster={}", cluster);
        } else {
            for (Map.Entry<String, TemplateInfo> entry : aliasMap.entrySet()) {
                String expression = entry.getKey();
                for (String index : indices) {
                    if (Regex.indexContainExp(index, expression)) {
                        TemplateInfo templateInfo = entry.getValue();
                        dealTemplateMappings(mergedMappings, templateInfo);
                        break;
                    }
                }
            }

            for (Map.Entry<String, TemplateInfo> entry : expressionMap.entrySet()) {
                String expression = entry.getKey();
                for (String index : indices) {
                    if (Regex.indexContainExp(index, expression)) {
                        TemplateInfo templateInfo = entry.getValue();
                        dealTemplateMappings(mergedMappings, templateInfo);
                        break;
                    }
                }
            }
        }

        return mergedMappings;
    }



    /************************************************************** private method **************************************************************/
    private JsonObject getAggsObject(JsonObject parent) {
        JsonElement aggs = parent.get("aggs");
        if (aggs == null) {
            aggs = parent.get("aggregations");
        }

        if (aggs == null) {
            return null;
        }

        return aggs.getAsJsonObject();
    }

    private AggsBukcetInfo checkBucketAggs(JsonObject aggsItem, int level, Map<String, FieldInfo> mergedMappings, AggsAnalyzerContext context) {
        if (level > context.getMaxLevel()) {
            context.setMaxLevel(level);
        }

        AggsBukcetInfo currentAggsBukcetInfo = computeAggsType(aggsItem, mergedMappings);

        JsonObject subItem = getAggsObject(aggsItem);
        if (subItem != null) {
            AggsBukcetInfo totalAggsBukcetInfo = new AggsBukcetInfo();

            List<AggsBukcetInfo> subBukcetInfos = checktSubAggs(subItem, level, mergedMappings, context);

            for (AggsBukcetInfo aggsBukcetInfo : subBukcetInfos) {
                if (aggsBukcetInfo.getBucketType() == AggsTypeEnum.BUCKET) {
                    totalAggsBukcetInfo.setLastBucket(false);

                    // 如果是该bucket聚合关键字是最后一个bucket，则用来计算的bucket是lastBucketNumber，否则计算的是bucketNumber
                    if (aggsBukcetInfo.isLastBucket()) {
                        totalAggsBukcetInfo.setBucketNumber(totalAggsBukcetInfo.getBucketNumber() + currentAggsBukcetInfo.getBucketNumber() * aggsBukcetInfo.getLastBucketNumber());
                        totalAggsBukcetInfo.setMemUsed(totalAggsBukcetInfo.getMemUsed() + currentAggsBukcetInfo.getBucketNumber() * aggsBukcetInfo.getMemUsed());
                    } else {
                        totalAggsBukcetInfo.setBucketNumber(totalAggsBukcetInfo.getBucketNumber() + currentAggsBukcetInfo.getBucketNumber() * aggsBukcetInfo.getBucketNumber());
                        totalAggsBukcetInfo.setMemUsed(totalAggsBukcetInfo.getMemUsed() + currentAggsBukcetInfo.getBucketNumber() * aggsBukcetInfo.getMemUsed());
                    }
                } else {
                    totalAggsBukcetInfo.setBucketNumber(totalAggsBukcetInfo.getBucketNumber() + currentAggsBukcetInfo.getBucketNumber());
                    totalAggsBukcetInfo.setLastBucketNumber(totalAggsBukcetInfo.getLastBucketNumber() + currentAggsBukcetInfo.getLastBucketNumber());
                    totalAggsBukcetInfo.setMemUsed(totalAggsBukcetInfo.getMemUsed() + currentAggsBukcetInfo.getBucketNumber() * aggsBukcetInfo.getMemUsed());
                }
            }

            return totalAggsBukcetInfo;
        } else {
            return currentAggsBukcetInfo;
        }
    }

    private AggsBukcetInfo computeAggsType(JsonObject aggsItem, Map<String, FieldInfo> mergedMappings) {
        for (Map.Entry<String, JsonElement> entry : aggsItem.entrySet()) {
            String field = entry.getKey();
            switch (field) {
                case "meta":
                case "aggregations":
                case "aggs":
                    break;
                default:
                    AggsType aggsType = aggsTypes.getAggsType(field);
                    if (aggsType != null) {
                        JsonObject item = entry.getValue().getAsJsonObject();
                        return aggsType.computeAggsType(item, mergedMappings);
                    } else {
                        logger.info("aggs_type_not_support||field={}||content={}", field, aggsItem.toString());
                        return AggsBukcetInfo.createSingleMetrics();
                    }
            }
        }

        return AggsBukcetInfo.createSingleBucket();
    }

    private List<AggsBukcetInfo> checktSubAggs(JsonObject aggsObject, int level, Map<String, FieldInfo> mergedMappings, AggsAnalyzerContext context) {
        List<AggsBukcetInfo> aggsBukcetInfos = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : aggsObject.entrySet()) {
            JsonObject aggsItem = entry.getValue().getAsJsonObject();

            AggsBukcetInfo aggsBukcetInfo = checkBucketAggs(aggsItem, level+1, mergedMappings, context);
            aggsBukcetInfos.add(aggsBukcetInfo);
        }

        return aggsBukcetInfos;
    }

    private void dealTemplateMappings(Map<String, FieldInfo> mergedMappings, TemplateInfo templateInfo) {
        Map<String, FieldInfo> mappings = templateInfo.getMappings();
        for (Map.Entry<String, FieldInfo> inEntry : mappings.entrySet()) {
            if (mergedMappings.containsKey(inEntry.getKey())) {
                FieldInfo oldFieldInfo = mergedMappings.get(inEntry.getKey());
                FieldInfo newFieldInfo = inEntry.getValue();
                mergedMappings.put(inEntry.getKey(), Convert.fieldInfoMerge(oldFieldInfo, newFieldInfo));
            } else {
                mergedMappings.put(inEntry.getKey(), inEntry.getValue());
            }
        }
    }

}
