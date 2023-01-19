package org.elasticsearch.dcdr;

import org.elasticsearch.action.admin.indices.stats.IndicesStatsRequest;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsResponse;
import org.elasticsearch.action.admin.indices.stats.ShardStats;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.dcdr.translog.replica.bulk.TranslogSyncAction;
import org.elasticsearch.dcdr.translog.replica.bulk.TranslogSyncRequest;
import org.elasticsearch.dcdr.translog.replica.bulk.TranslogSyncResponse;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.index.translog.Translog;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static java.util.Collections.singletonMap;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;


/**
 * author weizijun
 * date：2019-10-08
 */
public class ReplicationBulkIT extends DcdrIntegTestCase {
    private static final byte[] SOURCE = "{\"field\": 1}".getBytes(StandardCharsets.UTF_8);

    /**
     * 写重复的translog
     */
    public void testAppendSameTranslog() throws IOException, ExecutionException, InterruptedException {
        String replicaIndex = "replica_index";
        final int numberOfPrimaryShards = 1;
        final String replicaSettings = getIndexSettings(numberOfPrimaryShards, between(1, 2), singletonMap(DCDRSettings.DCDR_REPLICA_INDEX_SETTING.getKey(), "true"));
        assertAcked(replicaClient().admin().indices().prepareCreate(replicaIndex).setSource(replicaSettings, XContentType.JSON));

        IndicesStatsRequest stats = new IndicesStatsRequest();
        stats.indices(replicaIndex);
        stats.translog(true);
        IndicesStatsResponse indicesStatsResponse = replicaClient().admin().indices().stats(stats).get();

        ShardId replicaShardId = null;
        String replicaHistoryUuid = null;
        ShardStats[] shardStatsArr = indicesStatsResponse.getShards();
        for (ShardStats shardStats : shardStatsArr) {
            if (shardStats.getShardRouting().primary()) {
                replicaShardId = shardStats.getShardRouting().shardId();
                replicaHistoryUuid = shardStats.getCommitStats().getUserData().get("history_uuid");
                break;
            }
        }

        final int numDocs = randomIntBetween(2, 64);
        List<Translog.Operation> translogs = new ArrayList<>(numDocs);
        for (int i = 0; i < numDocs; ++i) {
            Translog.Index index = new Translog.Index("type", "1", i, 1, 1, SOURCE, null, -1);
            translogs.add(index);
        }

        TranslogSyncRequest bulkShardOperationsRequest = new TranslogSyncRequest(replicaShardId, replicaHistoryUuid, translogs, numDocs);
        TranslogSyncResponse bulkShardOperationsResponse = replicaClient().execute(TranslogSyncAction.INSTANCE, bulkShardOperationsRequest).get();
        // System.out.println("checkpoint:" + bulkShardOperationsResponse.getGlobalCheckpoint() + "," + bulkShardOperationsResponse.getMaxSeqNo());
        assertEquals(numDocs-1, bulkShardOperationsResponse.getLocalCheckpoint());
        assertEquals(numDocs-1, bulkShardOperationsResponse.getMaxSeqNo());

        replicaClient().admin().indices().prepareRefresh(replicaIndex).get();

        SearchResponse response = replicaClient().prepareSearch(replicaIndex).get();
        // System.out.println("totalHits:" + response.getHits().getTotalHits());
        assertEquals(response.getHits().getTotalHits().value, 1);

        TranslogSyncResponse bulkShardOperationsResponse2 = replicaClient().execute(TranslogSyncAction.INSTANCE, bulkShardOperationsRequest).get();
        // System.out.println("checkpoint:" + bulkShardOperationsResponse2.getGlobalCheckpoint() + "," + bulkShardOperationsResponse2.getMaxSeqNo());
        assertEquals(numDocs-1, bulkShardOperationsResponse2.getLocalCheckpoint());
        assertEquals(numDocs-1, bulkShardOperationsResponse2.getMaxSeqNo());

        replicaClient().admin().indices().prepareRefresh(replicaIndex).get();

        response = replicaClient().prepareSearch(replicaIndex).get();
        // System.out.println("totalHits:" + response.getHits().getTotalHits());
        assertEquals(response.getHits().getTotalHits().value, 1);

        replicaClient().admin().indices().prepareDelete(replicaIndex).get();
    }

