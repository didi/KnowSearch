/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.sql.querydsl.query;

import org.elasticsearch.common.Booleans;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.xpack.sql.expression.predicate.fulltext.MatchQueryPredicate;
import org.elasticsearch.xpack.sql.tree.Source;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

public class MatchQuery extends LeafQuery {

    private static final Map<String, BiConsumer<MatchQueryBuilder, String>> BUILDER_APPLIERS;

    static {
        HashMap<String, BiConsumer<MatchQueryBuilder, String>> appliers = new HashMap<>(11);
        // TODO: it'd be great if these could be constants instead of Strings, needs a core change to make the fields public first
        // TODO: add zero terms query support, I'm not sure the best way to parse it yet...
        // appliers.put("zero_terms_query", (qb, s) -> qb.zeroTermsQuery(s));
        appliers.put("analyzer", (qb, s) -> qb.analyzer(s));
        appliers.put("auto_generate_synonyms_phrase_query", (qb, s) -> qb.autoGenerateSynonymsPhraseQuery(Booleans.parseBoolean(s)));
        appliers.put("cutoff_frequency", (qb, s) -> qb.cutoffFrequency(Float.valueOf(s)));
        appliers.put("fuzziness", (qb, s) -> qb.fuzziness(Fuzziness.build(s)));
        appliers.put("fuzzy_transpositions", (qb, s) -> qb.fuzzyTranspositions(Booleans.parseBoolean(s)));
        appliers.put("fuzzy_rewrite", (qb, s) -> qb.fuzzyRewrite(s));
        appliers.put("lenient", (qb, s) -> qb.lenient(Booleans.parseBoolean(s)));
        appliers.put("max_expansions", (qb, s) -> qb.maxExpansions(Integer.valueOf(s)));
        appliers.put("minimum_should_match", (qb, s) -> qb.minimumShouldMatch(s));
        appliers.put("operator", (qb, s) -> qb.operator(Operator.fromString(s)));
        appliers.put("prefix_length", (qb, s) -> qb.prefixLength(Integer.valueOf(s)));
        BUILDER_APPLIERS = Collections.unmodifiableMap(appliers);
    }

    private final String name;
    private final Object text;
    private final MatchQueryPredicate predicate;
    private final Map<String, String> options;


    public MatchQuery(Source source, String name, Object text) {
        this(source, name, text, null);
    }

    public MatchQuery(Source source, String name, Object text, MatchQueryPredicate predicate) {
        super(source);
        this.name = name;
        this.text = text;
        this.predicate = predicate;
        this.options = predicate == null ? Collections.emptyMap() : predicate.optionMap();
    }

    @Override
    public QueryBuilder asBuilder() {
        final MatchQueryBuilder queryBuilder = QueryBuilders.matchQuery(name, text);
        options.forEach((k, v) -> {
            if (BUILDER_APPLIERS.containsKey(k)) {
                BUILDER_APPLIERS.get(k).accept(queryBuilder, v);
            } else {
                throw new IllegalArgumentException("illegal match option [" + k + "]");
            }
        });
        return queryBuilder;
    }

    public String name() {
        return name;
    }

    public Object text() {
        return text;
    }

    MatchQueryPredicate predicate() {
        return predicate;
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, name, predicate);
    }

    @Override
    public boolean equals(Object obj) {
        if (false == super.equals(obj)) {
            return false;
        }

        MatchQuery other = (MatchQuery) obj;
        return Objects.equals(text, other.text)
                && Objects.equals(name, other.name)
                && Objects.equals(predicate, other.predicate);
    }

    @Override
    protected String innerToString() {
        return name + ":" + text;
    }
}
