package com.didi.arius.gateway.core.service.dsl.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.metadata.*;
import com.didi.arius.gateway.common.utils.Convert;
import com.didi.arius.gateway.common.metadata.BaseContext;
import com.didi.arius.gateway.core.service.dsl.transform.RequestVisitorV2;
import com.didi.arius.gateway.core.service.dsl.transform.RequestVisitorV6;
import com.didi.arius.gateway.core.service.dsl.transform.RequestVisitorV7;
import com.didi.arius.gateway.core.service.dsl.DslRewriterService;
import com.didi.arius.gateway.core.service.arius.IndexTemplateService;
import com.didiglobal.knowframework.dsl.parse.dsl.ast.DslNode;
import com.didiglobal.knowframework.dsl.parse.dsl.parser.DslParser;
import com.didiglobal.knowframework.dsl.parse.dsl.visitor.basic.OutputVisitor;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import lombok.NoArgsConstructor;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * @author fitz
 * @date 2021/5/25 7:46 下午
 */
@Service
@NoArgsConstructor
public class DslRewriterServiceImpl implements DslRewriterService {
    private static final ILog logger = LogFactory.getLog(DslRewriterServiceImpl.class);
    private static final ILog statLogger = LogFactory.getLog(QueryConsts.STAT_LOGGER);
    public static final String QUERY = "query";
    public static final String POST_FILTER = "post_filter";
    public static final String AGG = "aggregations";
    public static final String ORDER = "order";
    public static final String TERM = "_term";

    private static final String TYPE_SPLIT = "#";

    @Autowired
    private IndexTemplateService indexTemplateService;

    @Override
    public BytesReference rewriteRequest(BaseContext context, String esVersion, JSONObject source) throws Exception {
        if (statLogger.isTraceEnabled()) {
            statLogger.trace("_arius_aggs_dSLRewriter||requestId={}||esVersion={}||userDsl={}", context.getRequestId(), esVersion, source.toJSONString());
        }

        dealFirstLevel(source);

        DslNode node = DslParser.parse(source);

        OutputVisitor transformVisitor;
        if (esVersion.startsWith(QueryConsts.ES_VERSION_2_PREFIX)) {
            transformVisitor = new RequestVisitorV2();
        } else if (esVersion.startsWith(QueryConsts.ES_VERSION_6_PREFIX)) {
            transformVisitor = new RequestVisitorV6();
        } else {
            transformVisitor = new RequestVisitorV7();
        }

        // dsl转换
        node.accept(transformVisitor);

        // typed_key处理
        if (esVersion.startsWith(QueryConsts.ES_VERSION_2_PREFIX)) {
            doTypedKey(context, (JSONObject) transformVisitor.ret);
        }

        String formatDSL = transformVisitor.ret.toString();

        if (statLogger.isTraceEnabled()) {
            statLogger.trace("_arius_aggs_dSLRewriter||requestId={}||esVersion={}||reWriterDsl={}", context.getRequestId(), esVersion, formatDSL);
        }

        return new BytesArray(formatDSL);
    }

    @Override
    public void doTypedKey(BaseContext context, JSONObject source) {
        if (!context.isTypedKeys()) {
            return ;
        }

        JSONObject aggs = getAggsObject(source);
        if (aggs == null) {
            return ;
        }

        IndexTemplate indexTemplate = context.getIndexTemplate();
        Map<String, FieldInfo> fieldInfoMap = new HashMap<>();
        if (indexTemplate != null) {
            try {
                Map<String, Map<String, TemplateInfo>> templateExpressionMap = indexTemplateService.getTemplateExpressionMap();
                Map<String, TemplateInfo> expressionMap = templateExpressionMap.get(indexTemplate.getMasterInfo().getCluster());
                TemplateInfo templateInfo = Objects.requireNonNull(expressionMap).get(indexTemplate.getExpression());
                fieldInfoMap = Objects.requireNonNull(templateInfo).getMappings();
            } catch (Exception e) {
                logger.warn("get mappings exception", e);
            }
        }


        AggsPath root = new AggsPath();
        buildAggsPath(root, aggs, fieldInfoMap);
        JSONObject newAggs = buildTypedKey(root, aggs);
        putAggsObject(source, newAggs);
    }