    public void testAppendDisorderTranslog() throws IOException, ExecutionException, InterruptedException {
        String replicaIndex = "replica_index";
        final int numberOfPrimaryShards = 1;
        final String replicaSettings = getIndexSettings(numberOfPrimaryShards, between(1, 2), singletonMap(DCDRSettings.DCDR_REPLICA_INDEX_SETTING.getKey(), "true"));
        assertAcked(replicaClient().admin().indices().prepareCreate(replicaIndex).setSource(replicaSettings, XContentType.JSON));

        IndicesStatsRequest stats = new IndicesStatsRequest();
        stats.indices(replicaIndex);
        stats.translog(true);
        IndicesStatsResponse indicesStatsResponse = replicaClient().admin().indices().stats(stats).get();

        ShardId replicaShardId = null;
        String replicaHistoryUuid = null;
        ShardStats[] shardStatsArr = indicesStatsResponse.getShards();
        for (ShardStats shardStats : shardStatsArr) {
            if (shardStats.getShardRouting().primary()) {
                replicaShardId = shardStats.getShardRouting().shardId();
                replicaHistoryUuid = shardStats.getCommitStats().getUserData().get("history_uuid");
                break;
            }
        }

        final int firstBatchNumDocs = randomIntBetween(2, 64);
        List<Translog.Operation> translogs = new ArrayList<>(firstBatchNumDocs);
        for (int i = 0; i < firstBatchNumDocs; ++i) {
            Translog.Index index = new Translog.Index("type", "1", i, 1, 1, SOURCE, null, -1);
            translogs.add(index);
        }

        final int secondBatchNumDocs = randomIntBetween(2, 64);
        List<Translog.Operation> translogs2 = new ArrayList<>(secondBatchNumDocs);
        for (int i = firstBatchNumDocs; i < (firstBatchNumDocs + secondBatchNumDocs); ++i) {
            Translog.Index index = new Translog.Index("type", "1", i, 1, 1, SOURCE, null, -1);
            translogs2.add(index);
        }

        TranslogSyncRequest bulkShardOperationsRequest2 = new TranslogSyncRequest(replicaShardId, replicaHistoryUuid, translogs2, firstBatchNumDocs+secondBatchNumDocs);
        TranslogSyncResponse bulkShardOperationsResponse2 = replicaClient().execute(TranslogSyncAction.INSTANCE, bulkShardOperationsRequest2).get();
        System.out.println("checkpoint:" + bulkShardOperationsResponse2.getLocalCheckpoint() + "," + bulkShardOperationsResponse2.getMaxSeqNo());
        assertEquals(-1, bulkShardOperationsResponse2.getLocalCheckpoint());
        assertEquals(firstBatchNumDocs+secondBatchNumDocs-1, bulkShardOperationsResponse2.getMaxSeqNo());

        TranslogSyncRequest bulkShardOperationsRequest = new TranslogSyncRequest(replicaShardId, replicaHistoryUuid, translogs, firstBatchNumDocs);
        TranslogSyncResponse bulkShardOperationsResponse = replicaClient().execute(TranslogSyncAction.INSTANCE, bulkShardOperationsRequest).get();
        System.out.println("checkpoint:" + bulkShardOperationsResponse.getLocalCheckpoint() + "," + bulkShardOperationsResponse.getMaxSeqNo());
        assertEquals(firstBatchNumDocs+secondBatchNumDocs-1, bulkShardOperationsResponse.getLocalCheckpoint());
        assertEquals(firstBatchNumDocs+secondBatchNumDocs-1, bulkShardOperationsResponse.getMaxSeqNo());

        replicaClient().admin().indices().prepareRefresh(replicaIndex).get();

        SearchResponse searchResponse = replicaClient().prepareSearch(replicaIndex).get();
        System.out.println("current:" + searchResponse);
        assertEquals(searchResponse.getHits().getTotalHits().value, 1);

        GetResponse getResponse = replicaClient().prepareGet(replicaIndex, "type", "1").get();
        System.out.println("version:" + getResponse.getVersion());
        assertEquals(getResponse.getVersion(), 1);

        replicaClient().admin().indices().prepareDelete(replicaIndex).get();
    }
}
