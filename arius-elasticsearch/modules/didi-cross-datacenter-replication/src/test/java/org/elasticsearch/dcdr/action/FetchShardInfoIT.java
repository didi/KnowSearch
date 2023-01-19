package org.elasticsearch.dcdr.action;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.stats.ShardStats;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.dcdr.DcdrIntegTestCase;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;

import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;

/**
 * author weizijun
 * date：2019-12-11
 */
public class FetchShardInfoIT extends DcdrIntegTestCase {
    public void testFetch() throws IOException {
        String replicaIndex = "replica_index";
        final int numberOfPrimaryShards = randomIntBetween(1, 5);
        final String replicaSettings = getIndexSettings(numberOfPrimaryShards, between(1, 2), Collections.emptyMap());
        assertAcked(replicaClient().admin().indices().prepareCreate(replicaIndex).setSource(replicaSettings, XContentType.JSON));

        final int firstBatchNumDocs = randomIntBetween(2, 64);
        logger.info("Indexing [{}] docs as first batch", firstBatchNumDocs);
        for (int i = 0; i < firstBatchNumDocs; i++) {
            final String source = String.format(Locale.ROOT, "{\"field\":%d}", i);
            replicaClient().prepareIndex(replicaIndex, "type", Integer.toString(i)).setSource(source, XContentType.JSON).get();
        }

        replicaClient().admin().indices().prepareFlush(replicaIndex).get();
        replicaClient().admin().indices().prepareRefresh(replicaIndex).get();
        FetchShardInfoAction.Request request = new FetchShardInfoAction.Request(replicaIndex, 0);
        FetchShardInfoAction.Response response = replicaClient().execute(FetchShardInfoAction.INSTANCE, request).actionGet();
        assertNotNull(response.getDcdrShardInfo());
        assertEquals(response.getDcdrShardInfo().getShardId().getIndexName(), replicaIndex);
        assertEquals(response.getDcdrShardInfo().getShardId().getId(), 0);

        ShardStats shardStats = getShardStats(response.getDcdrShardInfo().getShardId(), replicaClient());
        assertEquals(shardStats.getCommitStats().getUserData().get("history_uuid"), response.getDcdrShardInfo().getHistoryUUID());
        assertEquals(shardStats.getSeqNoStats().getLocalCheckpoint(), response.getDcdrShardInfo().getCheckPoint());

        DiscoveryNode node = getTargetNode(response.getDcdrShardInfo().getShardId(), replicaClient());
        assertEquals(node, response.getDcdrShardInfo().getDiscoveryNode());

        try {
            replicaClient().execute(FetchShardInfoAction.INSTANCE, new FetchShardInfoAction.Request(replicaIndex, 10)).actionGet();
            fail();
        } catch (ElasticsearchException e) {
            assertTrue(e.getMessage().endsWith("0"));
        }

    }
}
