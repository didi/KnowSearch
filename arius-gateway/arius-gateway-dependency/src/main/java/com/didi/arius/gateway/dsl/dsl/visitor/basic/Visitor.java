package com.didi.arius.gateway.dsl.dsl.visitor.basic;

import com.didi.arius.gateway.dsl.dsl.ast.DslNode;
import com.didi.arius.gateway.dsl.dsl.ast.aggr.*;
import com.didi.arius.gateway.dsl.dsl.ast.common.key.FieldNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.key.IdentityNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.key.StringNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.logic.*;
import com.didi.arius.gateway.dsl.dsl.ast.common.multi.NodeList;
import com.didi.arius.gateway.dsl.dsl.ast.common.multi.NodeMap;
import com.didi.arius.gateway.dsl.dsl.ast.common.script.Script;
import com.didi.arius.gateway.dsl.dsl.ast.common.value.JsonNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.value.ObjectNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.value.QueryStringValueNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.value.StringListNode;
import com.didi.arius.gateway.dsl.dsl.ast.query.*;
import com.didi.arius.gateway.dsl.dsl.ast.root.*;

public interface Visitor {
    void visit(DslNode node);

    /*---- root -----*/
    void visit(From node);

    void visit(Size node);

    void visit(Sort node);

    void visit(Source node);

    void visit(Timeout node);

    void visit(Highlight node);

    void visit(IndicesBoost node);

    void visit(MinScore node);

    void visit(PostFilter node);

    void visit(Profile node);

    void visit(TerminateAfter node);

    void visit(TrackScores node);

    void visit(Rescore node);

    void visit(Fields node);

    void visit(IndexConstraints node);

    void visit(Body node);

    void visit(ScriptFields node);

    void visit(Suggest node);

    void visit(Docs node);

    void visit(Scroll node);

    void visit(ScrollId node);

    void visit(Index node);

    void visit(IgnoreUnavailable node);

    void visit(FieldDataFields node);

    void visit(Type node);

    void visit(SearchType node);

    void visit(Explain node);

    void visit(QueryBinary node);

    void visit(TimeFieldName node);

    void visit(Title node);

    void visit(FieldFormatMap node);

    /*---- common -----*/
    void visit(FieldNode node);

    void visit(IdentityNode node);

    void visit(StringNode node);

    void visit(JsonNode node);

    void visit(ObjectNode node);


    void visit(NodeMap node);

    void visit(NodeList node);

    void visit(StringListNode node);


    /*---- logic -----*/
    void visit(Bool node);

    void visit(Must node);

    void visit(Should node);

    void visit(MustNot node);

    void visit(Filter node);

    void visit(Not node);

    void visit(And node);

    void visit(Or node);


    /*---- aggs -----*/
    void visit(Aggs node);

    void visit(AvgBucket node);

    void visit(MaxBucket node);

    void visit(DateHistoGram node);

    void visit(Sum node);

    void visit(AggrTerms node);

    void visit(Avg node);

    void visit(TopHits node);

    void visit(Cardinality node);

    void visit(ValueCount node);

    void visit(Max node);

    void visit(Min node);

    void visit(ScriptedMetric node);

    void visit(BucketSelector node);

    void visit(AggrRange node);

    void visit(Stats node);

    void visit(Percentiles node);

    void visit(AggrNested node);

    void visit(PercentileRanks node);

    void visit(SignificantTerms node);

    void visit(Histogram node);

    void visit(Children node);

    void visit(Sampler node);

    void visit(ReverseNested node);

    void visit(AggrGeoBounds node);

    void visit(AggrGeoCentroid node);

    void visit(AggrGeoDistance node);

    void visit(AggrGeohashGrid node);

    void visit(AggrMissing node);

    void visit(DateRange node);

    void visit(ExtendedStats node);

    void visit(Global node);

    void visit(IpRange node);

    void visit(PercentilesBucket node);

    /*---- query -----*/
    void visit(Query node);

    void visit(Term node);

    void visit(Terms node);

    void visit(Range node);

    void visit(Filtered node);

    void visit(Queryquery node);

    void visit(Match node);

    void visit(Wildcard node);

    void visit(Prefix node);

    void visit(Exists node);

    void visit(Regexp node);

    void visit(HasParent node);

    void visit(GeoShape node);

    void visit(QueryString node);

    void visit(FunctionScore node);

    void visit(ConstantScore node);

    void visit(GeoBbox node);

    void visit(Nested node);

    void visit(GeoDistance node);

    void visit(MatchAll node);

    void visit(MatchNone node);

    void visit(Ids node);

    void visit(HasChild node);

    void visit(Missing node);

    void visit(MatchPhrasePrefix node);

    void visit(MoreLikeThis node);

    void visit(MultiMatch node);

    void visit(Fuzzy node);

    void visit(Boosting node);

    void visit(Positive node);

    void visit(Negative node);

    void visit(Common node);

    void visit(DisMax node);

    void visit(HighlightQuery node);

    /*---- script -----*/
    void visit(Script node);

    void visit(QueryStringValueNode node);
}
