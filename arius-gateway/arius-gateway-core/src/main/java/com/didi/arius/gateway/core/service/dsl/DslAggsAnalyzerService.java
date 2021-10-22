package com.didi.arius.gateway.core.service.dsl;

import com.didi.arius.gateway.common.metadata.AggsAnalyzerContext;
import com.didi.arius.gateway.common.metadata.AggsBukcetInfo;
import com.didi.arius.gateway.common.metadata.BaseContext;
import com.didi.arius.gateway.common.metadata.FieldInfo;
import com.google.gson.JsonObject;
import org.elasticsearch.common.bytes.BytesReference;

import java.util.Map;

/**
 * @author fitz
 * @date 2021/5/25 7:23 下午
 * dsl结果聚合分析的，看看聚合计算的bucket有多大
 */
public interface DslAggsAnalyzerService {
    /**
     *
     * dsl结果聚合分析，看看聚合计算的bucket有多大，是否满足需求
     * @param baseContext
     * @param source
     * @param indices
     * @param cluster
     * @return
     */
    boolean analyze(BaseContext baseContext, BytesReference source, String[] indices, String cluster);

    /**
     * 同上， 聚合分析
     * @param baseContext
     * @param source
     * @param indices
     * @return
     */
    boolean analyzeAggs(BaseContext baseContext, BytesReference source, String[] indices);

    /**
     * 检测聚合查询bucket信息
     * @param aggsObject
     * @param level
     * @param mergedMappings
     * @param context
     * @return
     */
    AggsBukcetInfo checkAggs(JsonObject aggsObject, int level, Map<String, FieldInfo> mergedMappings, AggsAnalyzerContext context);

    /**
     * 合并templateAliasesMap和templateExpressionMap
     * @param indices
     * @param cluster
     * @return
     */
    Map<String, FieldInfo> mergeMappings(String[] indices, String cluster);
}
