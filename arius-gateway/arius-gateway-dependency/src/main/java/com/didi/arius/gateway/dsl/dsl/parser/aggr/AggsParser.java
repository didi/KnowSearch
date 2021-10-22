package com.didi.arius.gateway.dsl.dsl.parser.aggr;

import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.dsl.dsl.ast.aggr.Aggs;
import com.didi.arius.gateway.dsl.dsl.ast.common.KeyWord;
import com.didi.arius.gateway.dsl.dsl.ast.common.Node;
import com.didi.arius.gateway.dsl.dsl.ast.common.key.IdentityNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.key.KeyNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.key.StringNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.multi.NodeMap;
import com.didi.arius.gateway.dsl.dsl.ast.common.value.ValueNode;
import com.didi.arius.gateway.dsl.dsl.parser.DslParser;
import com.didi.arius.gateway.dsl.dsl.parser.ParserRegister;
import com.didi.arius.gateway.dsl.dsl.parser.ParserType;
import com.didi.arius.gateway.dsl.dsl.parser.logic.*;
import com.didi.arius.gateway.dsl.dsl.parser.root.SortParser;
import com.didi.arius.gateway.dsl.dsl.parser.script.ScriptParser;
import com.didi.arius.gateway.dsl.dsl.util.ConstValue;

public class AggsParser extends DslParser  {
    public AggsParser(ParserType type) {
        super(type);
    }

    @Override
    public KeyWord parse(String name, Object obj) throws Exception {
        Aggs node = new Aggs(name);

        JSONObject jsonObject = (JSONObject) obj;
        for(String key : jsonObject.keySet()) {
            KeyNode keyNode = new IdentityNode(key);
            Node valueNode = parseAgg(jsonObject.get(key));

            node.m.m.put(keyNode, valueNode);
        }

        return node;
    }

    public NodeMap parseAgg(Object obj) throws Exception {
        NodeMap m = new NodeMap();

        JSONObject jsonObject = (JSONObject) obj;
        for(String key : jsonObject.keySet()) {

            KeyNode keyNode;
            Node valueNode;

            if(key.equalsIgnoreCase(ConstValue.META)) {
                keyNode = new StringNode(key);
                valueNode = ValueNode.getValueNode(jsonObject.get(key));

            } else {
                keyNode = new StringNode(key);

                valueNode = ParserRegister.parse(parserType, key, jsonObject.get(key));
                if(valueNode==null) {
                    throw new Exception("unknown json, json:" + key);
//                    valueNode = ValueNode.getValueNode(jsonObject.get(key));
                }
            }

            m.m.put(keyNode, valueNode);
        }

        return m;
    }

    public static void registe() {
        ParserRegister.registe(ParserType.AGGR, "aggregations", 		new AggsParser(ParserType.AGGR));
        ParserRegister.registe(ParserType.AGGR, "aggs", 				new AggsParser(ParserType.AGGR));
        ParserRegister.registe(ParserType.AGGR, "avg", 				new AvgParser(ParserType.AGGR));
        ParserRegister.registe(ParserType.AGGR, "avg_bucket", 		new AvgBucketParser(ParserType.AGGR));
        ParserRegister.registe(ParserType.AGGR, "max_bucket", 		new MaxBucketParser(ParserType.AGGR));
        ParserRegister.registe(ParserType.AGGR, "percentiles_bucket", new PercentilesBucketParser(ParserType.AGGR));
        ParserRegister.registe(ParserType.AGGR, "bucket_selector", 	new BucketSelectorParser(ParserType.AGGR));
        ParserRegister.registe(ParserType.AGGR, "cardinality", 		new CardinalityParser(ParserType.AGGR));
        ParserRegister.registe(ParserType.AGGR, "children", 			new ChildrenParser(ParserType.AGGR));
        ParserRegister.registe(ParserType.AGGR, "date_histogram", 	new DateHistoGramParser(ParserType.AGGR));
        ParserRegister.registe(ParserType.AGGR, "histogram", 			new HistogramParser(ParserType.AGGR));
        ParserRegister.registe(ParserType.AGGR, "max", 				new MaxParser(ParserType.AGGR));
        ParserRegister.registe(ParserType.AGGR, "min", 				new MinParser(ParserType.AGGR));
        ParserRegister.registe(ParserType.AGGR, "nested", 			new AggrNestedParser(ParserType.AGGR));
        ParserRegister.registe(ParserType.AGGR, "percentile_ranks", 	new PercentileRanksParser(ParserType.AGGR));
        ParserRegister.registe(ParserType.AGGR, "percentiles", 		new PercentilesParser(ParserType.AGGR));
        ParserRegister.registe(ParserType.AGGR, "range", 				new AggrRangeParser(ParserType.AGGR));
        ParserRegister.registe(ParserType.AGGR, "reverse_nested", 	new ReverseNestedParser(ParserType.AGGR));
        ParserRegister.registe(ParserType.AGGR, "sampler", 			new SamplerParser(ParserType.AGGR));
        ParserRegister.registe(ParserType.AGGR, "scripted_metric", 	new ScriptedMetricParser(ParserType.AGGR));
        ParserRegister.registe(ParserType.AGGR, "significant_terms", 	new SignificantTermsParser(ParserType.AGGR));
        ParserRegister.registe(ParserType.AGGR, "sort", 				new SortParser(ParserType.AGGR));
        ParserRegister.registe(ParserType.AGGR, "stats", 				new StatsParser(ParserType.AGGR));
        ParserRegister.registe(ParserType.AGGR, "sum", 				new SumParser(ParserType.AGGR));
        ParserRegister.registe(ParserType.AGGR, "terms", 				new AggTermsParser(ParserType.AGGR));
        ParserRegister.registe(ParserType.AGGR, "top_hits", 			new TopHitsParser(ParserType.AGGR));
        ParserRegister.registe(ParserType.AGGR, "value_count", 		new ValueCountParser(ParserType.AGGR));
        ParserRegister.registe(ParserType.AGGR, "script",    			new ScriptParser(ParserType.AGGR));
        ParserRegister.registe(ParserType.AGGR, "date_range",    		new DateRangeParser(ParserType.AGGR));
        ParserRegister.registe(ParserType.AGGR, "extended_stats",    	new ExtendedStatsParser(ParserType.AGGR));
        ParserRegister.registe(ParserType.AGGR, "geo_bounds",    		new AggrGeoBoundsParser(ParserType.AGGR));
        ParserRegister.registe(ParserType.AGGR, "geo_centroid",    	new AggrGeoCentroidParser(ParserType.AGGR));
        ParserRegister.registe(ParserType.AGGR, "geo_distance",    	new AggrGeoDistanceParser(ParserType.AGGR));
        ParserRegister.registe(ParserType.AGGR, "geohash_grid",    	new AggrGeohashGridParser(ParserType.AGGR));
        ParserRegister.registe(ParserType.AGGR, "global",    			new GlobalParser(ParserType.AGGR));
        ParserRegister.registe(ParserType.AGGR, "ip_range",    		new IpRangeParser(ParserType.AGGR));
        ParserRegister.registe(ParserType.AGGR, "missing",      		new AggrMissingParser(ParserType.AGGR));


        ParserRegister.registe(ParserType.AGGR, "bool",    	new BoolParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.AGGR, "must",    	new MustParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.AGGR, "shoud",   	new ShouldParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.AGGR, "must_not",	new MustNotParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.AGGR, "should",  	new ShouldParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.AGGR, "filter",  	new FilterParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.AGGR, "filters",  	new FilterParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.AGGR, "not",  	    new NotParser(ParserType.QUERY));
    }
}
