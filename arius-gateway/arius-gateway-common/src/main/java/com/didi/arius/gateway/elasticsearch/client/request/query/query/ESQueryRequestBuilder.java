/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.didi.arius.gateway.elasticsearch.client.request.query.query;

import com.didi.arius.gateway.elasticsearch.client.response.query.query.ESQueryResponse;
import org.elasticsearch.ExceptionsHelper;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.innerhits.InnerHitsBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.rescore.RescoreBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.SuggestBuilder;

import java.util.Map;

/**
 * A search request request builder.
 */
public class ESQueryRequestBuilder extends ActionRequestBuilder<ESQueryRequest, ESQueryResponse, ESQueryRequestBuilder> {

    private SearchSourceBuilder sourceBuilder;

    public ESQueryRequestBuilder(ElasticsearchClient client, ESQueryAction action) {
        super(client, action, new ESQueryRequest());
    }

    public ESQueryRequestBuilder setClazz(Class clazz) {
        request.clazz(clazz);
        return this;
    }
    /**
     * Sets the indices the search will be executed on.
     */
    public ESQueryRequestBuilder setIndices(String... indices) {
        request.indices(indices);
        return this;
    }

    /**
     * The document types to execute the search against. Defaults to be executed against
     * all types.
     */
    public ESQueryRequestBuilder setTypes(String... types) {
        request.types(types);
        return this;
    }


    /**
     * If set, will enable scrolling of the search request for the specified timeout.
     */
    public ESQueryRequestBuilder setScroll(TimeValue keepAlive) {
        request.scroll(keepAlive);
        return this;
    }

    /**
     * If set, will enable scrolling of the search request for the specified timeout.
     */
    public ESQueryRequestBuilder setScroll(String keepAlive) {
        request.scroll(keepAlive);
        return this;
    }

    public ESQueryRequestBuilder preference(String preference) {
        request.preference(preference);
        return this;
    }

    /**
     * A comma separated list of routing values to control the shards the search will be executed on.
     */
    public ESQueryRequestBuilder setRouting(String routing) {
        request.routing(routing);
        return this;
    }

    /**
     * The routing values to control the shards that the search will be executed on.
     */
    public ESQueryRequestBuilder setRouting(String... routing) {
        request.routing(routing);
        return this;
    }

    /**
     * Sets the source of the request as a json string. Note, settings anything other
     * than the search type will cause this source to be overridden, consider using
     */
    public ESQueryRequestBuilder setSource(String source) {
        request.source(source);
        return this;
    }


    /**
     * Sets the source of the request as a json string. Note, settings anything other
     * than the search type will cause this source to be overridden, consider using
     */
    public ESQueryRequestBuilder setSource(BytesReference source) {
        request.source(source);
        return this;
    }

    /**
     * Sets the source of the request as a json string. Note, settings anything other
     * than the search type will cause this source to be overridden, consider using
     */
    public ESQueryRequestBuilder setSource(byte[] source) {
        request.source(source);
        return this;
    }

    /**
     * Sets the source of the request as a json string. Note, settings anything other
     * than the search type will cause this source to be overridden, consider using
     */
    public ESQueryRequestBuilder setSource(byte[] source, int offset, int length) {
        request.source(source, offset, length);
        return this;
    }

    /**
     * Sets the source of the request as a json string. Note, settings anything other
     * than the search type will cause this source to be overridden, consider using
     */
    public ESQueryRequestBuilder setSource(XContentBuilder builder) {
        request.source(builder);
        return this;
    }

    /**
     * Should the query be profiled. Defaults to <code>false</code>
     */
    public ESQueryRequestBuilder setProfile(boolean profile) {
        sourceBuilder().profile(profile);
        return this;
    }

    /**
     * Sets the source builder to be used with this request. Note, any operations done
     * on this require builder before are discarded as this internal builder replaces
     * what has been built up until this point.
     */
    public ESQueryRequestBuilder internalBuilder(SearchSourceBuilder sourceBuilder) {
        this.sourceBuilder = sourceBuilder;
        return this;
    }



                            /* 构建dsl */

    /**
     * An optional timeout to control how long search is allowed to take.
     */
    public ESQueryRequestBuilder setTimeout(TimeValue timeout) {
        sourceBuilder().timeout(timeout);
        return this;
    }

