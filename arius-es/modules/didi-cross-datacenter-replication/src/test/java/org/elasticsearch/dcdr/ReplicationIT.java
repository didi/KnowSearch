package org.elasticsearch.dcdr;

import static java.util.Collections.singletonMap;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import org.elasticsearch.action.admin.indices.stats.IndicesStatsResponse;
import org.elasticsearch.action.admin.indices.template.delete.DeleteIndexTemplateRequest;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.dcdr.action.*;
import org.elasticsearch.dcdr.translog.primary.CompositeDCDRStats;
import org.elasticsearch.dcdr.translog.primary.DCDRIndexMetadata;
import org.elasticsearch.dcdr.translog.primary.DCDRStats;
import org.elasticsearch.dcdr.translog.primary.DCDRTemplateMetadata;
import org.elasticsearch.search.sort.SortOrder;

/**
 * author weizijun
 * date：2019-09-17
 */
public class ReplicationIT extends DcdrIntegTestCase {
    public void testReplication() throws ExecutionException, InterruptedException, IOException {
        String primaryIndex = "primary_index";
        String replicaIndex = "replica_index";

        final int numberOfPrimaryShards = randomIntBetween(30, 50);
        createIndex(primaryIndex, numberOfPrimaryShards);

        CreateReplicationAction.Request request = createReplication(primaryIndex, replicaIndex);
        AcknowledgedResponse response = primaryClient().execute(CreateReplicationAction.INSTANCE, request).get();
        assertTrue(response.isAcknowledged());

        GetReplicationAction.Request getRequest = new GetReplicationAction.Request();
        getRequest.setPrimaryIndex(primaryIndex);
        GetReplicationAction.Response getResponse = primaryClient().execute(GetReplicationAction.INSTANCE, getRequest).get();

        DCDRIndexMetadata dcdrIndexMetadata = getResponse.getDcdrIndexMetadatas()
            .get(DCDRIndexMetadata.name(primaryIndex, replicaIndex, "replica_cluster"));
        assertNotNull(dcdrIndexMetadata);

        final int firstBatchNumDocs = randomIntBetween(2, 64);
        logger.info("Indexing [{}] docs as first batch", firstBatchNumDocs);
        for (int i = 0; i < firstBatchNumDocs; i++) {
            final String source = String.format(Locale.ROOT, "{\"field\":%d}", i);
            primaryClient().prepareIndex(primaryIndex, "type", Integer.toString(i)).setSource(source, XContentType.JSON).get();
        }

        assertReplicationDone(primaryIndex, replicaIndex, numberOfPrimaryShards);

        replicaClient().admin().indices().prepareRefresh(replicaIndex).get();
        SearchResponse searchResponse = replicaClient().prepareSearch(replicaIndex).get();
        assertEquals(searchResponse.getHits().getTotalHits().value, firstBatchNumDocs);

        DeleteReplicationAction.Request deleteRequest = deleteReplication(primaryIndex, replicaIndex);
        AcknowledgedResponse deleteResponse = primaryClient().execute(DeleteReplicationAction.INSTANCE, deleteRequest).get();
        assertTrue(deleteResponse.isAcknowledged());

        primaryClient().admin().indices().prepareDelete(primaryIndex).get();
        replicaClient().admin().indices().prepareDelete(replicaIndex).get();
    }

