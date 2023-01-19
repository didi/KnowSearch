/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.monitoring.exporter.local;

import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.ingest.GetPipelineResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.cluster.metadata.IndexTemplateMetaData;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.time.DateFormatter;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.ingest.PipelineConfiguration;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.Max;
import org.elasticsearch.test.ESIntegTestCase;
import org.elasticsearch.xpack.core.monitoring.MonitoredSystem;
import org.elasticsearch.xpack.core.monitoring.action.MonitoringBulkDoc;
import org.elasticsearch.xpack.core.monitoring.action.MonitoringBulkRequestBuilder;
import org.elasticsearch.xpack.core.monitoring.exporter.MonitoringTemplateUtils;
import org.elasticsearch.xpack.monitoring.MonitoringService;
import org.elasticsearch.xpack.monitoring.MonitoringTestUtils;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.elasticsearch.search.aggregations.AggregationBuilders.max;
import static org.elasticsearch.search.aggregations.AggregationBuilders.terms;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;
import static org.elasticsearch.xpack.core.monitoring.MonitoredSystem.BEATS;
import static org.elasticsearch.xpack.core.monitoring.MonitoredSystem.KIBANA;
import static org.elasticsearch.xpack.core.monitoring.MonitoredSystem.LOGSTASH;
import static org.elasticsearch.xpack.core.monitoring.exporter.MonitoringTemplateUtils.PIPELINE_IDS;
import static org.elasticsearch.xpack.core.monitoring.exporter.MonitoringTemplateUtils.TEMPLATE_VERSION;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

@ESIntegTestCase.ClusterScope(scope = ESIntegTestCase.Scope.SUITE,
                              numDataNodes = 1, numClientNodes = 0, transportClientRatio = 0.0, supportsDedicatedMasters = false)
public class LocalExporterIntegTests extends LocalExporterIntegTestCase {
    private final String indexTimeFormat = randomFrom("yy", "yyyy", "yyyy.MM", "yyyy-MM", "MM.yyyy", "MM", null);

    private void stopMonitoring() {
        // Now disabling the monitoring service, so that no more collection are started
        assertAcked(client().admin().cluster().prepareUpdateSettings().setTransientSettings(
                Settings.builder().putNull(MonitoringService.ENABLED.getKey())
                                  .putNull("xpack.monitoring.exporters._local.enabled")
                                  .putNull("xpack.monitoring.exporters._local.cluster_alerts.management.enabled")
                                  .putNull("xpack.monitoring.exporters._local.index.name.time_format")));
    }