    /**
     * An optional timeout to control how long search is allowed to take.
     */
    public ESQueryRequestBuilder setTimeout(String timeout) {
        sourceBuilder().timeout(timeout);
        return this;
    }

    /**
     * An optional document count, upon collecting which the search
     * query will early terminate
     */
    public ESQueryRequestBuilder setTerminateAfter(int terminateAfter) {
        sourceBuilder().terminateAfter(terminateAfter);
        return this;
    }

    /**
     * Constructs a new search source builder with a search query.
     *
     * @see org.elasticsearch.index.query.QueryBuilders
     */
    public ESQueryRequestBuilder setQuery(QueryBuilder queryBuilder) {
        sourceBuilder().query(queryBuilder);
        return this;
    }

    /**
     * Constructs a new search source builder with a raw search query.
     */
    public ESQueryRequestBuilder setQuery(String query) {
        sourceBuilder().query(query);
        return this;
    }

    /**
     * Constructs a new search source builder with a raw search query.
     */
    public ESQueryRequestBuilder setQuery(BytesReference queryBinary) {
        sourceBuilder().query(queryBinary);
        return this;
    }

    /**
     * Constructs a new search source builder with a raw search query.
     */
    public ESQueryRequestBuilder setQuery(byte[] queryBinary) {
        sourceBuilder().query(queryBinary);
        return this;
    }

    /**
     * Constructs a new search source builder with a raw search query.
     */
    public ESQueryRequestBuilder setQuery(byte[] queryBinary, int queryBinaryOffset, int queryBinaryLength) {
        sourceBuilder().query(queryBinary, queryBinaryOffset, queryBinaryLength);
        return this;
    }

    /**
     * Constructs a new search source builder with a raw search query.
     */
    public ESQueryRequestBuilder setQuery(XContentBuilder query) {
        sourceBuilder().query(query);
        return this;
    }

    /**
     * Constructs a new search source builder with a raw search query.
     */
    public ESQueryRequestBuilder setQuery(Map query) {
        sourceBuilder().query(query);
        return this;
    }

    /**
     * Sets a filter that will be executed after the query has been executed and only has affect on the search hits
     * (not aggregations). This filter is always executed as last filtering mechanism.
     */
    public ESQueryRequestBuilder setPostFilter(QueryBuilder postFilter) {
        sourceBuilder().postFilter(postFilter);
        return this;
    }

    /**
     * Sets a filter on the query executed that only applies to the search query
     * (and not aggs for example).
     */
    public ESQueryRequestBuilder setPostFilter(String postFilter) {
        sourceBuilder().postFilter(postFilter);
        return this;
    }

    /**
     * Sets a filter on the query executed that only applies to the search query
     * (and not aggs for example).
     */
    public ESQueryRequestBuilder setPostFilter(BytesReference postFilter) {
        sourceBuilder().postFilter(postFilter);
        return this;
    }

    /**
     * Sets a filter on the query executed that only applies to the search query
     * (and not aggs for example).
     */
    public ESQueryRequestBuilder setPostFilter(byte[] postFilter) {
        sourceBuilder().postFilter(postFilter);
        return this;
    }

    /**
     * Sets a filter on the query executed that only applies to the search query
     * (and not aggs for example).
     */
    public ESQueryRequestBuilder setPostFilter(byte[] postFilter, int postFilterOffset, int postFilterLength) {
        sourceBuilder().postFilter(postFilter, postFilterOffset, postFilterLength);
        return this;
    }

    /**
     * Sets a filter on the query executed that only applies to the search query
     * (and not aggs for example).
     */
    public ESQueryRequestBuilder setPostFilter(XContentBuilder postFilter) {
        sourceBuilder().postFilter(postFilter);
        return this;
    }

    /**
     * Sets a filter on the query executed that only applies to the search query
     * (and not aggs for example).
     */
    public ESQueryRequestBuilder setPostFilter(Map postFilter) {
        sourceBuilder().postFilter(postFilter);
        return this;
    }

    /**
     * Sets the minimum score below which docs will be filtered out.
     */
    public ESQueryRequestBuilder setMinScore(float minScore) {
        sourceBuilder().minScore(minScore);
        return this;
    }

    /**
     * From index to start the search from. Defaults to <tt>0</tt>.
     */
    public ESQueryRequestBuilder setFrom(int from) {
        sourceBuilder().from(from);
        return this;
    }