    public void testReplicationRecover() throws ExecutionException, InterruptedException, IOException {
        String primaryIndex = "primary_index";
        String replicaIndex = "replica_index";

        final int numberOfPrimaryShards = randomIntBetween(10, 20);
        createIndex(primaryIndex, numberOfPrimaryShards);

        final int firstBatchNumDocs = randomIntBetween(1000, 3000);
        logger.info("Indexing [{}] docs as first batch", firstBatchNumDocs);
        for (int i = 0; i < firstBatchNumDocs; i++) {
            final String source = String.format(Locale.ROOT, "{\"field\":%d}", i);
            primaryClient().prepareIndex(primaryIndex, "type", Integer.toString(i)).setSource(source, XContentType.JSON).get();
        }

        CreateReplicationAction.Request request = createReplication(primaryIndex, replicaIndex);
        AcknowledgedResponse response = primaryClient().execute(CreateReplicationAction.INSTANCE, request).get();
        assertTrue(response.isAcknowledged());

        assertReplicationDone(primaryIndex, replicaIndex, numberOfPrimaryShards);

        // 删除并创建新的replica索引
        AcknowledgedResponse deleteReplicaResponse = replicaClient().admin().indices().prepareDelete(replicaIndex).get();
        assertTrue(deleteReplicaResponse.isAcknowledged());

        // 继续写数据
        final int secondBatchNumDocs = randomIntBetween(2000, 5000);
        logger.info("Indexing [{}] docs as second batch", secondBatchNumDocs);
        for (int i = firstBatchNumDocs; i < firstBatchNumDocs + secondBatchNumDocs; i++) {
            final String source = String.format(Locale.ROOT, "{\"field\":%d}", i);
            primaryClient().prepareIndex(primaryIndex, "type", Integer.toString(i)).setSource(source, XContentType.JSON).get();
        }

        primaryClient().admin().indices().prepareRefresh(primaryIndex).get();

        createReplicaIndex(replicaIndex, numberOfPrimaryShards);

        assertReplicationDone(primaryIndex, replicaIndex, numberOfPrimaryShards);

        SearchResponse primaryResponse = primaryClient().prepareSearch(primaryIndex).get();
        // System.out.println("primary:" + primaryResponse);

        IndicesStatsResponse primaryStats = primaryClient().admin().indices().prepareStats(primaryIndex).all().get();
        Arrays.asList(primaryStats.getShards())
            .stream()
            .sorted(Comparator.comparingInt(o -> o.getShardRouting().shardId().id()))
            .forEach((shardStats) -> {
                if (shardStats.getShardRouting().primary()) {
//                    System.out.println(
//                        "shard=" + shardStats.getShardRouting().shardId().id() + ",count=" + shardStats.getSeqNoStats().getLocalCheckpoint()
//                    );
                }
            });

        replicaClient().admin().indices().prepareRefresh(replicaIndex).get();
        SearchResponse searchResponse = replicaClient().prepareSearch(replicaIndex).setSize(100).get();
        // System.out.println("current:" + searchResponse);
        IndicesStatsResponse replicaStats = replicaClient().admin().indices().prepareStats(replicaIndex).all().get();
        Arrays.asList(replicaStats.getShards())
            .stream()
            .sorted(Comparator.comparingInt(o -> o.getShardRouting().shardId().id()))
            .forEach((shardStats) -> {
                if (shardStats.getShardRouting().primary()) {
//                    System.out.println(
//                        "shard=" + shardStats.getShardRouting().shardId().id() + ",count=" + shardStats.getSeqNoStats().getLocalCheckpoint()
//                    );
                }
            });

        SearchResponse searchResponse2 = replicaClient().prepareSearch(replicaIndex).setSize(100).get();
        // System.out.println("replica:" + searchResponse2);

        assertEquals(searchResponse.getHits().getTotalHits().value, firstBatchNumDocs + secondBatchNumDocs);
        assertEquals(searchResponse.getHits().getTotalHits().value, primaryResponse.getHits().getTotalHits().value);
        assertEquals(searchResponse.getHits().getTotalHits().value, searchResponse2.getHits().getTotalHits().value);

        DeleteReplicationAction.Request deleteRequest = deleteReplication(primaryIndex, replicaIndex);
        AcknowledgedResponse deleteResponse = primaryClient().execute(DeleteReplicationAction.INSTANCE, deleteRequest).get();
        assertTrue(deleteResponse.isAcknowledged());

        primaryClient().admin().indices().prepareDelete(primaryIndex).get();
        replicaClient().admin().indices().prepareDelete(replicaIndex).get();
    }

