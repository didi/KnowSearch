package com.didi.arius.gateway.dsl.dsl.parser;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.didi.arius.gateway.dsl.dsl.ast.DslNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.KeyWord;
import com.didi.arius.gateway.dsl.dsl.ast.common.Node;
import com.didi.arius.gateway.dsl.dsl.ast.common.key.KeyNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.key.StringNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.value.ValueNode;
import com.didi.arius.gateway.dsl.dsl.parser.aggr.AggsParser;
import com.didi.arius.gateway.dsl.dsl.parser.logic.FilterParser;
import com.didi.arius.gateway.dsl.dsl.parser.query.QueryParser;
import com.didi.arius.gateway.dsl.dsl.parser.root.*;
import com.didi.arius.gateway.dsl.dsl.parser.script.ScriptParser;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;

public abstract class DslParser {
    protected ParserType parserType;

    private static final ILog LOGGER = LogFactory.getLog(DslParser.class);

    public DslParser(ParserType type) {
        this.parserType = type;
    }

    public abstract KeyWord parse(String name, Object obj) throws Exception;

    public static DslNode parse(Object obj) throws Exception {
        DslNode dslParser = new DslNode();

        JSONObject jsonObject = (JSONObject) obj;
        for (String key : jsonObject.keySet()) {
            KeyNode keyNode = new StringNode(key);
            Node valueNode = ParserRegister.parse(ParserType.COMMON, key, jsonObject.get(key));
            if (valueNode == null) {
                //throw new Exception("wrong json, json:" + key);
                //valueNode = ValueNode.getValueNode(jsonObject.get(key));
                LOGGER.error("DslParser can't find {} parser, jsonObject {}", key, JSON.toJSONString(jsonObject, SerializerFeature.WriteMapNullValue));
                valueNode = ValueNode.getValueNode(jsonObject.get(key));
            }

            dslParser.m.m.put(keyNode, valueNode);
        }

        return dslParser;
    }


    static {
        ParserRegister.registe(ParserType.COMMON, "body", new BodyParser(ParserType.COMMON));

        ParserRegister.registe(ParserType.COMMON, "query", new QueryParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.COMMON, "filter", new FilterParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.COMMON, "aggs", new AggsParser(ParserType.AGGR));
        ParserRegister.registe(ParserType.COMMON, "aggregations", new AggsParser(ParserType.AGGR));
        ParserRegister.registe(ParserType.COMMON, "post_filter", new PostFilterParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.COMMON, "postFilter", new PostFilterParser(ParserType.QUERY));

        ParserRegister.registe(ParserType.COMMON, "from", new FromParser(ParserType.COMMON));
        ParserRegister.registe(ParserType.COMMON, "size", new SizeParser(ParserType.COMMON));
        ParserRegister.registe(ParserType.COMMON, "sort", new SortParser(ParserType.COMMON));
        ParserRegister.registe(ParserType.COMMON, "_source", new SourceParser(ParserType.COMMON));
        ParserRegister.registe(ParserType.COMMON, "timeout", new TimeoutParser(ParserType.COMMON));
        ParserRegister.registe(ParserType.COMMON, "highlight", new HighlightParser(ParserType.COMMON));
        ParserRegister.registe(ParserType.COMMON, "indices_boost", new IndicesBoostParser(ParserType.COMMON));
        ParserRegister.registe(ParserType.COMMON, "indicesBoost", new IndicesBoostParser(ParserType.COMMON));
        ParserRegister.registe(ParserType.COMMON, "trackScores", new TrackScoresParser(ParserType.COMMON));
        ParserRegister.registe(ParserType.COMMON, "track_scores", new TrackScoresParser(ParserType.COMMON));
        ParserRegister.registe(ParserType.COMMON, "min_score", new MinScoreParser(ParserType.COMMON));
        ParserRegister.registe(ParserType.COMMON, "minScore", new MinScoreParser(ParserType.COMMON));
        ParserRegister.registe(ParserType.COMMON, "terminate_after", new TerminateAfterParser(ParserType.COMMON));
        ParserRegister.registe(ParserType.COMMON, "profile", new ProfileParser(ParserType.COMMON));
        ParserRegister.registe(ParserType.COMMON, "rescore", new RescoreParser(ParserType.COMMON));
        ParserRegister.registe(ParserType.COMMON, "fields", new FieldsParser(ParserType.COMMON));
        ParserRegister.registe(ParserType.COMMON, "index_constraints", new IndexConstraintsParser(ParserType.COMMON));
        ParserRegister.registe(ParserType.COMMON, "script_fields", new ScriptFieldsParser(ParserType.COMMON));
        ParserRegister.registe(ParserType.COMMON, "suggest", new SuggestParser(ParserType.COMMON));
        ParserRegister.registe(ParserType.COMMON, "docs", new DocsParser(ParserType.COMMON));
        ParserRegister.registe(ParserType.COMMON, "scroll", new ScrollParser(ParserType.COMMON));
        ParserRegister.registe(ParserType.COMMON, "scroll_id", new ScrollIdParser(ParserType.COMMON));
        ParserRegister.registe(ParserType.COMMON, "scrollId", new ScrollIdParser(ParserType.COMMON));
        ParserRegister.registe(ParserType.COMMON, "index", new IndexParser(ParserType.COMMON));
        ParserRegister.registe(ParserType.COMMON, "ignore_unavailable", new IgnoreUnavailableParser(ParserType.COMMON));
        ParserRegister.registe(ParserType.COMMON, "fielddata_fields", new FieldDataFieldsParser(ParserType.COMMON));
        ParserRegister.registe(ParserType.COMMON, "type", new TypeParser(ParserType.COMMON));
        ParserRegister.registe(ParserType.COMMON, "search_type", new SearchTypeParser(ParserType.COMMON));
        ParserRegister.registe(ParserType.COMMON, "explain", new ExplainParser(ParserType.COMMON));
        ParserRegister.registe(ParserType.COMMON, "query_binary", new QueryBinaryParser(ParserType.COMMON));
        ParserRegister.registe(ParserType.COMMON, "timeFieldName", new TimeFieldNameParser(ParserType.COMMON));
        ParserRegister.registe(ParserType.COMMON, "title", new TitleParser(ParserType.COMMON));
        ParserRegister.registe(ParserType.COMMON, "fieldFormatMap", new FieldFormatMapParser(ParserType.COMMON));

        ParserRegister.registe(ParserType.COMMON, "script", new ScriptParser(ParserType.COMMON));
        AggsParser.registe();
        QueryParser.registe();
        HighlightParser.registe();
    }
}