    @Override
    public BytesReference rewriteRequest(BaseContext context, String esVersion, BytesReference source) {
        JSONObject jsonObject;
        try {
            if (source == null || source.length() <= 0) {
                return source;
            }

            String strSource = XContentHelper.convertToJson(source, false);

            jsonObject = JSON.parseObject(strSource);

            return rewriteRequest(context, esVersion, jsonObject);
        } catch (Exception e) {
            logger.error("unexcept_error||type=rewriteRequest||e={}", Convert.logExceptionStack(e));
            return source;
        }
    }

    /************************************************************** private method **************************************************************/
    private void dealFirstLevel(JSONObject source) {
        //第一层兼容性处理
        if (source.containsKey("indicesBoost") && !source.containsKey("indices_boost")) {
            Object obj = source.remove("indicesBoost");
            source.put("indices_boost", obj);
        }

        if (source.containsKey("queryBinary") && !source.containsKey(QUERY)) {
            Object obj = source.remove("queryBinary");
            source.put(QUERY, obj);
        }

        if (source.containsKey("query_binary") && !source.containsKey(QUERY)) {
            Object obj = source.remove("query_binary");
            source.put(QUERY, obj);
        }

        if (source.containsKey("filter") && !source.containsKey(POST_FILTER)) {
            Object obj = source.remove("filter");
            source.put(POST_FILTER, obj);
        }

        if (source.containsKey("postFilter") && !source.containsKey(POST_FILTER)) {
            Object obj = source.remove("postFilter");
            source.put(POST_FILTER, obj);
        }

        if (source.containsKey("trackScores") && !source.containsKey("track_scores")) {
            Object obj = source.remove("trackScores");
            source.put("track_scores", obj);
        }

        if (source.containsKey("minScore") && !source.containsKey("min_score")) {
            Object obj = source.remove("min_score");
            source.put("minScore", obj);
        }
    }

    /**
     * 得到aggs的路径，路径上记录了aggs的名称，类型，aggs的字段，用作之后设置typed_key，
     * 以及将order，bucket_path进行相应的设置
     * @param parent 父path
     * @param aggs 聚合对象
     */
    private void buildAggsPath(AggsPath parent, JSONObject aggs, Map<String, FieldInfo> fieldInfoMap) {
        for (String key : aggs.keySet()) {
            AggsPath path = new AggsPath();
            path.setAggsName(key);

            JSONObject aggsItem = aggs.getJSONObject(key);
            for (String inKey : aggsItem.keySet()) {
                switch (inKey) {
                    case "meta":
                        break;
                    case AGG:
                    case "aggs":
                        buildAggsPath(path, aggsItem.getJSONObject(inKey), fieldInfoMap);
                        break;
                    default:
                        buildField(path, inKey, aggsItem, fieldInfoMap);
                }
            }

            parent.addItems(key, path);
        }
    }

    /**
     * 构建typed_key
     * @param path
     * @param type
     * @param aggsItem
     * @param fieldInfoMap
     */
    private void buildField(AggsPath path, String type, JSONObject aggsItem, Map<String, FieldInfo> fieldInfoMap) {
        JSONObject item = aggsItem.getJSONObject(type);
        switch (type.toLowerCase()) {
            case "significant_terms" :
                path.setAggsTypedKey("sigsterms");
                break;
            case "ranges" :
                break;
            case "terms" :
                path.setAggsTypedKey("sterms");
                if (item.containsKey("field")) {
                    String field = item.getString("field");
                    FieldInfo fieldInfo = fieldInfoMap.get(field);
                    if (fieldInfo != null) {
                        switch (fieldInfo.getType().toLowerCase()) {
                            case "double" :
                            case "float":
                            case "half_float":
                            case "scaled_float":
                                path.setAggsTypedKey("dterms");
                                break;
                            case "long" :
                            case "integer":
                            case "short":
                                path.setAggsTypedKey("lterms");
                                break;
                            default:
                        }
                    }
                }
                break;
            case "avg_bucket" :
            case "max_bucket":
            case "min_bucket":
            case "sum_bucket":
                path.setAggsTypedKey("simple_value");
                break;
            case "percentile_ranks" :
                if (item.containsKey("hdr")) {
                    path.setAggsTypedKey("hdr_percentile_ranks");
                } else {
                    path.setAggsTypedKey("tdigest_percentile_ranks");
                }
                break;
            case "percentiles" :
                path.setAggsTypedKey("tdigest_percentiles");
                if (item.containsKey("hdr")) {
                    path.setAggsTypedKey("hdr_percentiles");
                }
                break;
            default:
                path.setAggsTypedKey(type);
                break;
        }

    }