    public void testMappingUpdate() throws Exception {
        String primaryIndex = "primary_index";
        String replicaIndex = "replica_index";

        final int numberOfPrimaryShards = randomIntBetween(1, 1);
        createIndex(primaryIndex, numberOfPrimaryShards);

        CreateReplicationAction.Request request = createReplication(primaryIndex, replicaIndex);
        AcknowledgedResponse response = primaryClient().execute(CreateReplicationAction.INSTANCE, request).get();
        assertTrue(response.isAcknowledged());

        final int firstBatchNumDocs = randomIntBetween(2, 64);
        logger.info("Indexing [{}] docs as first batch", firstBatchNumDocs);
        for (int i = 0; i < firstBatchNumDocs; i++) {
            final String source = String.format(Locale.ROOT, "{\"new_field\":%d}", i);
            primaryClient().prepareIndex(primaryIndex, "type", Integer.toString(i)).setSource(source, XContentType.JSON).get();
        }

        assertReplicationDone(primaryIndex, replicaIndex, numberOfPrimaryShards);

        replicaClient().admin().indices().prepareRefresh(replicaIndex).get();
        SearchResponse searchResponse = replicaClient().prepareSearch(replicaIndex).get();
        assertEquals(searchResponse.getHits().getTotalHits().value, firstBatchNumDocs);

        DeleteReplicationAction.Request deleteRequest = deleteReplication(primaryIndex, replicaIndex);
        AcknowledgedResponse deleteResponse = primaryClient().execute(DeleteReplicationAction.INSTANCE, deleteRequest).get();
        assertTrue(deleteResponse.isAcknowledged());

        primaryClient().admin().indices().prepareDelete(primaryIndex).get();
        replicaClient().admin().indices().prepareDelete(replicaIndex).get();
    }

    public void testReplicationTwoIndex() throws ExecutionException, InterruptedException, IOException {
        String primaryIndex = "primary_index";
        String replicaIndex = "replica_index";
        String replicaIndex2 = "replica_index2";

        final int numberOfPrimaryShards = randomIntBetween(3, 3);
        createIndex(primaryIndex, numberOfPrimaryShards);

        CreateReplicationAction.Request request = createReplication(primaryIndex, replicaIndex);
        AcknowledgedResponse response = primaryClient().execute(CreateReplicationAction.INSTANCE, request).get();
        assertTrue(response.isAcknowledged());

        CreateReplicationAction.Request request2 = createReplication(primaryIndex, replicaIndex2);
        AcknowledgedResponse response2 = primaryClient().execute(CreateReplicationAction.INSTANCE, request2).get();
        assertTrue(response2.isAcknowledged());

        final int firstBatchNumDocs = randomIntBetween(2, 64);
        logger.info("Indexing [{}] docs as first batch", firstBatchNumDocs);
        for (int i = 0; i < firstBatchNumDocs; i++) {
            final String source = String.format(Locale.ROOT, "{\"field\":%d}", i);
            primaryClient().prepareIndex(primaryIndex, "type", Integer.toString(i)).setSource(source, XContentType.JSON).get();
        }

        assertReplicationDone(primaryIndex, replicaIndex, numberOfPrimaryShards);
        assertReplicationDone(primaryIndex, replicaIndex2, numberOfPrimaryShards);

        replicaClient().admin().indices().prepareRefresh(replicaIndex).get();
        SearchResponse searchResponse = replicaClient().prepareSearch(replicaIndex).get();
        assertEquals(searchResponse.getHits().getTotalHits().value, firstBatchNumDocs);

        replicaClient().admin().indices().prepareRefresh(replicaIndex2).get();
        SearchResponse searchResponse2 = replicaClient().prepareSearch(replicaIndex2).get();
        assertEquals(searchResponse2.getHits().getTotalHits().value, firstBatchNumDocs);

        DeleteReplicationAction.Request deleteRequest = deleteReplication(primaryIndex, replicaIndex);
        AcknowledgedResponse deleteResponse = primaryClient().execute(DeleteReplicationAction.INSTANCE, deleteRequest).get();
        assertTrue(deleteResponse.isAcknowledged());

        DeleteReplicationAction.Request deleteRequest2 = deleteReplication(primaryIndex, replicaIndex2);
        AcknowledgedResponse deleteResponse2 = primaryClient().execute(DeleteReplicationAction.INSTANCE, deleteRequest2).get();
        assertTrue(deleteResponse2.isAcknowledged());

        primaryClient().admin().indices().prepareDelete(primaryIndex).get();
        replicaClient().admin().indices().prepareDelete(replicaIndex).get();
        replicaClient().admin().indices().prepareDelete(replicaIndex2).get();
    }

