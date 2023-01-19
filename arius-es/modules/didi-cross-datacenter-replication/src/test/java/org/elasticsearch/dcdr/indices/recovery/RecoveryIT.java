package org.elasticsearch.dcdr.indices.recovery;

import com.carrotsearch.hppc.cursors.ObjectObjectCursor;
import org.elasticsearch.action.admin.indices.shards.IndicesShardStoresAction;
import org.elasticsearch.action.admin.indices.shards.IndicesShardStoresRequest;
import org.elasticsearch.action.admin.indices.shards.IndicesShardStoresResponse;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsRequest;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsResponse;
import org.elasticsearch.action.admin.indices.stats.ShardStats;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.collect.ImmutableOpenIntMap;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.dcdr.DcdrIntegTestCase;
import org.elasticsearch.index.seqno.SequenceNumbers;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.indices.recovery.RecoveryFinalizeRecoveryRequest;
import org.elasticsearch.transport.EmptyTransportResponseHandler;
import org.elasticsearch.transport.TransportRequestOptions;
import org.elasticsearch.transport.TransportService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;

public class RecoveryIT extends DcdrIntegTestCase {

    public void testStartRecovery() throws IOException {
        String replicaIndex = "replica_index";
        final int numberOfPrimaryShards = randomIntBetween(1, 1);
        final String replicaSettings = getIndexSettings(numberOfPrimaryShards, between(0, 0), Collections.emptyMap());
        assertAcked(replicaClient().admin().indices().prepareCreate(replicaIndex).setSource(replicaSettings, XContentType.JSON));

        final int firstBatchNumDocs = randomIntBetween(2, 64);
        logger.info("Indexing [{}] docs as first batch", firstBatchNumDocs);
        for (int i = 0; i < firstBatchNumDocs; i++) {
            final String source = String.format(Locale.ROOT, "{\"field\":%d}", i);
            replicaClient().prepareIndex(replicaIndex, "type", Integer.toString(i)).setSource(source, XContentType.JSON).get();
        }

        replicaClient().admin().indices().prepareFlush(replicaIndex).get();
        replicaClient().admin().indices().prepareRefresh(replicaIndex).get();

        IndicesStatsResponse indicesStatsResponse = replicaClient().admin().indices().prepareStats(replicaIndex).get();
        ShardId shardId = indicesStatsResponse.getShards()[0].getShardRouting().shardId();
        DiscoveryNode node = getTargetNode(shardId, replicaClient());

        TransportService datanodeTransportService = getReplicaCluster().getDataNodeInstance(TransportService.class);
        StartRecoveryResponse response = datanodeTransportService.submitRequest(node, PeerRecoveryTargetService.Actions.START_RECOVERY, new StartRecoveryRequest(shardId),
            TransportRequestOptions.builder().withTimeout(100000).build(),
            StartRecoveryResponse.HANDLER).txGet();
        // System.out.println(response);

        datanodeTransportService.submitRequest(node, PeerRecoveryTargetService.Actions.FINALIZE,
            new RecoveryFinalizeRecoveryRequest(response.recoveryId(), shardId, 1l, SequenceNumbers.UNASSIGNED_SEQ_NO),
            TransportRequestOptions.builder().withTimeout(100000).build(),
            EmptyTransportResponseHandler.INSTANCE_SAME).txGet();

        SearchResponse searchResponse = replicaClient().prepareSearch(replicaIndex).get();
        // System.out.println(searchResponse.getHits().getTotalHits());

        ShardStats shardStats = getShardStats(shardId, replicaClient());
        // System.out.println(shardStats);
    }
}
