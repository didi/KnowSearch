package com.didi.arius.gateway.elasticsearch.client.gateway.document;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionRequest;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionResponse;
import com.didi.arius.gateway.elasticsearch.client.model.RestRequest;
import com.didi.arius.gateway.elasticsearch.client.model.RestResponse;
import com.didi.arius.gateway.elasticsearch.client.utils.RequestConverters;
import org.apache.http.client.methods.HttpPost;
import org.elasticsearch.ElasticsearchParseException;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.ValidateActions;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.lucene.uid.Versions;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.search.fetch.source.FetchSourceContext;

import java.io.IOException;
import java.util.*;

public class ESMultiGetRequest extends ESActionRequest<ESMultiGetRequest> {
    private Map<String, String> params = new HashMap<>();
    private static final String FIELDS = "fields";
    private static final String STORED_FIELDS = "stored_fields";
    private static final String SOURCE = "_source";

    /**
     * A single get item.
     */
    public static class Item {
        private String index;
        private String type;
        private String id;
        private String routing;
        private String[] fields;
        private String[] storedFields;
        private long version = Versions.MATCH_ANY;
        private VersionType versionType = VersionType.INTERNAL;
        private FetchSourceContext fetchSourceContext;

        public Item() {

        }

        /**
         * Constructs a single get item.
         *
         * @param index The index name
         * @param type  The type (can be null)
         * @param id    The id
         */
        public Item(String index, @Nullable String type, String id) {
            this.index = index;
            this.type = type;
            this.id = id;
        }

        public String index() {
            return this.index;
        }

        public Item index(String index) {
            this.index = index;
            return this;
        }

        public String type() {
            return this.type;
        }

        public Item type(String type) {
            this.type = type;
            return this;
        }

        public String id() {
            return this.id;
        }

        /**
         * The routing associated with this document.
         */
        public Item routing(String routing) {
            this.routing = routing;
            return this;
        }

        public String routing() {
            return this.routing;
        }

        public Item parent(String parent) {
            if (routing == null) {
                this.routing = parent;
            }
            return this;
        }

        public Item fields(String... fields) {
            this.fields = fields;
            return this;
        }

        public String[] fields() {
            return this.fields;
        }

        public Item storedFields(String... storedFields) {
            this.storedFields = storedFields;
            return this;
        }

        public String[] storedFields() {
            return this.storedFields;
        }

        public long version() {
            return version;
        }

        public Item version(long version) {
            this.version = version;
            return this;
        }

        public VersionType versionType() {
            return versionType;
        }

        public Item versionType(VersionType versionType) {
            this.versionType = versionType;
            return this;
        }

        public FetchSourceContext fetchSourceContext() {
            return this.fetchSourceContext;
        }

        /**
         * Allows setting the {@link FetchSourceContext} for this request, controlling if and how _source should be returned.
         */
        public Item fetchSourceContext(FetchSourceContext fetchSourceContext) {
            this.fetchSourceContext = fetchSourceContext;
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Item)) return false;

            Item item = (Item) o;