    public void testReplicationSwitch() throws ExecutionException, InterruptedException, IOException {
        String primaryIndex = "primary_index";
        String replicaIndex = "replica_index";

        final int numberOfPrimaryShards = randomIntBetween(1, 5);
        createIndex(primaryIndex, numberOfPrimaryShards);

        CreateReplicationAction.Request request = createReplication(primaryIndex, replicaIndex);
        AcknowledgedResponse response = primaryClient().execute(CreateReplicationAction.INSTANCE, request).get();
        assertTrue(response.isAcknowledged());

        assertReplicationDone(primaryIndex, replicaIndex, numberOfPrimaryShards);

        // 暂停链路
        SwitchReplicationAction.Request pauseRequest = createSwitchReplicationRequest(primaryIndex, replicaIndex);
        pauseRequest.setReplicationState(false);
        response = primaryClient().execute(SwitchReplicationAction.INSTANCE, pauseRequest).get();
        assertTrue(response.isAcknowledged());

        assertReplicationDone(primaryIndex, replicaIndex, numberOfPrimaryShards);

        final int firstBatchNumDocs = randomIntBetween(2, 64);
        logger.info("Indexing [{}] docs as first batch", firstBatchNumDocs);
        for (int i = 0; i < firstBatchNumDocs; i++) {
            final String source = String.format(Locale.ROOT, "{\"field\":%d}", i);
            primaryClient().prepareIndex(primaryIndex, "type", Integer.toString(i)).setSource(source, XContentType.JSON).get();
        }

        Thread.sleep(2000);

        replicaClient().admin().indices().prepareRefresh(replicaIndex).get();
        SearchResponse searchResponse = replicaClient().prepareSearch(replicaIndex).get();
        assertEquals(0, searchResponse.getHits().getTotalHits().value);

        ReplicationStatsAction.Request statsRequest = new ReplicationStatsAction.Request();
        statsRequest.setIndices(Strings.splitStringByCommaToArray(primaryIndex));
        ReplicationStatsAction.Response statsResponse = primaryClient().execute(ReplicationStatsAction.INSTANCE, statsRequest).get();
        assertEquals(statsResponse.getDcdrStats().size(), numberOfPrimaryShards);
        for (CompositeDCDRStats compositeDCDRStats : statsResponse.getDcdrStats()) {
            for (DCDRStats dcdrStats : compositeDCDRStats.getDcdrStatsList()) {
                if (!dcdrStats.getReplicaIndex().equals(replicaIndex)) {
                    continue;
                }

                assertFalse(dcdrStats.getReplicationState());
            }
        }

        // 恢复链路
        SwitchReplicationAction.Request resumeRequest = createSwitchReplicationRequest(primaryIndex, replicaIndex);
        resumeRequest.setReplicationState(true);
        response = primaryClient().execute(SwitchReplicationAction.INSTANCE, resumeRequest).get();
        assertTrue(response.isAcknowledged());

        assertReplicationDone(primaryIndex, replicaIndex, numberOfPrimaryShards);

        statsResponse = primaryClient().execute(ReplicationStatsAction.INSTANCE, statsRequest).get();
        assertEquals(statsResponse.getDcdrStats().size(), numberOfPrimaryShards);
        for (CompositeDCDRStats compositeDCDRStats : statsResponse.getDcdrStats()) {
            for (DCDRStats dcdrStats : compositeDCDRStats.getDcdrStatsList()) {
                if (!dcdrStats.getReplicaIndex().equals(replicaIndex)) {
                    continue;
                }

                assertTrue(dcdrStats.getReplicationState());
            }
        }

        replicaClient().admin().indices().prepareRefresh(replicaIndex).get();
        searchResponse = replicaClient().prepareSearch(replicaIndex).get();
        assertEquals(searchResponse.getHits().getTotalHits().value, firstBatchNumDocs);

        primaryClient().admin().indices().prepareDelete(primaryIndex).get();
        replicaClient().admin().indices().prepareDelete(replicaIndex).get();

        DeleteReplicationAction.Request deleteRequest = deleteReplication(primaryIndex, replicaIndex);
        AcknowledgedResponse deleteResponse = primaryClient().execute(DeleteReplicationAction.INSTANCE, deleteRequest).get();
        assertTrue(deleteResponse.isAcknowledged());
    }

