package com.didi.arius.gateway.dsl.dsl.visitor.basic;

import com.didi.arius.gateway.dsl.dsl.ast.aggr.*;
import com.didi.arius.gateway.dsl.dsl.ast.DslNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.script.Script;
import com.didi.arius.gateway.dsl.dsl.ast.common.logic.*;
import com.didi.arius.gateway.dsl.dsl.ast.common.value.QueryStringValueNode;
import com.didi.arius.gateway.dsl.dsl.ast.root.*;
import com.didi.arius.gateway.dsl.dsl.ast.query.*;

public abstract class BaseVisitor implements Visitor {
    @Override
    public void visit(DslNode node) {
        node.m.accept(this);
    }

    /*---- root -----*/
    @Override
    public void visit(From node) {
        node.v.accept(this);
    }

    @Override
    public void visit(Size node) {
        node.v.accept(this);
    }

    @Override
    public void visit(Sort node) {
        node.n.accept(this);
    }

    @Override
    public void visit(Source node) {
        node.n.accept(this);
    }

    @Override
    public void visit(Timeout node) {
        node.v.accept(this);
    }

    @Override
    public void visit(Highlight node) {
        node.n.accept(this);
    }

    @Override
    public void visit(IndicesBoost node) {
        node.n.accept(this);
    }

    @Override
    public void visit(MinScore node) {
        node.n.accept(this);
    }

    @Override
    public void visit(PostFilter node) {
        node.n.accept(this);
    }

    @Override
    public void visit(Profile node) {
        node.n.accept(this);
    }

    @Override
    public void visit(TerminateAfter node) {
        node.n.accept(this);
    }

    @Override
    public void visit(TrackScores node) {
        node.n.accept(this);
    }

    @Override
    public void visit(Rescore node) {
        node.n.accept(this);
    }

    @Override
    public void visit(Fields node) {
        node.n.accept(this);
    }

    @Override
    public void visit(IndexConstraints node) {
        node.n.accept(this);
    }

    @Override
    public void visit(Body node) {
        node.n.accept(this);
    }

    @Override
    public void visit(ScriptFields node) {
        node.n.accept(this);
    }

    @Override
    public void visit(Suggest node) {
        node.n.accept(this);
    }

    @Override
    public void visit(Docs node) {
        node.n.accept(this);
    }

    @Override
    public void visit(Scroll node) {
        node.n.accept(this);
    }

    @Override
    public void visit(ScrollId node) {
        node.n.accept(this);
    }

    @Override
    public void visit(Index node) {
        node.n.accept(this);
    }

    @Override
    public void visit(IgnoreUnavailable node) {
        node.n.accept(this);
    }

    @Override
    public void visit(FieldDataFields node) {
        node.n.accept(this);
    }

    @Override
    public void visit(Type node) {
        node.n.accept(this);
    }

    @Override
    public void visit(SearchType node) {
        node.n.accept(this);
    }

    @Override
    public void visit(Explain node) {
        node.n.accept(this);
    }

    @Override
    public void visit(QueryBinary node) {
        node.n.accept(this);
    }

    @Override
    public void visit(TimeFieldName node) {
        node.n.accept(this);
    }

    @Override
    public void visit(Title node) {
        node.n.accept(this);
    }

    @Override
    public void visit(FieldFormatMap node) {
        node.n.accept(this);
    }

    /*---- logic -----*/
    @Override
    public void visit(Bool node) {
        node.m.accept(this);
    }

    @Override
    public void visit(Must node) {
        node.n.accept(this);
    }

    @Override
    public void visit(Should node) {
        node.n.accept(this);
    }

    @Override
    public void visit(MustNot node) {
        node.n.accept(this);
    }

    @Override
    public void visit(Filter node) {
        node.n.accept(this);
    }

    @Override
    public void visit(Not node) {
        node.n.accept(this);
    }

    @Override
    public void visit(And node) {
        node.n.accept(this);
    }

    @Override
    public void visit(Or node) {
        node.n.accept(this);
    }


    /*---- aggr -----*/
    @Override
    public void visit(Aggs node) {
        node.m.accept(this);
    }

    @Override
    public void visit(AvgBucket node) {
        node.m.accept(this);
    }

    @Override
    public void visit(MaxBucket node) {
        node.m.accept(this);
    }

    @Override
    public void visit(DateHistoGram node) {
        node.m.accept(this);
    }

    @Override
    public void visit(Sum node) {
        node.m.accept(this);
    }

    @Override
    public void visit(AggrTerms node) {
        node.m.accept(this);
    }

    @Override
    public void visit(Avg node) {
        node.m.accept(this);
    }

    @Override
    public void visit(TopHits node) {
        node.m.accept(this);
    }

    @Override
    public void visit(Cardinality node) {
        node.n.accept(this);
    }

    @Override
    public void visit(ValueCount node) {
        node.n.accept(this);
    }

    @Override
    public void visit(Max node) {
        node.n.accept(this);
    }

    @Override
    public void visit(Min node) {
        node.n.accept(this);
    }