            if (version != item.version) return false;
            if (fetchSourceContext != null ? !fetchSourceContext.equals(item.fetchSourceContext) : item.fetchSourceContext != null)
                return false;
            if (!Arrays.equals(fields, item.fields)) return false;
            if (!id.equals(item.id)) return false;
            if (!index.equals(item.index)) return false;
            if (routing != null ? !routing.equals(item.routing) : item.routing != null) return false;
            if (type != null ? !type.equals(item.type) : item.type != null) return false;
            if (versionType != item.versionType) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = index.hashCode();
            result = 31 * result + (type != null ? type.hashCode() : 0);
            result = 31 * result + id.hashCode();
            result = 31 * result + (routing != null ? routing.hashCode() : 0);
            result = 31 * result + (fields != null ? Arrays.hashCode(fields) : 0);
            result = 31 * result + (int) (version ^ (version >>> 32));
            result = 31 * result + versionType.hashCode();
            result = 31 * result + (fetchSourceContext != null ? fetchSourceContext.hashCode() : 0);
            return result;
        }
    }

    String preference;
    Boolean realtime;
    String refresh;
    private boolean ignoreErrorsOnGeneratedFields = false;

    List<Item> items = new ArrayList<>();

    public ESMultiGetRequest() {
        // pass
    }

    @Override
    public RestRequest toRequest() throws Exception {
        RestRequest request = new RestRequest(HttpPost.METHOD_NAME, "/_mget");
        request.setParams(params);

        RequestConverters.Params parameters = new RequestConverters.Params(request);
        parameters.withPreference(this.preference());
        parameters.withRealtime(this.realtime());
        parameters.withRefresh(this.refresh());

        JSONArray jsonArray = new JSONArray();
        for (Item item : items) {
            JSONObject itemJson = new JSONObject();
            itemJson.put("_index", item.index);
            itemJson.put("_type", item.type);
            itemJson.put("_id", item.id);

            if (item.routing != null) {
                itemJson.put("routing", item.routing);
            }

            if (item.version != Versions.MATCH_ANY) {
                itemJson.put("version", item.version);
            }

            if (item.versionType != null) {
                itemJson.put("version_type", item.versionType.name().toLowerCase(Locale.ROOT));
            }

            if (item.fields != null) {
                itemJson.put(FIELDS, item.fields);
            }

            if (item.storedFields != null) {
                itemJson.put(STORED_FIELDS, item.storedFields);
            }

            if (item.fetchSourceContext != null) {
                fetchSource(item, itemJson);
            }

            jsonArray.add(itemJson);
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("docs", jsonArray);
        request.setBody(jsonObject.toJSONString());

        return request;
    }

    private void fetchSource(Item item, JSONObject itemJson) {
        if (!item.fetchSourceContext.fetchSource()) {
            itemJson.put(SOURCE, Boolean.FALSE.toString());
        } else {
            JSONObject sourceObj = new JSONObject();
            if (item.fetchSourceContext.includes() != null && item.fetchSourceContext.includes().length > 0) {
                JSONArray includes = new JSONArray();
                for (String include : item.fetchSourceContext.includes()) {
                    includes.add(include);
                }
                sourceObj.put("includes", includes);
            }

            if (item.fetchSourceContext.excludes() != null && item.fetchSourceContext.excludes().length > 0) {
                JSONArray excludes = new JSONArray();
                for (String exclude : item.fetchSourceContext.excludes()) {
                    excludes.add(exclude);
                }
                sourceObj.put("excludes", excludes);
            }

            itemJson.put(SOURCE, sourceObj);
        }
    }

    @Override
    public ESActionResponse toResponse(RestResponse response) throws Exception {
        XContentParser parser = JsonXContent.jsonXContent.createParser(response.getResponseStream());
        return ESMultiGetResponse.fromXContent(parser);
    }

    public List<Item> getItems() {
        return this.items;
    }

    public ESMultiGetRequest add(Item item) {
        items.add(item);
        return this;
    }

    public ESMultiGetRequest add(String index, @Nullable String type, String id) {
        items.add(new Item(index, type, id));
        return this;
    }

    @Override
    public ActionRequestValidationException validate() {
        ActionRequestValidationException validationException = null;
        if (items.isEmpty()) {
            validationException = ValidateActions.addValidationError("no documents to get", validationException);
        } else {
            for (int i = 0; i < items.size(); i++) {
                Item item = items.get(i);
                if (item.index() == null) {
                    validationException = ValidateActions.addValidationError("index is missing for doc " + i, validationException);
                }
                if (item.id() == null) {
                    validationException = ValidateActions.addValidationError("id is missing for doc " + i, validationException);
                }
            }
        }
        return validationException;
    }

    /**
     * Sets the preference to execute the search. Defaults to randomize across shards. Can be set to
     * <tt>_local</tt> to prefer local shards, <tt>_primary</tt> to execute only on primary shards, or
     * a custom value, which guarantees that the same order will be used across different requests.
     */
    public ESMultiGetRequest preference(String preference) {
        this.preference = preference;
        return this;
    }

    public String preference() {
        return this.preference;
    }

    public boolean realtime() {
        return this.realtime == null || this.realtime;
    }

    public ESMultiGetRequest realtime(Boolean realtime) {
        this.realtime = realtime;
        return this;
    }

    public String refresh() {
        return this.refresh;
    }

    public ESMultiGetRequest refresh(String refresh) {
        this.refresh = refresh;
        return this;
    }


    public ESMultiGetRequest ignoreErrorsOnGeneratedFields(boolean ignoreErrorsOnGeneratedFields) {
        this.ignoreErrorsOnGeneratedFields = ignoreErrorsOnGeneratedFields;
        return this;
    }

    public ESMultiGetRequest add(@Nullable String defaultIndex, @Nullable String defaultType, @Nullable String[] defaultFields, @Nullable FetchSourceContext defaultFetchSource, byte[] data, int from, int length) throws Exception {
        return add(defaultIndex, defaultType, defaultFields, defaultFetchSource, new BytesArray(data, from, length), true);
    }

    public ESMultiGetRequest add(@Nullable String defaultIndex, @Nullable String defaultType, @Nullable String[] defaultFields, @Nullable FetchSourceContext defaultFetchSource, BytesReference data) throws Exception {
        return add(defaultIndex, defaultType, defaultFields, defaultFetchSource, data, true);
    }

    public ESMultiGetRequest add(@Nullable String defaultIndex, @Nullable String defaultType, @Nullable String[] defaultFields, @Nullable FetchSourceContext defaultFetchSource, BytesReference data, boolean allowExplicitIndex) throws Exception {
        return add(defaultIndex, defaultType, defaultFields, defaultFetchSource, null, data, allowExplicitIndex);
    }

    public ESMultiGetRequest add(@Nullable String defaultIndex, @Nullable String defaultType, @Nullable String[] defaultFields, @Nullable FetchSourceContext defaultFetchSource, @Nullable String defaultRouting, BytesReference data, boolean allowExplicitIndex) throws IOException {
        try (XContentParser parser = XContentFactory.xContent(data).createParser(data)) {
            XContentParser.Token token;
            String currentFieldName = null;
            while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
                if (token == XContentParser.Token.FIELD_NAME) {
                    currentFieldName = parser.currentName();
                } else if (token == XContentParser.Token.START_ARRAY) {
                    if ("docs".equals(currentFieldName)) {
                        parseDocuments(parser, this.items, defaultIndex, defaultType, defaultFields, defaultFetchSource, defaultRouting, allowExplicitIndex);
                    } else if ("ids".equals(currentFieldName)) {
                        parseIds(parser, this.items, defaultIndex, defaultType, defaultFields, defaultFetchSource, defaultRouting);
                    }
                }
            }
        }
        return this;
    }

    public static void parseDocuments(XContentParser parser, List<Item> items, @Nullable String defaultIndex, @Nullable String defaultType, @Nullable String[] defaultFields, @Nullable FetchSourceContext defaultFetchSource, @Nullable String defaultRouting, boolean allowExplicitIndex) throws IOException {
        String currentFieldName = null;
        XContentParser.Token token;
        while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
            if (token != XContentParser.Token.START_OBJECT) {
                throw new IllegalArgumentException("docs array element should include an object");
            }
            String index = defaultIndex;
            String type = defaultType;
            String id = null;
            String routing = defaultRouting;
            String parent = null;
            List<String> fields = null;
            List<String> storedFields = null;
            long version = Versions.MATCH_ANY;
            VersionType versionType = VersionType.INTERNAL;

            FetchSourceContext fetchSourceContext = null;

            while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
                if (token == XContentParser.Token.FIELD_NAME) {
                    currentFieldName = parser.currentName();
                } else if (token.isValue()) {
                    if ("_index".equals(currentFieldName)) {
                        if (!allowExplicitIndex) {
                            throw new IllegalArgumentException("explicit index in multi get is not allowed");
                        }
                        index = parser.text();
                    } else if ("_type".equals(currentFieldName)) {
                        type = parser.text();
                    } else if ("_id".equals(currentFieldName)) {
                        id = parser.text();
                    } else if ("_routing".equals(currentFieldName) || "routing".equals(currentFieldName)) {
                        routing = parser.text();
                    } else if ("_parent".equals(currentFieldName) || "parent".equals(currentFieldName)) {
                        parent = parser.text();
                    } else if (FIELDS.equals(currentFieldName)) {
                        fields = new ArrayList<>();
                        fields.add(parser.text());
                    } else if (STORED_FIELDS.equals(currentFieldName)) {
                        storedFields = new ArrayList<>();
                        storedFields.add(parser.text());
                    } else if ("_version".equals(currentFieldName) || "version".equals(currentFieldName)) {
                        version = parser.longValue();
                    } else if ("_version_type".equals(currentFieldName) || "_versionType".equals(currentFieldName) || "version_type".equals(currentFieldName) || "versionType".equals(currentFieldName)) {
                        versionType = VersionType.fromString(parser.text());
                    } else if (SOURCE.equals(currentFieldName)) {
                        if (parser.isBooleanValue()) {
                            fetchSourceContext = new FetchSourceContext(parser.booleanValue());
                        } else if (token == XContentParser.Token.VALUE_STRING) {
                            fetchSourceContext = new FetchSourceContext(new String[]{parser.text()});
                        } else {
                            throw new ElasticsearchParseException("illegal type for _source: [{}]", token);
                        }
                    }
                } else if (token == XContentParser.Token.START_ARRAY) {
                    if (FIELDS.equals(currentFieldName)) {
                        fields = new ArrayList<>();
                        while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
                            fields.add(parser.text());
                        }
                    } else if (STORED_FIELDS.equals(currentFieldName)) {
                        storedFields = new ArrayList<>();
                        while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
                            storedFields.add(parser.text());
                        }
                    } else if (SOURCE.equals(currentFieldName)) {
                        ArrayList<String> includes = new ArrayList<>();
                        while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
                            includes.add(parser.text());
                        }
                        fetchSourceContext = new FetchSourceContext(includes.toArray(Strings.EMPTY_ARRAY));
                    }

                } else if (token == XContentParser.Token.START_OBJECT && SOURCE.equals(currentFieldName)) {
                    List<String> currentList = new ArrayList<>(), includes = null, excludes = null;

                    while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
                        if (token == XContentParser.Token.FIELD_NAME) {
                            currentFieldName = parser.currentName();
                            if ("includes".equals(currentFieldName) || "include".equals(currentFieldName)) {
                                currentList = includes != null ? includes : (includes = new ArrayList<>(2));
                            } else if ("excludes".equals(currentFieldName) || "exclude".equals(currentFieldName)) {
                                currentList = excludes != null ? excludes : (excludes = new ArrayList<>(2));
                            } else {
                                throw new ElasticsearchParseException("source definition may not contain [{}]", parser.text());
                            }
                        } else if (token == XContentParser.Token.START_ARRAY) {
                            while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
                                currentList.add(parser.text());
                            }
                        } else if (token.isValue()) {
                            currentList.add(parser.text());
                        } else {
                            throw new ElasticsearchParseException("unexpected token while parsing source settings");
                        }
                    }

                    fetchSourceContext = new FetchSourceContext(
                            includes == null ? Strings.EMPTY_ARRAY : includes.toArray(new String[includes.size()]),
                            excludes == null ? Strings.EMPTY_ARRAY : excludes.toArray(new String[excludes.size()]));
                }
            }
            String[] aFields;
            if (fields != null) {
                aFields = fields.toArray(new String[fields.size()]);
            } else {
                aFields = defaultFields;
            }

            String[] aStoredFields = null;
            if (storedFields != null) {
                aStoredFields = storedFields.toArray(new String[storedFields.size()]);
            }

            items.add(new Item(index, type, id).routing(routing).fields(aFields).storedFields(aStoredFields).parent(parent).version(version).versionType(versionType)
                    .fetchSourceContext(fetchSourceContext == null ? defaultFetchSource : fetchSourceContext));
        }
    }

    public static void parseDocuments(XContentParser parser, List<Item> items) throws IOException {
        parseDocuments(parser, items, null, null, null, null, null, true);
    }

    public static void parseIds(XContentParser parser, List<Item> items, @Nullable String defaultIndex, @Nullable String defaultType, @Nullable String[] defaultFields, @Nullable FetchSourceContext defaultFetchSource, @Nullable String defaultRouting) throws IOException {
        XContentParser.Token token;
        while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
            if (!token.isValue()) {
                throw new IllegalArgumentException("ids array element should only contain ids");
            }
            items.add(new Item(defaultIndex, defaultType, parser.text()).fields(defaultFields).fetchSourceContext(defaultFetchSource).routing(defaultRouting));
        }
    }

    public static void parseIds(XContentParser parser, List<Item> items) throws IOException {
        parseIds(parser, items, null, null, null, null, null);
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

}