    public void testReplicationStatis() throws ExecutionException, InterruptedException, IOException {
        String primaryIndex = "primary_index";
        String replicaIndex = "replica_index";

        final int numberOfPrimaryShards = randomIntBetween(1, 5);
        createIndex(primaryIndex, numberOfPrimaryShards);

        CreateReplicationAction.Request request = createReplication(primaryIndex, replicaIndex);
        AcknowledgedResponse response = primaryClient().execute(CreateReplicationAction.INSTANCE, request).get();
        assertTrue(response.isAcknowledged());

        final int firstBatchNumDocs = randomIntBetween(2, 64);
        logger.info("Indexing [{}] docs as first batch", firstBatchNumDocs);
        for (int i = 0; i < firstBatchNumDocs; i++) {
            final String source = String.format(Locale.ROOT, "{\"field\":%d}", i);
            primaryClient().prepareIndex(primaryIndex, "type", Integer.toString(i)).setSource(source, XContentType.JSON).get();
        }

        assertReplicationDone(primaryIndex, replicaIndex, numberOfPrimaryShards);

        replicaClient().admin().indices().prepareRefresh(replicaIndex).get();
        SearchResponse searchResponse = replicaClient().prepareSearch(replicaIndex).get();
        assertEquals(searchResponse.getHits().getTotalHits().value, firstBatchNumDocs);

        ReplicationStatsAction.Request statisRequest = new ReplicationStatsAction.Request();
        statisRequest.setIndices(Strings.splitStringByCommaToArray(primaryIndex));
        ReplicationStatsAction.Response statisResponse = primaryClient().execute(ReplicationStatsAction.INSTANCE, statisRequest).get();

        long succ = 0;
        for (CompositeDCDRStats compositeDCDRStats : statisResponse.getDcdrStats()) {
            succ += compositeDCDRStats.getDcdrStatsList().get(0).getOperationsSends();
        }

        assertEquals(succ, searchResponse.getHits().getTotalHits().value);

        primaryClient().admin().indices().prepareDelete(primaryIndex).get();
        replicaClient().admin().indices().prepareDelete(replicaIndex).get();

        DeleteReplicationAction.Request deleteRequest = deleteReplication(primaryIndex, replicaIndex);
        AcknowledgedResponse deleteResponse = primaryClient().execute(DeleteReplicationAction.INSTANCE, deleteRequest).get();
        assertTrue(deleteResponse.isAcknowledged());
    }

    public void testGetAllReplica() throws ExecutionException, InterruptedException {
        GetReplicationAction.Request request = new GetReplicationAction.Request();
        GetReplicationAction.Response response = primaryClient().execute(
            GetReplicationAction.INSTANCE,
            request
        ).get();

        assertNotNull(response.getDcdrIndexMetadatas());
    }

    public void testGetAllAutoReplica() throws ExecutionException, InterruptedException {
        GetAutoReplicationAction.Request request = new GetAutoReplicationAction.Request();
        GetAutoReplicationAction.Response response = primaryClient().execute(
            GetAutoReplicationAction.INSTANCE,
            request
        ).get();

        assertNotNull(response.getDcdrTemplateMetadatas());
    }