    @Override
    public void visit(ScriptedMetric node) {
        node.n.accept(this);
    }

    @Override
    public void visit(BucketSelector node) {
        node.n.accept(this);
    }

    @Override
    public void visit(AggrRange node) {
        node.n.accept(this);
    }

    @Override
    public void visit(Stats node) {
        node.n.accept(this);
    }

    @Override
    public void visit(Percentiles node) {
        node.n.accept(this);
    }

    @Override
    public void visit(AggrNested node) {
        node.n.accept(this);
    }

    @Override
    public void visit(PercentileRanks node) {
        node.n.accept(this);
    }

    @Override
    public void visit(SignificantTerms node) {
        node.n.accept(this);
    }

    @Override
    public void visit(Histogram node) {
        node.n.accept(this);
    }

    @Override
    public void visit(Children node) {
        node.n.accept(this);
    }

    @Override
    public void visit(Sampler node) {
        node.n.accept(this);
    }

    @Override
    public void visit(ReverseNested node) {
        node.n.accept(this);
    }

    @Override
    public void visit(AggrGeoBounds node) {
        node.n.accept(this);
    }

    @Override
    public void visit(AggrGeoCentroid node) {
        node.n.accept(this);
    }

    @Override
    public void visit(AggrGeoDistance node) {
        node.n.accept(this);
    }

    @Override
    public void visit(AggrGeohashGrid node) {
        node.n.accept(this);
    }

    @Override
    public void visit(AggrMissing node) {
        node.n.accept(this);
    }

    @Override
    public void visit(DateRange node) {
        node.n.accept(this);
    }

    @Override
    public void visit(ExtendedStats node) {
        node.n.accept(this);
    }

    @Override
    public void visit(Global node) {
        node.n.accept(this);
    }

    @Override
    public void visit(IpRange node) {
        node.n.accept(this);
    }

    @Override
    public void visit(PercentilesBucket node) {
        node.n.accept(this);
    }

    /**** query ******/
    @Override
    public void visit(Query node) {
        node.m.accept(this);
    }

    @Override
    public void visit(Term node) {
        node.m.accept(this);
    }

    @Override
    public void visit(Terms node) {
        node.m.accept(this);
    }

    @Override
    public void visit(Range node) {
        node.m.accept(this);
    }

    @Override
    public void visit(Filtered node) {
        node.m.accept(this);
    }

    @Override
    public void visit(Queryquery node) {
        node.m.accept(this);
    }

    @Override
    public void visit(Match node) {
        node.m.accept(this);
    }

    @Override
    public void visit(Wildcard node) {
        node.m.accept(this);
    }

    @Override
    public void visit(Prefix node) {
        node.m.accept(this);
    }

    @Override
    public void visit(Exists node) {
        node.m.accept(this);
    }

    @Override
    public void visit(Regexp node) {
        node.m.accept(this);
    }

    @Override
    public void visit(HasParent node) {
        node.m.accept(this);
    }

    @Override
    public void visit(GeoShape node) {
        node.n.accept(this);
    }

    @Override
    public void visit(QueryString node) {
        node.n.accept(this);
    }

    @Override
    public void visit(FunctionScore node) {
        node.n.accept(this);
    }

    @Override
    public void visit(ConstantScore node) {
        node.n.accept(this);
    }

    @Override
    public void visit(GeoBbox node) {
        node.n.accept(this);
    }

    @Override
    public void visit(Nested node) {
        node.n.accept(this);
    }

    @Override
    public void visit(GeoDistance node) {
        node.n.accept(this);
    }

    @Override
    public void visit(MatchAll node) {
        node.n.accept(this);
    }

    @Override
    public void visit(MatchNone node) {
        node.n.accept(this);
    }

    @Override
    public void visit(Ids node) {
        node.n.accept(this);
    }

    @Override
    public void visit(HasChild node) {
        node.n.accept(this);
    }

    @Override
    public void visit(Missing node) {
        node.n.accept(this);
    }

    @Override
    public void visit(MatchPhrasePrefix node) {
        node.n.accept(this);
    }

    @Override
    public void visit(MoreLikeThis node) {
        node.n.accept(this);
    }

    @Override
    public void visit(MultiMatch node) {
        node.n.accept(this);
    }

    @Override
    public void visit(Fuzzy node) {
        node.n.accept(this);
    }

    @Override
    public void visit(Boosting node) {
        node.n.accept(this);
    }

    @Override
    public void visit(Positive node) {
        node.m.accept(this);
    }

    @Override
    public void visit(Negative node) {
        node.m.accept(this);
    }

    @Override
    public void visit(Common node) {
        node.n.accept(this);
    }

    @Override
    public void visit(DisMax node) {
        node.n.accept(this);
    }

    @Override
    public void visit(HighlightQuery node) {
        node.m.accept(this);
    }

    /**** scrip ******/
    @Override
    public void visit(Script node) {
        node.n.accept(this);
    }

    @Override
    public void visit(QueryStringValueNode node) {
        throw new RuntimeException("not support");
    }
}
