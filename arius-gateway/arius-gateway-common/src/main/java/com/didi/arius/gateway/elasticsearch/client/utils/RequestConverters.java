package com.didi.arius.gateway.elasticsearch.client.utils;

import com.didi.arius.gateway.elasticsearch.client.model.RestRequest;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.lucene.uid.Versions;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.search.fetch.source.FetchSourceContext;
import org.elasticsearch.tasks.TaskId;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.StringJoiner;

public class RequestConverters {
    static final XContentType REQUEST_BODY_CONTENT_TYPE = XContentType.JSON;

    private RequestConverters() {
        // Contains only status utility methods
    }

    public static String endpoint(String index, String type, String id) {
        return new EndpointBuilder().addPathPart(index, type, id).build();
    }

    public static String endpoint(String index, String type, String id, String endpoint) {
        return new EndpointBuilder().addPathPart(index, type, id).addPathPartAsIs(endpoint).build();
    }

    public static String endpoint(String[] indices) {
        return new EndpointBuilder().addCommaSeparatedPathParts(indices).build();
    }

    public static String endpoint(String[] indices, String endpoint) {
        return new EndpointBuilder().addCommaSeparatedPathParts(indices).addPathPartAsIs(endpoint).build();
    }

    public static String endpoint(String[] indices, String[] types, String endpoint) {
        return new EndpointBuilder().addCommaSeparatedPathParts(indices).addCommaSeparatedPathParts(types)
                .addPathPartAsIs(endpoint).build();
    }

    public static String endpoint(String[] indices, String endpoint, String[] suffixes) {
        return new EndpointBuilder().addCommaSeparatedPathParts(indices).addPathPartAsIs(endpoint)
                .addCommaSeparatedPathParts(suffixes).build();
    }

    public static String endpoint(String[] indices, String endpoint, String type) {
        return new EndpointBuilder().addCommaSeparatedPathParts(indices).addPathPartAsIs(endpoint).addPathPart(type).build();
    }

    /**
     * Utility class to help with common parameter names and patterns. Wraps
     * a {@link RestRequest} and adds the parameters to it directly.
     */
    public static class Params {
        private final RestRequest request;

        public Params(RestRequest request) {
            this.request = request;
        }

        public Params putParam(String name, String value) {
            if (Strings.hasLength(value)) {
                request.addParam(name, value);
            }
            return this;
        }

        public Params putParam(String key, TimeValue value) {
            if (value != null) {
                return putParam(key, value.toString());
            }
            return this;
        }

        public Params withDocAsUpsert(boolean docAsUpsert) {
            if (docAsUpsert) {
                return putParam("doc_as_upsert", Boolean.TRUE.toString());
            }
            return this;
        }

        public Params withFetchSourceContext(FetchSourceContext fetchSourceContext) {
            if (fetchSourceContext != null) {
                if (!fetchSourceContext.fetchSource()) {
                    putParam("_source", Boolean.FALSE.toString());
                }
                if (fetchSourceContext.includes() != null && fetchSourceContext.includes().length > 0) {
                    putParam("_source_include", String.join(",", fetchSourceContext.includes()));
                }
                if (fetchSourceContext.excludes() != null && fetchSourceContext.excludes().length > 0) {
                    putParam("_source_exclude", String.join(",", fetchSourceContext.excludes()));
                }
            }
            return this;
        }

        public Params withFields(String[] fields) {
            if (fields != null && fields.length > 0) {
                return putParam("fields", String.join(",", fields));
            }
            return this;
        }

        public Params withMasterTimeout(TimeValue masterTimeout) {
            return putParam("master_timeout", masterTimeout);
        }

        public Params withParent(String parent) {
            return putParam("parent", parent);
        }

        public Params withPipeline(String pipeline) {
            return putParam("pipeline", pipeline);
        }

        public Params withPreference(String preference) {
            return putParam("preference", preference);
        }

        public Params withRealtime(boolean realtime) {
            if (!realtime) {
                return putParam("realtime", Boolean.FALSE.toString());
            }
            return this;
        }

        public Params withRefresh(String refresh) {
            return putParam("refresh", refresh);
        }

        public Params withRetryOnConflict(int retryOnConflict) {
            if (retryOnConflict > 0) {
                return putParam("retry_on_conflict", String.valueOf(retryOnConflict));
            }
            return this;
        }

        public Params withConsistency(String consistency) {
            return putParam("consistency", consistency);
        }

        public Params withWaitForActiveShards(String waitForActiveShards) {
            return putParam("wait_for_active_shards", waitForActiveShards);
        }

        public Params withRouting(String routing) {
            return putParam("routing", routing);
        }

        public Params withStoredFields(String[] storedFields) {
            if (storedFields != null && storedFields.length > 0) {
                return putParam("stored_fields", String.join(",", storedFields));
            }
            return this;
        }

        public Params withTimeout(TimeValue timeout) {
            return putParam("timeout", timeout);
        }

        public Params withUpdateAllTypes(boolean updateAllTypes) {
            if (updateAllTypes) {
                return putParam("update_all_types", Boolean.TRUE.toString());
            }
            return this;
        }

        public Params withVersion(long version) {
            if (version != Versions.MATCH_ANY) {
                return putParam("version", Long.toString(version));
            }
            return this;
        }

        public Params withVersionType(VersionType versionType) {
            if (versionType != VersionType.INTERNAL) {
                return putParam("version_type", versionType.name().toLowerCase(Locale.ROOT));
            }
            return this;
        }

