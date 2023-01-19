/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.watcher.support.search;

import org.apache.logging.log4j.LogManager;
import org.elasticsearch.ElasticsearchParseException;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.logging.DeprecationLogger;
import org.elasticsearch.common.xcontent.ToXContentObject;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * A {@link WatcherSearchTemplateRequest} contains the search request and the eventual template that will
 * be rendered as a script by {@link WatcherSearchTemplateService} before being executed.
 */
public class WatcherSearchTemplateRequest implements ToXContentObject {

    private final String[] indices;
    private final String[] types;
    private final SearchType searchType;
    private final IndicesOptions indicesOptions;
    private final Script template;
    private final BytesReference searchSource;
    private boolean restTotalHitsAsInt = true;

    private static final DeprecationLogger deprecationLogger =
        new DeprecationLogger(LogManager.getLogger(WatcherSearchTemplateRequest.class));
    public static final String TYPES_DEPRECATION_MESSAGE = "[types removal] Specifying types in a watcher search request is deprecated.";

    public WatcherSearchTemplateRequest(String[] indices, String[] types, SearchType searchType, IndicesOptions indicesOptions,
                                        BytesReference searchSource) {
        this.indices = indices;
        this.types = types;
        this.searchType = searchType;
        this.indicesOptions = indicesOptions;
        // Here we convert a watch search request body into an inline search template,
        // this way if any Watcher related context variables are used, they will get resolved.
        this.template = new Script(ScriptType.INLINE, Script.DEFAULT_TEMPLATE_LANG, searchSource.utf8ToString(), Collections.emptyMap());
        this.searchSource = BytesArray.EMPTY;
    }

    public WatcherSearchTemplateRequest(String[] indices, String[] types, SearchType searchType, IndicesOptions indicesOptions,
                                        Script template) {
        this.indices = indices;
        this.types = types;
        this.searchType = searchType;
        this.indicesOptions = indicesOptions;
        this.template = template;
        this.searchSource = BytesArray.EMPTY;
    }

    public WatcherSearchTemplateRequest(WatcherSearchTemplateRequest original, BytesReference source) {
        this.indices = original.indices;
        this.types = original.types;
        this.searchType = original.searchType;
        this.indicesOptions = original.indicesOptions;
        this.searchSource = source;
        this.template = original.template;
        this.restTotalHitsAsInt = original.restTotalHitsAsInt;
    }

    private WatcherSearchTemplateRequest(String[] indices, String[] types, SearchType searchType, IndicesOptions indicesOptions,
                                 BytesReference searchSource, Script template) {
        this.indices = indices;
        this.types = types;
        this.searchType = searchType;
        this.indicesOptions = indicesOptions;
        this.template = template;
        this.searchSource = searchSource;
    }

    @Nullable
    public Script getTemplate() {
        return template;
    }

    public String[] getIndices() {
        return indices;
    }

    public String[] getTypes() {
        return types;
    }

    public SearchType getSearchType() {
        return searchType;
    }

    public IndicesOptions getIndicesOptions() {
        return indicesOptions;
    }

    public boolean isRestTotalHitsAsint() {
        return restTotalHitsAsInt;
    }

    /**
     * Indicates whether the total hits in the response should be
     * serialized as number (<code>true</code>) or as an object (<code>false</code>).
     * Defaults to false.
     */
    public void setRestTotalHitsAsInt(boolean value) {
        this.restTotalHitsAsInt = value;
    }

    public BytesReference getSearchSource() {
        return searchSource;
    }

