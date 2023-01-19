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


package org.elasticsearch.action.bulk;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.Version;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsResponse;
import org.elasticsearch.action.admin.indices.stats.ShardStats;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.ingest.PutPipelineRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.support.replication.ReplicationRequest;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.routing.Murmur3HashFunction;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.ingest.IngestTestPlugin;
import org.elasticsearch.ingest.Pipeline;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.test.ESIntegTestCase;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.elasticsearch.action.DocWriteResponse.Result.CREATED;
import static org.elasticsearch.action.DocWriteResponse.Result.UPDATED;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.test.StreamsUtils.copyToStringFromClasspath;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.isOneOf;

public class BulkIntegrationIT extends ESIntegTestCase {
    @Override
    protected Collection<Class<? extends Plugin>> nodePlugins() {
        return Arrays.asList(IngestTestPlugin.class);
    }

    public void testBulkIndexCreatesMapping() throws Exception {
        String bulkAction = copyToStringFromClasspath("/org/elasticsearch/action/bulk/bulk-log.json");
        BulkRequestBuilder bulkBuilder = client().prepareBulk();
        bulkBuilder.add(bulkAction.getBytes(StandardCharsets.UTF_8), 0, bulkAction.length(), null, XContentType.JSON);
        bulkBuilder.get();
        assertBusy(() -> {
            GetMappingsResponse mappingsResponse = client().admin().indices().prepareGetMappings().get();
            assertTrue(mappingsResponse.getMappings().containsKey("logstash-2014.03.30"));
            assertTrue(mappingsResponse.getMappings().get("logstash-2014.03.30").containsKey("logs"));
        });
    }

    /**
     * This tests that the {@link TransportBulkAction} evaluates alias routing values correctly when dealing with
     * an alias pointing to multiple indices, while a write index exits.
     */
    public void testBulkWithWriteIndexAndRouting() {
        Map<String, Integer> twoShardsSettings = Collections.singletonMap(IndexMetaData.SETTING_NUMBER_OF_SHARDS, 2);
        client().admin().indices().prepareCreate("index1")
            .addAlias(new Alias("alias1").indexRouting("0")).setSettings(twoShardsSettings).get();
        client().admin().indices().prepareCreate("index2")
            .addAlias(new Alias("alias1").indexRouting("0").writeIndex(randomFrom(false, null)))
            .setSettings(twoShardsSettings).get();
        client().admin().indices().prepareCreate("index3")
            .addAlias(new Alias("alias1").indexRouting("1").writeIndex(true)).setSettings(twoShardsSettings).get();

        IndexRequest indexRequestWithAlias = new IndexRequest("alias1", "type", "id");
        if (randomBoolean()) {
            indexRequestWithAlias.routing("1");
        }
        indexRequestWithAlias.source(Collections.singletonMap("foo", "baz"));
        BulkResponse bulkResponse = client().prepareBulk().add(indexRequestWithAlias).get();
        assertThat(bulkResponse.getItems()[0].getResponse().getIndex(), equalTo("index3"));
        assertThat(bulkResponse.getItems()[0].getResponse().getVersion(), equalTo(1L));
        assertThat(bulkResponse.getItems()[0].getResponse().status(), equalTo(RestStatus.CREATED));
        assertThat(client().prepareGet("index3", "type", "id").setRouting("1").get().getSource().get("foo"), equalTo("baz"));

        bulkResponse = client().prepareBulk().add(client().prepareUpdate("alias1", "type", "id").setDoc("foo", "updated")).get();
        assertFalse(bulkResponse.buildFailureMessage(), bulkResponse.hasFailures());
        assertThat(client().prepareGet("index3", "type", "id").setRouting("1").get().getSource().get("foo"), equalTo("updated"));
        bulkResponse = client().prepareBulk().add(client().prepareDelete("alias1", "type", "id")).get();
        assertFalse(bulkResponse.buildFailureMessage(), bulkResponse.hasFailures());
        assertFalse(client().prepareGet("index3", "type", "id").setRouting("1").get().isExists());
    }

    // allowing the auto-generated timestamp to externally be set would allow making the index inconsistent with duplicate docs
    public void testExternallySetAutoGeneratedTimestamp() {
        IndexRequest indexRequest = new IndexRequest("index1", "_doc").source(Collections.singletonMap("foo", "baz"));
        indexRequest.process(Version.CURRENT, null, null); // sets the timestamp
        if (randomBoolean()) {
            indexRequest.id("test");
        }
        assertThat(expectThrows(IllegalArgumentException.class, () -> client().prepareBulk().add(indexRequest).get()).getMessage(),
            containsString("autoGeneratedTimestamp should not be set externally"));
    }