        /*Params withWaitForActiveShards(ActiveShardCount currentActiveShardCount, ActiveShardCount defaultActiveShardCount) {
            if (currentActiveShardCount != null && currentActiveShardCount != defaultActiveShardCount) {
                return putParam("wait_for_active_shards", currentActiveShardCount.toString().toLowerCase(Locale.ROOT));
            }
            return this;
        }*/

        public Params withIndicesOptions(IndicesOptions indicesOptions) {
            withIgnoreUnavailable(indicesOptions.ignoreUnavailable());
            putParam("allow_no_indices", Boolean.toString(indicesOptions.allowNoIndices()));
            String expandWildcards;
            if (!indicesOptions.expandWildcardsOpen() && !indicesOptions.expandWildcardsClosed()) {
                expandWildcards = "none";
            } else {
                StringJoiner joiner = new StringJoiner(",");
                if (indicesOptions.expandWildcardsOpen()) {
                    joiner.add("open");
                }
                if (indicesOptions.expandWildcardsClosed()) {
                    joiner.add("closed");
                }
                expandWildcards = joiner.toString();
            }
            putParam("expand_wildcards", expandWildcards);
            return this;
        }

        public Params withIgnoreUnavailable(boolean ignoreUnavailable) {
            // Always explicitly place the ignore_unavailable value.
            putParam("ignore_unavailable", Boolean.toString(ignoreUnavailable));
            return this;
        }

        public Params withHuman(boolean human) {
            if (human) {
                putParam("human", Boolean.toString(human));
            }
            return this;
        }

        public Params withLocal(boolean local) {
            if (local) {
                putParam("local", Boolean.toString(local));
            }
            return this;
        }

        public Params withIncludeDefaults(boolean includeDefaults) {
            if (includeDefaults) {
                return putParam("include_defaults", Boolean.TRUE.toString());
            }
            return this;
        }

        public Params withPreserveExisting(boolean preserveExisting) {
            if (preserveExisting) {
                return putParam("preserve_existing", Boolean.TRUE.toString());
            }
            return this;
        }

        public Params withDetailed(boolean detailed) {
            if (detailed) {
                return putParam("detailed", Boolean.TRUE.toString());
            }
            return this;
        }

        public Params withWaitForCompletion(boolean waitForCompletion) {
            if (waitForCompletion) {
                return putParam("wait_for_completion", Boolean.TRUE.toString());
            }
            return this;
        }

        public Params withNodes(String[] nodes) {
            if (nodes != null && nodes.length > 0) {
                return putParam("nodes", String.join(",", nodes));
            }
            return this;
        }

        public Params withActions(String[] actions) {
            if (actions != null && actions.length > 0) {
                return putParam("actions", String.join(",", actions));
            }
            return this;
        }

        public Params withTaskId(TaskId taskId) {
            if (taskId != null && taskId.isSet()) {
                return putParam("task_id", taskId.toString());
            }
            return this;
        }

        public Params withParentTaskId(TaskId parentTaskId) {
            if (parentTaskId != null && parentTaskId.isSet()) {
                return putParam("parent_task_id", parentTaskId.toString());
            }
            return this;
        }

        public Params withVerify(boolean verify) {
            if (verify) {
                return putParam("verify", Boolean.TRUE.toString());
            }
            return this;
        }

        public Params withWaitForStatus(ClusterHealthStatus status) {
            if (status != null) {
                return putParam("wait_for_status", status.name().toLowerCase(Locale.ROOT));
            }
            return this;
        }

        public Params withWaitForNoRelocatingShards(boolean waitNoRelocatingShards) {
            if (waitNoRelocatingShards) {
                return putParam("wait_for_no_relocating_shards", Boolean.TRUE.toString());
            }
            return this;
        }

        public Params withWaitForNoInitializingShards(boolean waitNoInitShards) {
            if (waitNoInitShards) {
                return putParam("wait_for_no_initializing_shards", Boolean.TRUE.toString());
            }
            return this;
        }

        public Params withWaitForNodes(String waitForNodes) {
            return putParam("wait_for_nodes", waitForNodes);
        }

    }

    /**
     * Utility class to build request's endpoint given its parts as strings
     */
    static class EndpointBuilder {

        private final StringJoiner joiner = new StringJoiner("/", "/", "");

        EndpointBuilder addPathPart(String... parts) {
            for (String part : parts) {
                if (Strings.hasLength(part)) {
                    joiner.add(encodePart(part));
                }
            }
            return this;
        }

        EndpointBuilder addCommaSeparatedPathParts(String[] parts) {
            addPathPart(String.join(",", parts));
            return this;
        }

        EndpointBuilder addPathPartAsIs(String... parts) {
            for (String part : parts) {
                if (Strings.hasLength(part)) {
                    joiner.add(part);
                }
            }
            return this;
        }

        String build() {
            return joiner.toString();
        }

        private static String encodePart(String pathPart) {
            try {
                //encode each part (e.g. index, type and id) separately before merging them into the path
                //we prepend "/" to the path part to make this path absolute, otherwise there can be issues with
                //paths that start with `-` or contain `:`
                URI uri = new URI(null, null, null, -1, "/" + pathPart, null, null);
                //manually encode any slash that each part may contain
                return uri.getRawPath().substring(1).replace("/", "%2F");
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Path part [" + pathPart + "] couldn't be encoded", e);
            }
        }
    }
}