    public void testExport() throws Exception {
        try {
            if (randomBoolean()) {
                // indexing some random documents
                IndexRequestBuilder[] indexRequestBuilders = new IndexRequestBuilder[5];
                for (int i = 0; i < indexRequestBuilders.length; i++) {
                    indexRequestBuilders[i] = client().prepareIndex("test", "type", Integer.toString(i))
                            .setSource("title", "This is a random document");
                }
                indexRandom(true, indexRequestBuilders);
            }

            // start the monitoring service so that /_monitoring/bulk is not ignored
            final Settings.Builder exporterSettings = Settings.builder()
                    .put(MonitoringService.ENABLED.getKey(), true)
                    .put("xpack.monitoring.exporters._local.enabled", true)
                    .put("xpack.monitoring.exporters._local.cluster_alerts.management.enabled", false);

            if (indexTimeFormat != null) {
                exporterSettings.put("xpack.monitoring.exporters._local.index.name.time_format", indexTimeFormat);
            }

            // local exporter is now enabled
            assertAcked(client().admin().cluster().prepareUpdateSettings().setTransientSettings(exporterSettings));

            if (randomBoolean()) {
                // export some documents now, before starting the monitoring service
                final int nbDocs = randomIntBetween(1, 20);
                List<MonitoringBulkDoc> monitoringDocs = new ArrayList<>(nbDocs);
                for (int i = 0; i < nbDocs; i++) {
                    monitoringDocs.add(createMonitoringBulkDoc());
                }

                assertBusy(() -> {
                    MonitoringBulkRequestBuilder bulk = monitoringClient().prepareMonitoringBulk();
                    monitoringDocs.forEach(bulk::add);
                    assertEquals(RestStatus.OK, bulk.get().status());
                    refresh();

                    assertThat(client().admin().indices().prepareExists(".monitoring-*").get().isExists(), is(true));
                    ensureYellowAndNoInitializingShards(".monitoring-*");

                    SearchResponse response = client().prepareSearch(".monitoring-*").get();
                    assertThat((long)nbDocs, lessThanOrEqualTo(response.getHits().getTotalHits().value));
                });

                checkMonitoringTemplates();
                checkMonitoringPipelines();
                checkMonitoringDocs();
            }

            final int numNodes = internalCluster().getNodeNames().length;
            assertBusy(() -> {
                assertThat(client().admin().indices().prepareExists(".monitoring-*").get().isExists(), is(true));
                ensureYellowAndNoInitializingShards(".monitoring-*");

                assertThat(client().prepareSearch(".monitoring-es-*")
                        .setSize(0)
                        .setQuery(QueryBuilders.termQuery("type", "cluster_stats"))
                        .get().getHits().getTotalHits().value, greaterThan(0L));

                assertThat(client().prepareSearch(".monitoring-es-*")
                        .setSize(0)
                        .setQuery(QueryBuilders.termQuery("type", "index_recovery"))
                        .get().getHits().getTotalHits().value, greaterThan(0L));

                assertThat(client().prepareSearch(".monitoring-es-*")
                        .setSize(0)
                        .setQuery(QueryBuilders.termQuery("type", "index_stats"))
                        .get().getHits().getTotalHits().value, greaterThan(0L));

                assertThat(client().prepareSearch(".monitoring-es-*")
                        .setSize(0)
                        .setQuery(QueryBuilders.termQuery("type", "indices_stats"))
                        .get().getHits().getTotalHits().value, greaterThan(0L));

                assertThat(client().prepareSearch(".monitoring-es-*")
                        .setSize(0)
                        .setQuery(QueryBuilders.termQuery("type", "shards"))
                        .get().getHits().getTotalHits().value, greaterThan(0L));

                SearchResponse response = client().prepareSearch(".monitoring-es-*")
                        .setSize(0)
                        .setQuery(QueryBuilders.termQuery("type", "node_stats"))
                        .addAggregation(terms("agg_nodes_ids").field("node_stats.node_id"))
                        .get();

                Terms aggregation = response.getAggregations().get("agg_nodes_ids");
                assertEquals("Aggregation on node_id must return a bucket per node involved in test",
                        numNodes, aggregation.getBuckets().size());

                for (String nodeName : internalCluster().getNodeNames()) {
                    String nodeId = internalCluster().clusterService(nodeName).localNode().getId();
                    Terms.Bucket bucket = aggregation.getBucketByKey(nodeId);
                    assertTrue("No bucket found for node id [" + nodeId + "]", bucket != null);
                    assertTrue(bucket.getDocCount() >= 1L);
                }

            }, 30L, TimeUnit.SECONDS);

            checkMonitoringTemplates();
            checkMonitoringPipelines();
            checkMonitoringDocs();
        } finally {
            stopMonitoring();
        }

        // This assertion loop waits for in flight exports to terminate. It checks that the latest
        // node_stats document collected for each node is at least 10 seconds old, corresponding to
        // 2 or 3 elapsed collection intervals.
        final int elapsedInSeconds = 10;
        final ZonedDateTime startTime = ZonedDateTime.now(ZoneOffset.UTC);
        assertBusy(() -> {
            IndicesExistsResponse indicesExistsResponse = client().admin().indices().prepareExists(".monitoring-*").get();
            if (indicesExistsResponse.isExists()) {
                ensureYellowAndNoInitializingShards(".monitoring-*");
                refresh(".monitoring-es-*");

                SearchResponse response = client().prepareSearch(".monitoring-es-*")
                        .setSize(0)
                        .setQuery(QueryBuilders.termQuery("type", "node_stats"))
                        .addAggregation(terms("agg_nodes_ids").field("node_stats.node_id")
                                .subAggregation(max("agg_last_time_collected").field("timestamp")))
                        .get();

                Terms aggregation = response.getAggregations().get("agg_nodes_ids");
                for (String nodeName : internalCluster().getNodeNames()) {
                    String nodeId = internalCluster().clusterService(nodeName).localNode().getId();
                    Terms.Bucket bucket = aggregation.getBucketByKey(nodeId);
                    assertTrue("No bucket found for node id [" + nodeId + "]", bucket != null);
                    assertTrue(bucket.getDocCount() >= 1L);

                    Max subAggregation = bucket.getAggregations().get("agg_last_time_collected");
                    ZonedDateTime lastCollection = Instant.ofEpochMilli(Math.round(subAggregation.getValue())).atZone(ZoneOffset.UTC);
                    assertTrue(lastCollection.plusSeconds(elapsedInSeconds).isBefore(ZonedDateTime.now(ZoneOffset.UTC)));
                }
            } else {
                assertTrue(ZonedDateTime.now(ZoneOffset.UTC).isAfter(startTime.plusSeconds(elapsedInSeconds)));
            }
        }, 30L, TimeUnit.SECONDS);
    }