    public void testBulkWithGlobalDefaults() throws Exception {
        // all requests in the json are missing index and type parameters: "_index" : "test", "_type" : "type1",
        String bulkAction = copyToStringFromClasspath("/org/elasticsearch/action/bulk/simple-bulk-missing-index-type.json");
        {
            BulkRequestBuilder bulkBuilder = client().prepareBulk();
            bulkBuilder.add(bulkAction.getBytes(StandardCharsets.UTF_8), 0, bulkAction.length(), null, XContentType.JSON);
            ActionRequestValidationException ex = expectThrows(ActionRequestValidationException.class, bulkBuilder::get);

            assertThat(ex.validationErrors(), containsInAnyOrder(
                "index is missing",
                "index is missing",
                "index is missing"));
        }

        {
            createSamplePipeline("pipeline");
            BulkRequestBuilder bulkBuilder = client().prepareBulk("test","type1")
                .routing("routing")
                .pipeline("pipeline");

            bulkBuilder.add(bulkAction.getBytes(StandardCharsets.UTF_8), 0, bulkAction.length(), null, XContentType.JSON);
            BulkResponse bulkItemResponses = bulkBuilder.get();
            assertFalse(bulkItemResponses.hasFailures());
        }
    }

    private void createSamplePipeline(String pipelineId) throws IOException, ExecutionException, InterruptedException {
        XContentBuilder pipeline = jsonBuilder()
            .startObject()
                .startArray("processors")
                    .startObject()
                        .startObject("test")
                        .endObject()
                    .endObject()
                .endArray()
            .endObject();

        AcknowledgedResponse acknowledgedResponse = client().admin()
            .cluster()
            .putPipeline(new PutPipelineRequest(pipelineId, BytesReference.bytes(pipeline), XContentType.JSON))
            .get();

        assertTrue(acknowledgedResponse.isAcknowledged());
    }

