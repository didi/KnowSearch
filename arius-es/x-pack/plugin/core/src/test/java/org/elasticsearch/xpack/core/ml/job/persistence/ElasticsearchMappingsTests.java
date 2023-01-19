/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.core.ml.job.persistence;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.elasticsearch.Version;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingAction;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.util.concurrent.ThreadContext;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.get.GetResult;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.test.VersionUtils;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.xpack.core.ml.datafeed.DatafeedConfig;
import org.elasticsearch.xpack.core.ml.datafeed.DatafeedTimingStats;
import org.elasticsearch.xpack.core.ml.job.config.Job;
import org.elasticsearch.xpack.core.ml.job.config.ModelPlotConfig;
import org.elasticsearch.xpack.core.ml.job.process.autodetect.state.DataCounts;
import org.elasticsearch.xpack.core.ml.job.process.autodetect.state.ModelSizeStats;
import org.elasticsearch.xpack.core.ml.job.process.autodetect.state.ModelSnapshot;
import org.elasticsearch.xpack.core.ml.job.process.autodetect.state.Quantiles;
import org.elasticsearch.xpack.core.ml.job.process.autodetect.state.TimingStats;
import org.elasticsearch.xpack.core.ml.job.results.AnomalyRecord;
import org.elasticsearch.xpack.core.ml.job.results.CategoryDefinition;
import org.elasticsearch.xpack.core.ml.job.results.ReservedFieldNames;
import org.elasticsearch.xpack.core.ml.job.results.Result;
import org.mockito.ArgumentCaptor;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.mapper.MapperService.SINGLE_MAPPING_NAME;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


public class ElasticsearchMappingsTests extends ESTestCase {

    // These are not reserved because they're Elasticsearch keywords, not
    // field names
    private static List<String> KEYWORDS = Arrays.asList(
            ElasticsearchMappings.ANALYZER,
            ElasticsearchMappings.COPY_TO,
            ElasticsearchMappings.DYNAMIC,
            ElasticsearchMappings.ENABLED,
            ElasticsearchMappings.NESTED,
            ElasticsearchMappings.PROPERTIES,
            ElasticsearchMappings.TYPE,
            ElasticsearchMappings.WHITESPACE
    );

    private static List<String> INTERNAL_FIELDS = Arrays.asList(
            GetResult._ID,
            GetResult._INDEX,
            GetResult._TYPE
    );

    public void testResultsMappingReservedFields() throws Exception {
        Set<String> overridden = new HashSet<>(KEYWORDS);

        // These are not reserved because they're data types, not field names
        overridden.add(Result.TYPE.getPreferredName());
        overridden.add(DataCounts.TYPE.getPreferredName());
        overridden.add(CategoryDefinition.TYPE.getPreferredName());
        overridden.add(ModelSizeStats.RESULT_TYPE_FIELD.getPreferredName());
        overridden.add(ModelSnapshot.TYPE.getPreferredName());
        overridden.add(Quantiles.TYPE.getPreferredName());
        overridden.add(TimingStats.TYPE.getPreferredName());
        overridden.add(DatafeedTimingStats.TYPE.getPreferredName());

        Set<String> expected = collectResultsDocFieldNames();
        expected.removeAll(overridden);
        expected.addAll(INTERNAL_FIELDS);

        compareFields(expected, ReservedFieldNames.RESERVED_RESULT_FIELD_NAMES);
    }

    public void testConfigMappingReservedFields() throws Exception {
        Set<String> overridden = new HashSet<>(KEYWORDS);

        // These are not reserved because they're data types, not field names
        overridden.add(Job.TYPE);
        overridden.add(DatafeedConfig.TYPE);
        // ModelPlotConfig has an 'enabled' the same as one of the keywords
        overridden.remove(ModelPlotConfig.ENABLED_FIELD.getPreferredName());

        Set<String> expected = collectConfigDocFieldNames();
        expected.removeAll(overridden);
        expected.addAll(INTERNAL_FIELDS);

        compareFields(expected, ReservedFieldNames.RESERVED_CONFIG_FIELD_NAMES);
    }


    private void compareFields(Set<String> expected, Set<String> reserved) {
        if (reserved.size() != expected.size()) {
            Set<String> diff = new HashSet<>(reserved);
            diff.removeAll(expected);
            StringBuilder errorMessage = new StringBuilder("Fields in ReservedFieldNames but not in expected: ").append(diff);

            diff = new HashSet<>(expected);
            diff.removeAll(reserved);
            errorMessage.append("\nFields in expected but not in ReservedFieldNames: ").append(diff);
            fail(errorMessage.toString());
        }
        assertEquals(reserved.size(), expected.size());

        for (String s : expected) {
            // By comparing like this the failure messages say which string is missing
            String reservedField = reserved.contains(s) ? s : null;
            assertEquals(s, reservedField);
        }
    }