    /**
     * Checks that the monitoring templates have been created by the local exporter
     */
    private void checkMonitoringTemplates() {
        final Set<String> templates = new HashSet<>();
        templates.add(".monitoring-alerts-7");
        templates.add(".monitoring-es");
        templates.add(".monitoring-kibana");
        templates.add(".monitoring-logstash");
        templates.add(".monitoring-beats");

        GetIndexTemplatesResponse response = client().admin().indices().prepareGetTemplates(".monitoring-*").get();
        Set<String> actualTemplates = response.getIndexTemplates().stream().map(IndexTemplateMetaData::getName).collect(Collectors.toSet());
        assertEquals(templates, actualTemplates);
    }

    /**
     * Checks that the monitoring ingest pipelines have been created by the local exporter
     */
    private void checkMonitoringPipelines() {
        final Set<String> expectedPipelines =
                Arrays.stream(PIPELINE_IDS).map(MonitoringTemplateUtils::pipelineName).collect(Collectors.toSet());

        final GetPipelineResponse response = client().admin().cluster().prepareGetPipeline("xpack_monitoring_*").get();

        // actual pipelines
        final Set<String> pipelines = response.pipelines().stream().map(PipelineConfiguration::getId).collect(Collectors.toSet());

        assertEquals("Missing expected pipelines", expectedPipelines, pipelines);
        assertTrue("monitoring ingest pipeline not found", response.isFound());
    }

    /**
     * Checks that the monitoring documents all have the cluster_uuid, timestamp and source_node
     * fields and belongs to the right data or timestamped index.
     */
    private void checkMonitoringDocs() {
        ClusterStateResponse response = client().admin().cluster().prepareState().get();
        String customTimeFormat = response.getState().getMetaData().transientSettings()
                .get("xpack.monitoring.exporters._local.index.name.time_format");
        assertEquals(indexTimeFormat, customTimeFormat);
        if (customTimeFormat == null) {
            customTimeFormat = "yyyy.MM.dd";
        }

        DateFormatter dateParser = DateFormatter.forPattern("strict_date_time");
        DateFormatter dateFormatter = DateFormatter.forPattern(customTimeFormat).withZone(ZoneOffset.UTC);

        SearchResponse searchResponse = client().prepareSearch(".monitoring-*").setSize(100).get();
        assertThat(searchResponse.getHits().getTotalHits().value, greaterThan(0L));

        for (SearchHit hit : searchResponse.getHits().getHits()) {
            final Map<String, Object> source = hit.getSourceAsMap();

            assertTrue(source != null && source.isEmpty() == false);

            final String timestamp = (String) source.get("timestamp");
            final String type = (String) source.get("type");

            assertTrue("document is missing cluster_uuid field", Strings.hasText((String) source.get("cluster_uuid")));
            assertTrue("document is missing timestamp field", Strings.hasText(timestamp));
            assertTrue("document is missing type field", Strings.hasText(type));

            @SuppressWarnings("unchecked")
            Map<String, Object> docSource = (Map<String, Object>) source.get("doc");

            MonitoredSystem expectedSystem;
            if (docSource == null) {
                // This is a document indexed by the Monitoring service
                expectedSystem = MonitoredSystem.ES;
            } else {
                // This is a document indexed through the Monitoring Bulk API
                expectedSystem = MonitoredSystem.fromSystem((String) docSource.get("expected_system"));
            }

            String dateTime = dateFormatter.format(dateParser.parse(timestamp));
            final String expectedIndex = ".monitoring-" + expectedSystem.getSystem() + "-" + TEMPLATE_VERSION + "-" + dateTime;
            assertEquals("Expected " + expectedIndex + " but got " + hit.getIndex(), expectedIndex, hit.getIndex());

            @SuppressWarnings("unchecked")
            Map<String, Object> sourceNode = (Map<String, Object>) source.get("source_node");
            if ("shards".equals(type) == false) {
                assertNotNull("document is missing source_node field", sourceNode);
            }
        }
    }

    private static MonitoringBulkDoc createMonitoringBulkDoc() throws IOException {
        final MonitoredSystem system = randomFrom(BEATS, KIBANA, LOGSTASH);
        final XContentType xContentType = randomFrom(XContentType.values());
        final BytesReference source;

        try (XContentBuilder builder = XContentBuilder.builder(xContentType.xContent())) {
            builder.startObject();
            {
                builder.field("expected_system", system.getSystem());
                final int nbFields = randomIntBetween(1, 3);
                for (int i = 0; i < nbFields; i++) {
                    builder.field("field_" + i, i);
                }
            }
            builder.endObject();
            source = BytesReference.bytes(builder);
        }

        return MonitoringTestUtils.randomMonitoringBulkDoc(random(), xContentType, source, system, "doc");
    }

}