    /** This test ensures that index deletion makes indexing fail quickly, not wait on the index that has disappeared */
    public void testDeleteIndexWhileIndexing() throws Exception {
        String index = "deleted_while_indexing";
        createIndex(index);
        AtomicBoolean stopped = new AtomicBoolean();
        Thread[] threads = new Thread[between(1, 4)];
        AtomicInteger docID = new AtomicInteger();
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                while (stopped.get() == false && docID.get() < 5000) {
                    String id = Integer.toString(docID.incrementAndGet());
                    try {
                        IndexResponse response = client().prepareIndex(index, "_doc").setId(id)
                            .setSource(Collections.singletonMap("f" + randomIntBetween(1, 10), randomNonNegativeLong()),
                                XContentType.JSON).get();
                        assertThat(response.getResult(), isOneOf(CREATED, UPDATED));
                        logger.info("--> index id={} seq_no={}", response.getId(), response.getSeqNo());
                    } catch (ElasticsearchException ignore) {
                        logger.info("--> fail to index id={}", id);
                    }
                }
            });
            threads[i].start();
        }
        ensureGreen(index);
        assertBusy(() -> assertThat(docID.get(), greaterThanOrEqualTo(1)));
        assertAcked(client().admin().indices().prepareDelete(index));
        stopped.set(true);
        for (Thread thread : threads) {
            thread.join(ReplicationRequest.DEFAULT_TIMEOUT.millis() / 2);
            assertFalse(thread.isAlive());
        }
    }

    public void testBulkRemoveWithPipeline() {
        Map<String, Integer> twoShardsSettings = Collections.singletonMap(IndexMetaData.SETTING_NUMBER_OF_SHARDS, 2);
        client().admin().indices().prepareCreate("index1").setSettings(twoShardsSettings).get();
        client().admin().cluster().preparePutPipeline("pipeline", new BytesArray("{\"description\":\"test\",\"processors\":[]}"), XContentType.JSON).get();

        IndexRequest indexRequest = new IndexRequest("index1", "type", "id").setPipeline("pipeline");
        indexRequest.source(Collections.singletonMap("foo", "baz"));
        BulkResponse bulkResponse = client().prepareBulk().add(indexRequest).get();
        assertThat(bulkResponse.getItems()[0].getResponse().getIndex(), equalTo("index1"));
        assertThat(bulkResponse.getItems()[0].getResponse().getVersion(), equalTo(1L));
        assertThat(bulkResponse.getItems()[0].getResponse().status(), equalTo(RestStatus.CREATED));
        assertThat(client().prepareGet("index1", "type", "id").get().getSource().get("foo"), equalTo("baz"));

        DeleteRequest deleteRequest = new DeleteRequest("index1", "type", "id").setPipeline("pipeline");
        deleteRequest.source(Collections.singletonMap("foo", "baz"));
        BulkResponse bulkResponse2 = client().prepareBulk().add(deleteRequest).get();
        assertThat(bulkResponse2.getItems()[0].getResponse().getIndex(), equalTo("index1"));
        assertThat(bulkResponse2.getItems()[0].getResponse().getVersion(), equalTo(2L));
        assertThat(bulkResponse2.getItems()[0].getResponse().status(), equalTo(RestStatus.OK));
        assertThat(client().prepareGet("index1", "type", "id").get().isExists(), equalTo(false));
    }

    public void testBulkWithRandom() {
        Map<String, String> indexSettings = new HashMap<>();
        int shardNum = randomIntBetween(10, 20);
        indexSettings.put(IndexMetaData.SETTING_NUMBER_OF_SHARDS, String.valueOf(shardNum));
        indexSettings.put(IndexSettings.INDEX_ROUTING_RANDOM, "true");
        client().admin().indices().prepareCreate("index").setSettings(indexSettings).get();

        int indexDocs = randomIntBetween(5, 10);
        BulkRequest bulkRequest = new BulkRequest();
        for (int i = 0; i < indexDocs; i ++) {
            IndexRequest indexRequest = new IndexRequest("index", "type");
            indexRequest.source(Collections.singletonMap("foo", "baz"));
            bulkRequest.add(indexRequest);
        }

        BulkResponse bulkItemResponses = client().bulk(bulkRequest).actionGet();
        assertThat(bulkItemResponses.getItems().length, equalTo(indexDocs));
        int shardId = bulkItemResponses.getItems()[0].getResponse().getShardId().id();
        for (BulkItemResponse itemResponse : bulkItemResponses.getItems()) {
            assertThat(itemResponse.getResponse().getShardId().id(), equalTo(shardId));
        }
    }

    public void testBulkWithLogicShard() {
        Map<String, String> indexSettings = new HashMap<>();
        int logicShard = randomIntBetween(4, 8);
        int shardNum = logicShard * randomIntBetween(4, 8);
        indexSettings.put(IndexMetaData.SETTING_NUMBER_OF_SHARDS, String.valueOf(shardNum));
        indexSettings.put(IndexSettings.INDEX_NUMBER_OF_ROUTING_SIZE, String.valueOf(logicShard));
        client().admin().indices().prepareCreate("index").setSettings(indexSettings).get();

        int indexDocs = randomIntBetween(500, 1000);
        BulkRequest bulkRequest = new BulkRequest();
        for (int i = 0; i < indexDocs; i ++) {
            if (Murmur3HashFunction.hash(String.valueOf(i)) % logicShard == 1) {
                IndexRequest indexRequest = new IndexRequest("index", "type").routing(String.valueOf(i));
                indexRequest.source(Collections.singletonMap("foo", "baz"));
                bulkRequest.add(indexRequest);
            }
        }

        BulkResponse bulkItemResponses = client().bulk(bulkRequest).actionGet();
        int shardId = bulkItemResponses.getItems()[0].getResponse().getShardId().id();
        for (BulkItemResponse itemResponse : bulkItemResponses.getItems()) {
            assertThat(itemResponse.getResponse().getShardId().id(), equalTo(shardId));
        }
    }

    /**
     * 验证：http://git.xiaojukeji.com/bigdata-databus/elasticsearch-didi/issues/147
     */
    public void testBulkWithBothRandomAndLogicShard() {
        Map<String, String> indexSettings = new HashMap<>();
        int logicShard = 2;
        int shardNum = 4;
        indexSettings.put(IndexMetaData.SETTING_NUMBER_OF_SHARDS, String.valueOf(shardNum));
        indexSettings.put(IndexSettings.INDEX_NUMBER_OF_ROUTING_SIZE, String.valueOf(logicShard));
        indexSettings.put(IndexSettings.INDEX_ROUTING_RANDOM, "true");
        client().admin().indices().prepareCreate("index").setSettings(indexSettings).get();

        int bulkCount = randomIntBetween(30, 50);
        for (int i = 0; i < bulkCount; i ++) {
            int indexDocs = randomIntBetween(5, 10);
            BulkRequest bulkRequest = new BulkRequest();
            for (int j = 0; j < indexDocs; j ++) {
                IndexRequest indexRequest = new IndexRequest("index", "type");
                indexRequest.source(Collections.singletonMap("foo", "baz"));
                bulkRequest.add(indexRequest);
            }

            BulkResponse bulkItemResponses = client().bulk(bulkRequest).actionGet();
            assertThat(bulkItemResponses.getItems().length, equalTo(indexDocs));
            int shardId = bulkItemResponses.getItems()[0].getResponse().getShardId().id();
            for (BulkItemResponse itemResponse : bulkItemResponses.getItems()) {
                assertThat(itemResponse.getResponse().getShardId().id(), equalTo(shardId));
            }
        }

        client().admin().indices().prepareRefresh("index").get();

        IndicesStatsResponse indicesStatsResponse = client().admin().indices().prepareStats("index").all().get();
        for (ShardStats shardStats : indicesStatsResponse.getShards()) {
            assertThat(shardStats.getStats().getDocs().getCount(), greaterThan(0L));
        }
    }
}