    public void testGetAllReplicaStats() throws ExecutionException, InterruptedException {
        ReplicationStatsAction.Request request = new ReplicationStatsAction.Request();
        ReplicationStatsAction.Response response = primaryClient().execute(
            ReplicationStatsAction.INSTANCE,
            request
        ).get();

        assertNotNull(response.getDcdrStats());
    }

    public void testAutoReplicationCreate() throws ExecutionException, InterruptedException, IOException {
        String templateName = "primary_index";
        putTemplate(templateName);

        // 创建DCDR模板链路
        CreateAutoReplicationAction.Request createAutoReplicationRequest = new CreateAutoReplicationAction.Request();
        createAutoReplicationRequest.setName(templateName);
        createAutoReplicationRequest.setTemplate(templateName);
        createAutoReplicationRequest.setReplicaCluster("replica_cluster");
        AcknowledgedResponse createAutoReplicationResponse = primaryClient().execute(
            CreateAutoReplicationAction.INSTANCE,
            createAutoReplicationRequest
        ).get();
        assertTrue(createAutoReplicationResponse.isAcknowledged());

        Thread.sleep(2000);

        GetAutoReplicationAction.Request getAutoReplicationRequest = new GetAutoReplicationAction.Request();
        getAutoReplicationRequest.setName(templateName);
        GetAutoReplicationAction.Response getAutoReplicationResponse = primaryClient().execute(
            GetAutoReplicationAction.INSTANCE,
            getAutoReplicationRequest
        ).get();

        DCDRTemplateMetadata dcdrTemplateMetadata = getAutoReplicationResponse.getDcdrTemplateMetadatas().get(templateName);
        assertEquals(dcdrTemplateMetadata.getName(), templateName);
        assertEquals(dcdrTemplateMetadata.getTemplate(), templateName);
        assertEquals(dcdrTemplateMetadata.getReplicaCluster(), "replica_cluster");

        String indexName = templateName + "_2019";

        final int numberOfPrimaryShards = randomIntBetween(1, 5);
        createIndex(indexName, numberOfPrimaryShards);

        final int firstBatchNumDocs = randomIntBetween(2, 64);
        logger.info("Indexing [{}] docs as first batch", firstBatchNumDocs);
        for (int i = 0; i < firstBatchNumDocs; i++) {
            final String source = String.format(Locale.ROOT, "{\"field\":%d}", i);
            primaryClient().prepareIndex(indexName, "type", Integer.toString(i)).setSource(source, XContentType.JSON).get();
        }

        assertReplicationDone(indexName, indexName, numberOfPrimaryShards);

        replicaClient().admin().indices().prepareRefresh(indexName).get();
        SearchResponse searchResponse = replicaClient().prepareSearch(indexName).get();
        assertEquals(searchResponse.getHits().getTotalHits().value, firstBatchNumDocs);

        primaryClient().admin().indices().prepareDelete(indexName).get();
        replicaClient().admin().indices().prepareDelete(indexName).get();

        DeleteAutoReplicationAction.Request deleteAutoReplicationRequest = new DeleteAutoReplicationAction.Request();
        deleteAutoReplicationRequest.setName(templateName);
        AcknowledgedResponse deleteAutoReplicationResponse = primaryClient().execute(
            DeleteAutoReplicationAction.INSTANCE,
            deleteAutoReplicationRequest
        ).get();
        assertTrue(deleteAutoReplicationResponse.isAcknowledged());

        Thread.sleep(2000);

        getAutoReplicationResponse = primaryClient().execute(
            GetAutoReplicationAction.INSTANCE,
            getAutoReplicationRequest
        ).get();

        assertNull(getAutoReplicationResponse.getDcdrTemplateMetadatas().get(templateName));

        deleteTemplate(templateName);
    }