    public Script getOrCreateTemplate() {
        if (template != null) {
            return template;
        } else {
            return new Script(ScriptType.INLINE, Script.DEFAULT_TEMPLATE_LANG, searchSource.utf8ToString(), Collections.emptyMap());
        }
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();
        if (searchType != null) {
            builder.field(SEARCH_TYPE_FIELD.getPreferredName(), searchType.toString().toLowerCase(Locale.ENGLISH));
        }
        if (indices != null) {
            builder.array(INDICES_FIELD.getPreferredName(), indices);
        }
        if (types != null) {
            builder.array(TYPES_FIELD.getPreferredName(), types);
        }
        if (restTotalHitsAsInt) {
            builder.field(REST_TOTAL_HITS_AS_INT_FIELD.getPreferredName(), restTotalHitsAsInt);
        }
        if (searchSource != null && searchSource.length() > 0) {
            try (InputStream stream = searchSource.streamInput()) {
                builder.rawField(BODY_FIELD.getPreferredName(), stream);
            }
        }
        if (indicesOptions != DEFAULT_INDICES_OPTIONS) {
            builder.startObject(INDICES_OPTIONS_FIELD.getPreferredName());
            String value;
            if (indicesOptions.expandWildcardsClosed() && indicesOptions.expandWildcardsOpen()) {
                value = "all";
            } else if (indicesOptions.expandWildcardsOpen()) {
                value = "open";
            } else if (indicesOptions.expandWildcardsClosed()) {
                value = "closed";
            } else {
                value = "none";
            }
            builder.field(EXPAND_WILDCARDS_FIELD.getPreferredName(), value);
            builder.field(IGNORE_UNAVAILABLE_FIELD.getPreferredName(), indicesOptions.ignoreUnavailable());
            builder.field(ALLOW_NO_INDICES_FIELD.getPreferredName(), indicesOptions.allowNoIndices());
            builder.endObject();
        }
        if (template != null) {
            builder.field(TEMPLATE_FIELD.getPreferredName(), template);
        }
        return builder.endObject();
    }


