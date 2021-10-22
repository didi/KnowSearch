package com.didi.arius.gateway.dsl.dsl.parser.query;

import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.dsl.dsl.ast.common.KeyWord;
import com.didi.arius.gateway.dsl.dsl.ast.common.multi.NodeMap;
import com.didi.arius.gateway.dsl.dsl.ast.query.Query;
import com.didi.arius.gateway.dsl.dsl.parser.DslParser;
import com.didi.arius.gateway.dsl.dsl.parser.ParserRegister;
import com.didi.arius.gateway.dsl.dsl.parser.ParserType;
import com.didi.arius.gateway.dsl.dsl.parser.logic.*;
import com.didi.arius.gateway.dsl.dsl.parser.script.ScriptParser;

public class QueryParser extends DslParser {

    public QueryParser(ParserType type) {
        super(type);
    }

    @Override
    public KeyWord parse(String name, Object obj) throws Exception {
        Query node = new Query(name);

        NodeMap.toString2Node(parserType, (JSONObject) obj, node.m);

        return node;
    }

    public static void registe() {
        ParserRegister.registe(ParserType.QUERY, "bool",    new BoolParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.QUERY, "constant_score",    new ConstantScoreParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.QUERY, "exists",    new ExistsParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.QUERY, "filter",  new FilterParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.QUERY, "filtered",new FilteredParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.QUERY, "filters",  new FilterParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.QUERY, "function_score",    new FunctionScoreParser(ParserType.QUERY));

        ParserRegister.registe(ParserType.QUERY, "geo_bbox",    new GeoBboxParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.QUERY, "geo_bounding_box",    new GeoDistanceParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.QUERY, "geo_distance",    new GeoDistanceParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.QUERY, "geo_polygon",    new GeoDistanceParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.QUERY, "geo_shape",    new GeoShapeParser(ParserType.QUERY));

        ParserRegister.registe(ParserType.QUERY, "has_child",    new HasChildParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.QUERY, "has_parent",    new HasParentParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.QUERY, "ids",    new IdsParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.QUERY, "match",   new MatchParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.QUERY, "match_all",    new MatchAllParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.QUERY, "match_none",    new MatchNoneParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.QUERY, "match_phrase",    new MatchParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.QUERY, "match_phrase_prefix",    new MatchPhrasePrefixParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.QUERY, "missing",    new MissingParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.QUERY, "must",    new MustParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.QUERY, "and",    new AndParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.QUERY, "or",    new OrParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.QUERY, "must_not",new MustNotParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.QUERY, "nested",    new NestedParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.QUERY, "not",  new NotParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.QUERY, "prefix",    new PrefixParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.QUERY, "query",   new QueryqueryParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.QUERY, "query_string",    new QueryStringParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.QUERY, "range",   new RangeParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.QUERY, "regexp",    new RegexpParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.QUERY, "script",    new ScriptParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.QUERY, "shoud",   new ShouldParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.QUERY, "should",  new ShouldParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.QUERY, "term",    new TermParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.QUERY, "terms",   new TermsParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.QUERY, "wildcard",    new WildcardParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.QUERY, "more_like_this",    new MoreLikeThisParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.QUERY, "multi_match",    new MultiMatchParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.QUERY, "fuzzy",    new FuzzyParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.QUERY, "boosting", new BoostingParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.QUERY, "positive", new PositiveParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.QUERY, "negative", new NegativeParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.QUERY, "common", new CommonParser(ParserType.QUERY));
        ParserRegister.registe(ParserType.QUERY, "dis_max", new DisMaxParser(ParserType.QUERY));
    }
}