    public void testReplicationRepeatData() throws Exception {
        String primaryIndex = "primary_index";
        String replicaIndex = "replica_index";

        final int numberOfPrimaryShards = randomIntBetween(30, 50);
        createIndex(primaryIndex, numberOfPrimaryShards);

        CreateReplicationAction.Request request = createReplication(primaryIndex, replicaIndex);
        AcknowledgedResponse response = primaryClient().execute(CreateReplicationAction.INSTANCE, request).get();
        assertTrue(response.isAcknowledged());

        GetReplicationAction.Request getRequest = new GetReplicationAction.Request();
        getRequest.setPrimaryIndex(primaryIndex);
        GetReplicationAction.Response getResponse = primaryClient().execute(GetReplicationAction.INSTANCE, getRequest).get();

        DCDRIndexMetadata dcdrIndexMetadata = getResponse.getDcdrIndexMetadatas()
            .get(DCDRIndexMetadata.name(primaryIndex, replicaIndex, "replica_cluster"));
        assertNotNull(dcdrIndexMetadata);

        final int firstBatchNumDocs = randomIntBetween(200, 640);
        int docNum = randomIntBetween(2, 10);
        logger.info("Indexing [{}] docs as first batch", firstBatchNumDocs);
        for (int i = 0; i < firstBatchNumDocs; i++) {
            final String source = String.format(Locale.ROOT, "{\"field\":%d}", i);
            String id = Integer.toString(i % docNum);
            primaryClient().prepareIndex(primaryIndex, "type", id).setSource(source, XContentType.JSON).get();
        }

        assertReplicationDone(primaryIndex, replicaIndex, numberOfPrimaryShards);

        replicaClient().admin().indices().prepareRefresh(replicaIndex).get();
        SearchResponse searchResponse = replicaClient().prepareSearch(replicaIndex).addSort("_id", SortOrder.ASC).get();
        assertEquals(searchResponse.getHits().getTotalHits().value, docNum);

        primaryClient().admin().indices().prepareRefresh(primaryIndex).get();
        SearchResponse primarySearchResponse = primaryClient().prepareSearch(primaryIndex).addSort("_id", SortOrder.ASC).get();
        assertEquals(searchResponse.getHits().getTotalHits().value, primarySearchResponse.getHits().getTotalHits().value);
        for (int i = 0; i < docNum; i++) {
            assertEquals(searchResponse.getHits().getHits()[i].getId(), primarySearchResponse.getHits().getHits()[i].getId());
            assertEquals(searchResponse.getHits().getHits()[i].getSourceAsString(), primarySearchResponse.getHits().getHits()[i].getSourceAsString());
        }

        DeleteReplicationAction.Request deleteRequest = deleteReplication(primaryIndex, replicaIndex);
        AcknowledgedResponse deleteResponse = primaryClient().execute(DeleteReplicationAction.INSTANCE, deleteRequest).get();
        assertTrue(deleteResponse.isAcknowledged());

        primaryClient().admin().indices().prepareDelete(primaryIndex).get();
        replicaClient().admin().indices().prepareDelete(replicaIndex).get();
    }