    /**
     * The number of search hits to return. Defaults to <tt>10</tt>.
     */
    public ESQueryRequestBuilder setSize(int size) {
        sourceBuilder().size(size);
        return this;
    }

    /**
     * Should each {@link org.elasticsearch.search.SearchHit} be returned with an
     * explanation of the hit (ranking).
     */
    public ESQueryRequestBuilder setExplain(boolean explain) {
        sourceBuilder().explain(explain);
        return this;
    }

    /**
     * Should each {@link org.elasticsearch.search.SearchHit} be returned with its
     * version.
     */
    public ESQueryRequestBuilder setVersion(boolean version) {
        sourceBuilder().version(version);
        return this;
    }

    /**
     * Sets the boost a specific index will receive when the query is executeed against it.
     *
     * @param index      The index to apply the boost against
     * @param indexBoost The boost to apply to the index
     */
    public ESQueryRequestBuilder addIndexBoost(String index, float indexBoost) {
        sourceBuilder().indexBoost(index, indexBoost);
        return this;
    }

    /**
     * The nodestats groups this request will be aggregated under.
     */
    public ESQueryRequestBuilder setStats(String... statsGroups) {
        sourceBuilder().stats(statsGroups);
        return this;
    }

    /**
     * Sets no fields to be loaded, resulting in only id and type to be returned per field.
     */
    public ESQueryRequestBuilder setNoFields() {
        sourceBuilder().noFields();
        return this;
    }

    /**
     * Indicates whether the response should contain the stored _source for every hit
     */
    public ESQueryRequestBuilder setFetchSource(boolean fetch) {
        sourceBuilder().fetchSource(fetch);
        return this;
    }

    /**
     * Indicate that _source should be returned with every hit, with an "include" and/or "exclude" set which can include simple wildcard
     * elements.
     *
     * @param include An optional include (optionally wildcarded) pattern to filter the returned _source
     * @param exclude An optional exclude (optionally wildcarded) pattern to filter the returned _source
     */
    public ESQueryRequestBuilder setFetchSource(@Nullable String include, @Nullable String exclude) {
        sourceBuilder().fetchSource(include, exclude);
        return this;
    }

    /**
     * Indicate that _source should be returned with every hit, with an "include" and/or "exclude" set which can include simple wildcard
     * elements.
     *
     * @param includes An optional list of include (optionally wildcarded) pattern to filter the returned _source
     * @param excludes An optional list of exclude (optionally wildcarded) pattern to filter the returned _source
     */
    public ESQueryRequestBuilder setFetchSource(@Nullable String[] includes, @Nullable String[] excludes) {
        sourceBuilder().fetchSource(includes, excludes);
        return this;
    }


    /**
     * Adds a field to load and return (note, it must be stored) as part of the search request.
     * If none are specified, the source of the document will be return.
     */
    public ESQueryRequestBuilder addField(String field) {
        sourceBuilder().field(field);
        return this;
    }

    /**
     * Adds a field data based field to load and return. The field does not have to be stored,
     * but its recommended to use non analyzed or numeric fields.
     *
     * @param name The field to get from the field data cache
     */
    public ESQueryRequestBuilder addFieldDataField(String name) {
        sourceBuilder().fieldDataField(name);
        return this;
    }

    /**
     * Adds a script based field to load and return. The field does not have to be stored,
     * but its recommended to use non analyzed or numeric fields.
     *
     * @param name   The name that will represent this value in the return hit
     * @param script The script to use
     */
    public ESQueryRequestBuilder addScriptField(String name, Script script) {
        sourceBuilder().scriptField(name, script);
        return this;
    }

    /**
     * Adds a sort against the given field name and the sort ordering.
     *
     * @param field The name of the field
     * @param order The sort ordering
     */
    public ESQueryRequestBuilder addSort(String field, SortOrder order) {
        sourceBuilder().sort(field, order);
        return this;
    }

    /**
     * Adds a generic sort builder.
     *
     * @see org.elasticsearch.search.sort.SortBuilders
     */
    public ESQueryRequestBuilder addSort(SortBuilder sort) {
        sourceBuilder().sort(sort);
        return this;
    }