    /**
     * Reads a new watcher search request instance for the specified parser.
     */
    public static WatcherSearchTemplateRequest fromXContent(XContentParser parser, SearchType searchType) throws IOException {
        List<String> indices = new ArrayList<>();
        List<String> types = new ArrayList<>();
        IndicesOptions indicesOptions = DEFAULT_INDICES_OPTIONS;
        BytesReference searchSource = null;
        Script template = null;
        // TODO this is to retain BWC compatibility in 7.0 and can be removed for 8.0
        boolean totalHitsAsInt = true;

        XContentParser.Token token;
        String currentFieldName = null;
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (token == XContentParser.Token.START_ARRAY) {
                if (INDICES_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
                        if (token == XContentParser.Token.VALUE_STRING) {
                            indices.add(parser.textOrNull());
                        } else {
                            throw new ElasticsearchParseException("could not read search request. expected string values in [" +
                                    currentFieldName + "] field, but instead found [" + token + "]");
                        }
                    }
                } else if (TYPES_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    deprecationLogger.deprecatedAndMaybeLog("watcher_search_input", TYPES_DEPRECATION_MESSAGE);
                    while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
                        if (token == XContentParser.Token.VALUE_STRING) {
                            types.add(parser.textOrNull());
                        } else {
                            throw new ElasticsearchParseException("could not read search request. expected string values in [" +
                                    currentFieldName + "] field, but instead found [" + token + "]");
                        }
                    }
                } else {
                    throw new ElasticsearchParseException("could not read search request. unexpected array field [" +
                            currentFieldName + "]");
                }
            } else if (token == XContentParser.Token.START_OBJECT) {
                if (BODY_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    try (XContentBuilder builder = XContentFactory.jsonBuilder()) {
                        builder.copyCurrentStructure(parser);
                        searchSource = BytesReference.bytes(builder);
                    }
                } else if (INDICES_OPTIONS_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    boolean expandOpen = DEFAULT_INDICES_OPTIONS.expandWildcardsOpen();
                    boolean expandClosed = DEFAULT_INDICES_OPTIONS.expandWildcardsClosed();
                    boolean allowNoIndices = DEFAULT_INDICES_OPTIONS.allowNoIndices();
                    boolean ignoreUnavailable = DEFAULT_INDICES_OPTIONS.ignoreUnavailable();
                    while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
                        if (token == XContentParser.Token.FIELD_NAME) {
                            currentFieldName = parser.currentName();
                        } else if (token.isValue()) {
                            if (EXPAND_WILDCARDS_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                                switch (parser.text()) {
                                    case "all":
                                        expandOpen = true;
                                        expandClosed = true;
                                        break;
                                    case "open":
                                        expandOpen = true;
                                        expandClosed = false;
                                        break;
                                    case "closed":
                                        expandOpen = false;
                                        expandClosed = true;
                                        break;
                                    case "none":
                                        expandOpen = false;
                                        expandClosed = false;
                                        break;
                                    default:
                                        throw new ElasticsearchParseException("could not read search request. unknown value [" +
                                                parser.text() + "] for [" + currentFieldName + "] field ");
                                }
                            } else if (IGNORE_UNAVAILABLE_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                                ignoreUnavailable = parser.booleanValue();
                            } else if (ALLOW_NO_INDICES_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                                allowNoIndices = parser.booleanValue();
                            } else {
                                throw new ElasticsearchParseException("could not read search request. unexpected index option [" +
                                        currentFieldName + "]");
                            }
                        } else {
                            throw new ElasticsearchParseException("could not read search request. unexpected object field [" +
                                    currentFieldName + "]");
                        }
                    }
                    indicesOptions = IndicesOptions.fromOptions(ignoreUnavailable, allowNoIndices, expandOpen, expandClosed,
                            DEFAULT_INDICES_OPTIONS);
                } else if (TEMPLATE_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    template = Script.parse(parser, Script.DEFAULT_TEMPLATE_LANG);
                } else {
                    throw new ElasticsearchParseException("could not read search request. unexpected object field [" +
                            currentFieldName + "]");
                }
            } else if (token == XContentParser.Token.VALUE_STRING) {
                if (INDICES_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    String indicesStr = parser.text();
                    indices.addAll(Arrays.asList(Strings.delimitedListToStringArray(indicesStr, ",", " \t")));
                } else if (TYPES_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    deprecationLogger.deprecatedAndMaybeLog("watcher_search_input", TYPES_DEPRECATION_MESSAGE);
                    String typesStr = parser.text();
                    types.addAll(Arrays.asList(Strings.delimitedListToStringArray(typesStr, ",", " \t")));
                } else if (SEARCH_TYPE_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    searchType = SearchType.fromString(parser.text().toLowerCase(Locale.ROOT));
                } else if (REST_TOTAL_HITS_AS_INT_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    totalHitsAsInt = parser.booleanValue();
                } else {
                    throw new ElasticsearchParseException("could not read search request. unexpected string field [" +
                            currentFieldName + "]");
                }
            } else if (token == XContentParser.Token.VALUE_BOOLEAN) {
                if (REST_TOTAL_HITS_AS_INT_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    totalHitsAsInt = parser.booleanValue();
                } else {
                    throw new ElasticsearchParseException("could not read search request. unexpected boolean field [" +
                        currentFieldName + "]");
                }
            } else {
                throw new ElasticsearchParseException("could not read search request. unexpected token [" + token + "]");
            }
        }

        if (searchSource == null) {
            searchSource = BytesArray.EMPTY;
        }

        WatcherSearchTemplateRequest request = new WatcherSearchTemplateRequest(indices.toArray(new String[0]),
            types.size() == 0 ? null : types.toArray(new String[0]), searchType, indicesOptions, searchSource, template);
        request.setRestTotalHitsAsInt(totalHitsAsInt);
        return request;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WatcherSearchTemplateRequest other = (WatcherSearchTemplateRequest) o;
        return Arrays.equals(indices, other.indices) &&
                Arrays.equals(types, other.types) &&
                Objects.equals(searchType, other.searchType) &&
                Objects.equals(indicesOptions, other.indicesOptions) &&
                Objects.equals(searchSource, other.searchSource) &&
                Objects.equals(template, other.template) &&
                Objects.equals(restTotalHitsAsInt, other.restTotalHitsAsInt);

    }

    @Override
    public int hashCode() {
        return Objects.hash(indices, types, searchType, indicesOptions, searchSource, template, restTotalHitsAsInt);
    }

    private static final ParseField INDICES_FIELD = new ParseField("indices");
    private static final ParseField TYPES_FIELD = new ParseField("types");
    private static final ParseField BODY_FIELD = new ParseField("body");
    private static final ParseField SEARCH_TYPE_FIELD = new ParseField("search_type");
    private static final ParseField INDICES_OPTIONS_FIELD = new ParseField("indices_options");
    private static final ParseField EXPAND_WILDCARDS_FIELD = new ParseField("expand_wildcards");
    private static final ParseField IGNORE_UNAVAILABLE_FIELD = new ParseField("ignore_unavailable");
    private static final ParseField ALLOW_NO_INDICES_FIELD = new ParseField("allow_no_indices");
    private static final ParseField TEMPLATE_FIELD = new ParseField("template");
    private static final ParseField REST_TOTAL_HITS_AS_INT_FIELD = new ParseField("rest_total_hits_as_int");

    public static final IndicesOptions DEFAULT_INDICES_OPTIONS = IndicesOptions.lenientExpandOpen();
}