    private JSONObject buildTypedKey(AggsPath parent, JSONObject aggs) {
        JSONObject newAggs = new JSONObject();
        Iterator<Map.Entry<String, Object>> iter = aggs.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Object> entry = iter.next();
            String key = entry.getKey();
            JSONObject aggsItem = (JSONObject) entry.getValue();
            JSONObject newAggsItem = new JSONObject();

            AggsPath path = parent.getItem(key);

            for (Map.Entry<String,Object> aggEntry : aggsItem.entrySet()) {
                String inKey = aggEntry.getKey();
                switch (inKey) {
                    case "meta":
                        newAggsItem.put(inKey, aggsItem.get(inKey));
                        break;
                    case AGG:
                    case "aggs":
                        JSONObject newItem = buildTypedKey(path, aggsItem.getJSONObject(inKey));
                        newAggsItem.put(inKey, newItem);
                        break;
                    default:
                        JSONObject aggsType = aggsItem.getJSONObject(inKey);
                        if (aggsType.containsKey(ORDER) && inKey.equals("terms")) {
                            if (aggsType.get(ORDER) instanceof JSONObject) {
                                JSONObject order = aggsType.getJSONObject(ORDER);
                                JSONObject newOrder = dealOrder(path, order);
                                aggsType.put(ORDER, newOrder);
                            } else if (aggsType.get(ORDER) instanceof JSONArray) {
                                JSONArray newOrder = new JSONArray();
                                for (Object o : (JSONArray) aggsType.get(ORDER)) {
                                    JSONObject order = (JSONObject) o;
                                    newOrder.add(dealOrder(path, order));
                                }
                                aggsType.put(ORDER, newOrder);
                            }
                        }
                        newAggsItem.put(inKey, aggsType);
                        break;
                }
            }

            newAggs.put(path.getAggsTypedKey() + TYPE_SPLIT + key, newAggsItem);
        }

        return newAggs;
    }

    private JSONObject dealOrder(AggsPath path, JSONObject order) {
        JSONObject newOrder = new JSONObject();
        Iterator<Map.Entry<String, Object>> iter = order.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Object> entryOrder = iter.next();
            String key = entryOrder.getKey();
            Object value = entryOrder.getValue();

            if (key.equalsIgnoreCase("_key")) {
                newOrder.put(TERM, value);
            } else if (key.equalsIgnoreCase("_count")) {
                newOrder.put("_count", value);
            } else if (key.equalsIgnoreCase(TERM)) {
                newOrder.put(TERM, value);
            } else {
                String[] keys = key.split(">");
                int i = 0;
                StringBuilder builder = new StringBuilder();
                while (i < keys.length) {
                    String item = keys[i];
                    boolean find = false;
                    AggsPath aggsPath = null;
                    if (path.getItems() != null) {
                        for (Map.Entry<String, AggsPath> entry : path.getItems().entrySet()) {
                            if (entry.getKey().equals(item)) {
                                find = true;
                                aggsPath = entry.getValue();
                                break;
                            }
                        }
                    }

                    if (!find) {
                        builder = new StringBuilder(key);
                        break;
                    }

                    builder.append(aggsPath.getAggsTypedKey());
                    builder.append(TYPE_SPLIT);
                    builder.append(item);

                    if (i < keys.length - 1) {
                        builder.append(">");
                    }

                    i++;
                }
                newOrder.put(builder.toString(), value);
            }
        }

        return newOrder;
    }

    private JSONObject getAggsObject(JSONObject parent) {
        Object aggs = parent.get("aggs");
        if (aggs == null) {
            aggs = parent.get(AGG);
        }

        if (aggs == null) {
            return null;
        }

        return (JSONObject) aggs;
    }

    private void putAggsObject(JSONObject source, JSONObject aggs) {
        if (source.containsKey("aggs")) {
            source.put("aggs", aggs);
        } else {
            source.put(AGG, aggs);
        }
    }
}