    /**
     * Applies when sorting, and controls if scores will be tracked as well. Defaults to
     * <tt>false</tt>.
     */
    public ESQueryRequestBuilder setTrackScores(boolean trackScores) {
        sourceBuilder().trackScores(trackScores);
        return this;
    }

    /**
     * Adds the fields to load and return as part of the search request. If none are specified,
     * the source of the document will be returned.
     */
    public ESQueryRequestBuilder addFields(String... fields) {
        sourceBuilder().fields(fields);
        return this;
    }

    /**
     * Adds an get to the search operation.
     */
    public ESQueryRequestBuilder addAggregation(AbstractAggregationBuilder aggregation) {
        sourceBuilder().aggregation(aggregation);
        return this;
    }

    /**
     * Sets a raw (xcontent) binary representation of addAggregation to use.
     */
    public ESQueryRequestBuilder setAggregations(BytesReference aggregations) {
        sourceBuilder().aggregations(aggregations);
        return this;
    }

    /**
     * Sets a raw (xcontent) binary representation of addAggregation to use.
     */
    public ESQueryRequestBuilder setAggregations(byte[] aggregations) {
        sourceBuilder().aggregations(aggregations);
        return this;
    }

    /**
     * Sets a raw (xcontent) binary representation of addAggregation to use.
     */
    public ESQueryRequestBuilder setAggregations(byte[] aggregations, int aggregationsOffset, int aggregationsLength) {
        sourceBuilder().aggregations(aggregations, aggregationsOffset, aggregationsLength);
        return this;
    }

    /**
     * Sets a raw (xcontent) binary representation of addAggregation to use.
     */
    public ESQueryRequestBuilder setAggregations(XContentBuilder aggregations) {
        sourceBuilder().aggregations(aggregations);
        return this;
    }

    /**
     * Sets a raw (xcontent) binary representation of addAggregation to use.
     */
    public ESQueryRequestBuilder setAggregations(Map aggregations) {
        sourceBuilder().aggregations(aggregations);
        return this;
    }

    /**
     * Adds a field to be highlighted with default fragment size of 100 characters, and
     * default number of fragments of 5.
     *
     * @param name The field to highlight
     */
    public ESQueryRequestBuilder addHighlightedField(String name) {
        highlightBuilder().field(name);
        return this;
    }


    /**
     * Adds a field to be highlighted with a provided fragment size (in characters), and
     * default number of fragments of 5.
     *
     * @param name         The field to highlight
     * @param fragmentSize The size of a fragment in characters
     */
    public ESQueryRequestBuilder addHighlightedField(String name, int fragmentSize) {
        highlightBuilder().field(name, fragmentSize);
        return this;
    }

    /**
     * Adds a field to be highlighted with a provided fragment size (in characters), and
     * a provided (maximum) number of fragments.
     *
     * @param name              The field to highlight
     * @param fragmentSize      The size of a fragment in characters
     * @param numberOfFragments The (maximum) number of fragments
     */
    public ESQueryRequestBuilder addHighlightedField(String name, int fragmentSize, int numberOfFragments) {
        highlightBuilder().field(name, fragmentSize, numberOfFragments);
        return this;
    }

    /**
     * Adds a field to be highlighted with a provided fragment size (in characters),
     * a provided (maximum) number of fragments and an offset for the highlight.
     *
     * @param name              The field to highlight
     * @param fragmentSize      The size of a fragment in characters
     * @param numberOfFragments The (maximum) number of fragments
     */
    public ESQueryRequestBuilder addHighlightedField(String name, int fragmentSize, int numberOfFragments,
                                                     int fragmentOffset) {
        highlightBuilder().field(name, fragmentSize, numberOfFragments, fragmentOffset);
        return this;
    }

    /**
     * Adds a highlighted field.
     */
    public ESQueryRequestBuilder addHighlightedField(HighlightBuilder.Field field) {
        highlightBuilder().field(field);
        return this;
    }

    /**
     * Set a tag scheme that encapsulates a built in pre and post tags. The allows schemes
     * are <tt>styled</tt> and <tt>default</tt>.
     *
     * @param schemaName The tag scheme name
     */
    public ESQueryRequestBuilder setHighlighterTagsSchema(String schemaName) {
        highlightBuilder().tagsSchema(schemaName);
        return this;
    }

    public ESQueryRequestBuilder setHighlighterFragmentSize(Integer fragmentSize) {
        highlightBuilder().fragmentSize(fragmentSize);
        return this;
    }