    public void testRetry() throws IOException, ExecutionException, InterruptedException {
        String primaryIndex = "primary_index";
        String replicaIndex = "replica_index";

        final int numberOfPrimaryShards = randomIntBetween(10, 30);
        createIndex(primaryIndex, numberOfPrimaryShards);

        CreateReplicationAction.Request request = createReplication(primaryIndex, replicaIndex);
        AcknowledgedResponse response = primaryClient().execute(CreateReplicationAction.INSTANCE, request).get();
        assertTrue(response.isAcknowledged());

        GetReplicationAction.Request getRequest = new GetReplicationAction.Request();
        getRequest.setPrimaryIndex(primaryIndex);
        GetReplicationAction.Response getResponse = primaryClient().execute(GetReplicationAction.INSTANCE, getRequest).get();

        DCDRIndexMetadata dcdrIndexMetadata = getResponse.getDcdrIndexMetadatas()
            .get(DCDRIndexMetadata.name(primaryIndex, replicaIndex, "replica_cluster"));
        assertNotNull(dcdrIndexMetadata);

        final int firstBatchNumDocs = randomIntBetween(2, 64);
        logger.info("Indexing [{}] docs as first batch", firstBatchNumDocs);
        for (int i = 0; i < firstBatchNumDocs; i++) {
            final String source = String.format(Locale.ROOT, "{\"field\":%d}", i);
            primaryClient().prepareIndex(primaryIndex, "type", Integer.toString(i)).setSource(source, XContentType.JSON).get();
        }

        assertReplicationDone(primaryIndex, replicaIndex, numberOfPrimaryShards);

        // close replica index
        replicaClient().admin().indices().prepareClose(replicaIndex).get();

        final int secondeBatchNumDocs = randomIntBetween(2, 64);
        logger.info("Indexing [{}] docs as first batch", secondeBatchNumDocs);
        for (int i = 0; i < secondeBatchNumDocs; i++) {
            final String source = String.format(Locale.ROOT, "{\"field\":%d}", i);
            primaryClient().prepareIndex(primaryIndex, "type", Integer.toString(i + firstBatchNumDocs)).setSource(source, XContentType.JSON).get();
        }

        Thread.sleep(1010);

        // open replica index
        replicaClient().admin().indices().prepareOpen(replicaIndex).get();

        assertReplicationDone(primaryIndex, replicaIndex, numberOfPrimaryShards);

        replicaClient().admin().indices().prepareRefresh(replicaIndex).get();
        SearchResponse searchResponse = replicaClient().prepareSearch(replicaIndex).get();
        assertEquals(searchResponse.getHits().getTotalHits().value, firstBatchNumDocs + secondeBatchNumDocs);

        DeleteReplicationAction.Request deleteRequest = deleteReplication(primaryIndex, replicaIndex);
        AcknowledgedResponse deleteResponse = primaryClient().execute(DeleteReplicationAction.INSTANCE, deleteRequest).get();
        assertTrue(deleteResponse.isAcknowledged());

        primaryClient().admin().indices().prepareDelete(primaryIndex).get();
        replicaClient().admin().indices().prepareDelete(replicaIndex).get();
    }

    private SwitchReplicationAction.Request createSwitchReplicationRequest(String primaryIndex, String replicaIndex) {
        SwitchReplicationAction.Request request = new SwitchReplicationAction.Request();
        request.setPrimaryIndex(primaryIndex);
        request.setReplicaIndex(replicaIndex);
        request.setReplicaCluster("replica_cluster");
        return request;
    }

    protected void putTemplate(String templateName) throws IOException {
        PutIndexTemplateRequest putIndexTemplateRequest = new PutIndexTemplateRequest(templateName);
        putIndexTemplateRequest.patterns(Collections.singletonList(templateName + "*"));
        assertAcked(primaryClient().admin().indices().putTemplate(putIndexTemplateRequest).actionGet());
    }

    protected void deleteTemplate(String templateName) throws IOException {
        DeleteIndexTemplateRequest deleteIndexTemplateRequest = new DeleteIndexTemplateRequest(templateName);
        assertAcked(primaryClient().admin().indices().deleteTemplate(deleteIndexTemplateRequest).actionGet());
    }

    protected void createIndex(String primaryIndex, int numberOfPrimaryShards) throws IOException {
        final String settings = getIndexSettings(numberOfPrimaryShards, between(0, 1), Collections.emptyMap());

        assertAcked(primaryClient().admin().indices().prepareCreate(primaryIndex).setSource(settings, XContentType.JSON));
        ensurePrimaryYellow(primaryIndex);
    }

    protected void createReplicaIndex(String replicaIndex, int numberOfPrimaryShards) throws IOException {
        final String replicaSettings = getIndexSettings(
            numberOfPrimaryShards,
            between(1, 4),
            singletonMap(DCDRSettings.DCDR_REPLICA_INDEX_SETTING.getKey(), "true")
        );
        assertAcked(replicaClient().admin().indices().prepareCreate(replicaIndex).setSource(replicaSettings, XContentType.JSON));
        ensureReplicaGreen(replicaIndex);
    }

}