    @SuppressWarnings("unchecked")
    public void testTermFieldMapping() throws IOException {

        XContentBuilder builder = ElasticsearchMappings.termFieldsMapping(Arrays.asList("apple", "strawberry",
                AnomalyRecord.BUCKET_SPAN.getPreferredName()));

        XContentParser parser = createParser(builder);
        Map<String, Object> mapping = (Map<String, Object>) parser.map().get(SINGLE_MAPPING_NAME);
        Map<String, Object> properties = (Map<String, Object>) mapping.get(ElasticsearchMappings.PROPERTIES);

        Map<String, Object> instanceMapping = (Map<String, Object>) properties.get("apple");
        assertNotNull(instanceMapping);
        String dataType = (String)instanceMapping.get(ElasticsearchMappings.TYPE);
        assertEquals(ElasticsearchMappings.KEYWORD, dataType);

        instanceMapping = (Map<String, Object>) properties.get("strawberry");
        assertNotNull(instanceMapping);
        dataType = (String)instanceMapping.get(ElasticsearchMappings.TYPE);
        assertEquals(ElasticsearchMappings.KEYWORD, dataType);

        // check no mapping for the reserved field
        instanceMapping = (Map<String, Object>) properties.get(AnomalyRecord.BUCKET_SPAN.getPreferredName());
        assertNull(instanceMapping);
    }


    public void testMappingRequiresUpdateNoMapping() throws IOException {
        ClusterState.Builder csBuilder = ClusterState.builder(new ClusterName("_name"));
        ClusterState cs = csBuilder.build();
        String[] indices = new String[] { "no_index" };

        assertArrayEquals(new String[] { "no_index" }, ElasticsearchMappings.mappingRequiresUpdate(cs, indices, Version.CURRENT));
    }

    public void testMappingRequiresUpdateNullMapping() throws IOException {
        ClusterState cs = getClusterStateWithMappingsWithMetaData(Collections.singletonMap("null_mapping", null));
        String[] indices = new String[] { "null_index" };
        assertArrayEquals(indices, ElasticsearchMappings.mappingRequiresUpdate(cs, indices, Version.CURRENT));
    }

    public void testMappingRequiresUpdateNoVersion() throws IOException {
        ClusterState cs = getClusterStateWithMappingsWithMetaData(Collections.singletonMap("no_version_field", "NO_VERSION_FIELD"));
        String[] indices = new String[] { "no_version_field" };
        assertArrayEquals(indices, ElasticsearchMappings.mappingRequiresUpdate(cs, indices, Version.CURRENT));
    }

    public void testMappingRequiresUpdateRecentMappingVersion() throws IOException {
        ClusterState cs = getClusterStateWithMappingsWithMetaData(Collections.singletonMap("version_current", Version.CURRENT.toString()));
        String[] indices = new String[] { "version_current" };
        assertArrayEquals(new String[] {}, ElasticsearchMappings.mappingRequiresUpdate(cs, indices, Version.CURRENT));
    }

    public void testMappingRequiresUpdateMaliciousMappingVersion() throws IOException {
        ClusterState cs = getClusterStateWithMappingsWithMetaData(
            Collections.singletonMap("version_current", Collections.singletonMap("nested", "1.0")));
        String[] indices = new String[] { "version_nested" };
        assertArrayEquals(indices, ElasticsearchMappings.mappingRequiresUpdate(cs, indices, Version.CURRENT));
    }

    public void testMappingRequiresUpdateBogusMappingVersion() throws IOException {
        ClusterState cs = getClusterStateWithMappingsWithMetaData(Collections.singletonMap("version_bogus", "0.0"));
        String[] indices = new String[] { "version_bogus" };
        assertArrayEquals(indices, ElasticsearchMappings.mappingRequiresUpdate(cs, indices, Version.CURRENT));
    }

    public void testMappingRequiresUpdateNewerMappingVersion() throws IOException {
        ClusterState cs = getClusterStateWithMappingsWithMetaData(Collections.singletonMap("version_newer", Version.CURRENT));
        String[] indices = new String[] { "version_newer" };
        assertArrayEquals(new String[] {}, ElasticsearchMappings.mappingRequiresUpdate(cs, indices, VersionUtils.getPreviousVersion()));
    }

    public void testMappingRequiresUpdateNewerMappingVersionMinor() throws IOException {
        ClusterState cs = getClusterStateWithMappingsWithMetaData(Collections.singletonMap("version_newer_minor", Version.CURRENT));
        String[] indices = new String[] { "version_newer_minor" };
        assertArrayEquals(new String[] {},
            ElasticsearchMappings.mappingRequiresUpdate(cs, indices, VersionUtils.getPreviousMinorVersion()));
    }