    public ESQueryRequestBuilder setHighlighterNumOfFragments(Integer numOfFragments) {
        highlightBuilder().numOfFragments(numOfFragments);
        return this;
    }

    public ESQueryRequestBuilder setHighlighterFilter(Boolean highlightFilter) {
        highlightBuilder().highlightFilter(highlightFilter);
        return this;
    }

    /**
     * The encoder to set for highlighting
     */
    public ESQueryRequestBuilder setHighlighterEncoder(String encoder) {
        highlightBuilder().encoder(encoder);
        return this;
    }

    /**
     * Explicitly set the pre tags that will be used for highlighting.
     */
    public ESQueryRequestBuilder setHighlighterPreTags(String... preTags) {
        highlightBuilder().preTags(preTags);
        return this;
    }

    /**
     * Explicitly set the post tags that will be used for highlighting.
     */
    public ESQueryRequestBuilder setHighlighterPostTags(String... postTags) {
        highlightBuilder().postTags(postTags);
        return this;
    }

    /**
     * The order of fragments per field. By default, ordered by the order in the
     * highlighted text. Can be <tt>score</tt>, which then it will be ordered
     * by score of the fragments.
     */
    public ESQueryRequestBuilder setHighlighterOrder(String order) {
        highlightBuilder().order(order);
        return this;
    }

    public ESQueryRequestBuilder setHighlighterRequireFieldMatch(boolean requireFieldMatch) {
        highlightBuilder().requireFieldMatch(requireFieldMatch);
        return this;
    }

    public ESQueryRequestBuilder setHighlighterBoundaryMaxScan(Integer boundaryMaxScan) {
        highlightBuilder().boundaryMaxScan(boundaryMaxScan);
        return this;
    }

    public ESQueryRequestBuilder setHighlighterBoundaryChars(char[] boundaryChars) {
        highlightBuilder().boundaryChars(boundaryChars);
        return this;
    }

    /**
     * The highlighter type to use.
     */
    public ESQueryRequestBuilder setHighlighterType(String type) {
        highlightBuilder().highlighterType(type);
        return this;
    }

    public ESQueryRequestBuilder setHighlighterFragmenter(String fragmenter) {
        highlightBuilder().fragmenter(fragmenter);
        return this;
    }

    /**
     * Sets a query to be used for highlighting all fields instead of the search query.
     */
    public ESQueryRequestBuilder setHighlighterQuery(QueryBuilder highlightQuery) {
        highlightBuilder().highlightQuery(highlightQuery);
        return this;
    }

    /**
     * Sets the size of the fragment to return from the beginning of the field if there are no matches to
     * highlight and the field doesn't also define noMatchSize.
     *
     * @param noMatchSize integer to set or null to leave out of request.  default is null.
     * @return this builder for chaining
     */
    public ESQueryRequestBuilder setHighlighterNoMatchSize(Integer noMatchSize) {
        highlightBuilder().noMatchSize(noMatchSize);
        return this;
    }

    /**
     * Sets the maximum number of phrases the fvh will consider if the field doesn't also define phraseLimit.
     */
    public ESQueryRequestBuilder setHighlighterPhraseLimit(Integer phraseLimit) {
        highlightBuilder().phraseLimit(phraseLimit);
        return this;
    }

    public ESQueryRequestBuilder setHighlighterOptions(Map<String, Object> options) {
        highlightBuilder().options(options);
        return this;
    }

    /**
     * Forces to highlight fields based on the source even if fields are stored separately.
     */
    public ESQueryRequestBuilder setHighlighterForceSource(Boolean forceSource) {
        highlightBuilder().forceSource(forceSource);
        return this;
    }

    /**
     * Send the fields to be highlighted using a syntax that is specific about the order in which they should be highlighted.
     *
     * @return this for chaining
     */
    public ESQueryRequestBuilder setHighlighterExplicitFieldOrder(boolean explicitFieldOrder) {
        highlightBuilder().useExplicitFieldOrder(explicitFieldOrder);
        return this;
    }

    public ESQueryRequestBuilder addInnerHit(String name, InnerHitsBuilder.InnerHit innerHit) {
        innerHitsBuilder().addInnerHit(name, innerHit);
        return this;
    }