    public void testAddDocMappingIfMissing() throws IOException {
        ThreadPool threadPool = mock(ThreadPool.class);
        when(threadPool.getThreadContext()).thenReturn(new ThreadContext(Settings.EMPTY));
        Client client = mock(Client.class);
        when(client.threadPool()).thenReturn(threadPool);
        doAnswer(
            invocationOnMock -> {
                ActionListener listener = (ActionListener) invocationOnMock.getArguments()[2];
                listener.onResponse(new AcknowledgedResponse(true));
                return null;
            })
            .when(client).execute(eq(PutMappingAction.INSTANCE), any(), any(ActionListener.class));

        ClusterState clusterState = getClusterStateWithMappingsWithMetaData(Collections.singletonMap("index-name", "0.0"));
        ElasticsearchMappings.addDocMappingIfMissing(
            "index-name",
            ElasticsearchMappingsTests::fakeMapping,
            client,
            clusterState,
            ActionListener.wrap(
                ok -> assertTrue(ok),
                e -> fail(e.toString())
            )
        );

        ArgumentCaptor<PutMappingRequest> requestCaptor = ArgumentCaptor.forClass(PutMappingRequest.class);
        verify(client).threadPool();
        verify(client).execute(eq(PutMappingAction.INSTANCE), requestCaptor.capture(), any(ActionListener.class));
        verifyNoMoreInteractions(client);

        PutMappingRequest request = requestCaptor.getValue();
        assertThat(request.type(), equalTo("_doc"));
        assertThat(request.indices(), equalTo(new String[] { "index-name" }));
        assertThat(request.source(), equalTo("{\"_doc\":{\"properties\":{\"some-field\":{\"type\":\"long\"}}}}"));
    }

    private static XContentBuilder fakeMapping(String mappingType) throws IOException {
        return jsonBuilder()
            .startObject()
                .startObject(mappingType)
                    .startObject(ElasticsearchMappings.PROPERTIES)
                        .startObject("some-field")
                            .field(ElasticsearchMappings.TYPE, ElasticsearchMappings.LONG)
                        .endObject()
                    .endObject()
                .endObject()
            .endObject();
    }

    private ClusterState getClusterStateWithMappingsWithMetaData(Map<String, Object> namesAndVersions) throws IOException {
        MetaData.Builder metaDataBuilder = MetaData.builder();

        for (Map.Entry<String, Object> entry : namesAndVersions.entrySet()) {

            String indexName = entry.getKey();
            Object version = entry.getValue();

            IndexMetaData.Builder indexMetaData = IndexMetaData.builder(indexName);
            indexMetaData.settings(Settings.builder().put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                .put(IndexMetaData.SETTING_NUMBER_OF_SHARDS, 1).put(IndexMetaData.SETTING_NUMBER_OF_REPLICAS, 0));

            Map<String, Object> mapping = new HashMap<>();
            Map<String, Object> properties = new HashMap<>();
            for (int i = 0; i < 10; i++) {
                properties.put("field" + i, Collections.singletonMap("type", "string"));
            }
            mapping.put("properties", properties);

            Map<String, Object> meta = new HashMap<>();
            if (version != null && version.equals("NO_VERSION_FIELD") == false) {
                meta.put("version", version);
            }
            mapping.put("_meta", meta);

            indexMetaData.putMapping(new MappingMetaData("_doc", mapping));

            metaDataBuilder.put(indexMetaData);
        }
        MetaData metaData = metaDataBuilder.build();

        ClusterState.Builder csBuilder = ClusterState.builder(new ClusterName("_name"));
        csBuilder.metaData(metaData);
        return csBuilder.build();
    }

    private Set<String> collectResultsDocFieldNames() throws IOException {
        // Only the mappings for the results index should be added below.  Do NOT add mappings for other indexes here.
        return collectFieldNames(ElasticsearchMappings.resultsMapping("_doc"));
    }

    private Set<String> collectConfigDocFieldNames() throws IOException {
        // Only the mappings for the config index should be added below.  Do NOT add mappings for other indexes here.
        return collectFieldNames(ElasticsearchMappings.configMapping());
    }

    private Set<String> collectFieldNames(XContentBuilder mapping) throws IOException {
        BufferedInputStream inputStream =
                new BufferedInputStream(new ByteArrayInputStream(Strings.toString(mapping).getBytes(StandardCharsets.UTF_8)));
        JsonParser parser = new JsonFactory().createParser(inputStream);
        Set<String> fieldNames = new HashSet<>();
        boolean isAfterPropertiesStart = false;
        try {
            JsonToken token = parser.nextToken();
            while (token != null) {
                switch (token) {
                    case START_OBJECT:
                        break;
                    case FIELD_NAME:
                        String fieldName = parser.getCurrentName();
                        if (isAfterPropertiesStart) {
                            fieldNames.add(fieldName);
                        } else {
                            if (ElasticsearchMappings.PROPERTIES.equals(fieldName)) {
                                isAfterPropertiesStart = true;
                            }
                        }
                        break;
                    default:
                        break;
                }
                token = parser.nextToken();
            }
        } catch (JsonParseException e) {
            fail("Cannot parse JSON: " + e);
        }

        return fieldNames;
    }
}