    /**
     * Delegates to {@link SuggestBuilder#setText(String)}.
     */
    public ESQueryRequestBuilder setSuggestText(String globalText) {
        suggestBuilder().setText(globalText);
        return this;
    }

    /**
     * Delegates to {@link SuggestBuilder#addSuggestion(SuggestBuilder.SuggestionBuilder)}.
     */
    public ESQueryRequestBuilder addSuggestion(SuggestBuilder.SuggestionBuilder<?> suggestion) {
        suggestBuilder().addSuggestion(suggestion);
        return this;
    }

    /**
     * Clears all rescorers on the builder and sets the first one.  To use multiple rescore windows use
     * {@link #addRescorer(RescoreBuilder.Rescorer, int)}.
     *
     * @param rescorer rescorer configuration
     * @return this for chaining
     */
    public ESQueryRequestBuilder setRescorer(RescoreBuilder.Rescorer rescorer) {
        sourceBuilder().clearRescorers();
        return addRescorer(rescorer);
    }

    /**
     * Clears all rescorers on the builder and sets the first one.  To use multiple rescore windows use
     * {@link #addRescorer(RescoreBuilder.Rescorer, int)}.
     *
     * @param rescorer rescorer configuration
     * @param window   rescore window
     * @return this for chaining
     */
    public ESQueryRequestBuilder setRescorer(RescoreBuilder.Rescorer rescorer, int window) {
        sourceBuilder().clearRescorers();
        return addRescorer(rescorer, window);
    }

    /**
     * Adds a new rescorer.
     *
     * @param rescorer rescorer configuration
     * @return this for chaining
     */
    public ESQueryRequestBuilder addRescorer(RescoreBuilder.Rescorer rescorer) {
        sourceBuilder().addRescorer(new RescoreBuilder().rescorer(rescorer));
        return this;
    }

    /**
     * Adds a new rescorer.
     *
     * @param rescorer rescorer configuration
     * @param window   rescore window
     * @return this for chaining
     */
    public ESQueryRequestBuilder addRescorer(RescoreBuilder.Rescorer rescorer, int window) {
        sourceBuilder().addRescorer(new RescoreBuilder().rescorer(rescorer).windowSize(window));
        return this;
    }

    /**
     * Clears all rescorers from the builder.
     *
     * @return this for chaining
     */
    public ESQueryRequestBuilder clearRescorers() {
        sourceBuilder().clearRescorers();
        return this;
    }

    /**
     * Sets the rescore window for all rescorers that don't specify a window
     * when added.
     *
     * @param window
     *            rescore window
     * @return this for chaining
     *
     * @deprecated use
     *             {@link #addRescorer(RescoreBuilder.Rescorer, int)}
     *             instead.
     */
    @Deprecated
    public ESQueryRequestBuilder setRescoreWindow(int window) {
        sourceBuilder().defaultRescoreWindowSize(window);
        return this;
    }


    /**
     * Returns the internal search source builder used to construct the request.
     */
    public SearchSourceBuilder internalBuilder() {
        return sourceBuilder();
    }

    @Override
    public String toString() {
        if (sourceBuilder != null) {
            return sourceBuilder.toString();
        }
        if (request.source() != null) {
            try {
                return XContentHelper.convertToJson(request.source().toBytesArray(), false, true);
            } catch (Exception e) {
                return "{ \"error\" : \"" + ExceptionsHelper.detailedMessage(e) + "\"}";
            }
        }
        return new SearchSourceBuilder().toString();
    }

    @Override
    public ESQueryRequest request() {
        if (sourceBuilder != null) {
            request.source(sourceBuilder());
        }
        return request;
    }

    @Override
    protected ESQueryRequest beforeExecute(ESQueryRequest request) {
        if (sourceBuilder != null) {
            request.source(sourceBuilder());
        }
        return request;
    }

    private SearchSourceBuilder sourceBuilder() {
        if (sourceBuilder == null) {
            sourceBuilder = new SearchSourceBuilder();
        }
        return sourceBuilder;
    }

    private HighlightBuilder highlightBuilder() {
        return sourceBuilder().highlighter();
    }

    private InnerHitsBuilder innerHitsBuilder() {
        return sourceBuilder().innerHitsBuilder();
    }

    private SuggestBuilder suggestBuilder() {
        return sourceBuilder().suggest();
    }
}
