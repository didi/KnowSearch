package org.elasticsearch.dcdr.translog.replica.index.engine;

import java.io.IOException;
import java.util.Optional;
import java.util.OptionalLong;

import org.elasticsearch.Assertions;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.dcdr.DCDRSettings;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.index.engine.Engine;
import org.elasticsearch.index.engine.EngineConfig;
import org.elasticsearch.index.engine.InternalEngine;
import org.elasticsearch.index.seqno.SequenceNumbers;
import org.elasticsearch.rest.RestStatus;

/**
 * An engine implementation for replica shards.
 */
public final class ReplicaEngine extends InternalEngine {


    /**
     * Construct a new replica engine with the specified engine configuration.
     *
     * @param engineConfig the engine configuration
     */
    ReplicaEngine(final EngineConfig engineConfig) {
        super(validateEngineConfig(engineConfig));
    }

    private static EngineConfig validateEngineConfig(final EngineConfig engineConfig) {
        if (DCDRSettings.DCDR_REPLICA_INDEX_SETTING.get(engineConfig.getIndexSettings().getSettings()) == false) {
            throw new IllegalArgumentException("a replica engine can not be constructed for a non-following index");
        }
        return engineConfig;
    }

    private void preFlight(final Operation operation) {
        assert operation.seqNo() != SequenceNumbers.UNASSIGNED_SEQ_NO;
        assert (operation.origin() == Engine.Operation.Origin.PRIMARY) == (operation.versionType() == VersionType.EXTERNAL) :
            "invalid version_type in a replica engine; version_type=" + operation.versionType() + "origin=" + operation.origin();
        if (operation.seqNo() == SequenceNumbers.UNASSIGNED_SEQ_NO) {
            throw new ElasticsearchStatusException("a replica engine does not accept operations without an assigned sequence number",
                RestStatus.FORBIDDEN);
        }
    }

    @Override
    protected InternalEngine.IndexingStrategy indexingStrategyForOperation(final Index index) throws IOException {
        preFlight(index);
        if (index.origin() == Operation.Origin.PRIMARY && hasBeenProcessedBefore(index)) {
            /*
             * The existing operation in this engine was probably assigned the term of the previous primary shard which is different
             * from the term of the current operation. If the current operation arrives on replicas before the previous operation,
             * then the Lucene content between the primary and replicas are not identical (primary terms are different). We can safely
             * skip the existing operations below the global checkpoint, however must replicate the ones above the global checkpoint
             * but with the previous primary term (not the current term of the operation) in order to guarantee the consistency
             * between the primary and replicas (see TransportBulkShardOperationsAction#shardOperationOnPrimary).
             */
            final AlreadyProcessedReplicaEngineException error = new AlreadyProcessedReplicaEngineException(
                shardId, index.seqNo(), OptionalLong.empty());
            return IndexingStrategy.skipDueToVersionConflict(error, false, index.version());
        } else {
            return planIndexingAsNonPrimary(index);
        }
    }

    @Override
    protected InternalEngine.DeletionStrategy deletionStrategyForOperation(final Delete delete) throws IOException {
        preFlight(delete);
        if (delete.origin() == Operation.Origin.PRIMARY && hasBeenProcessedBefore(delete)) {
            // See the comment in #indexingStrategyForOperation for the explanation why we can safely skip this operation.
            final AlreadyProcessedReplicaEngineException error = new AlreadyProcessedReplicaEngineException(
                shardId, delete.seqNo(), OptionalLong.empty());
            return DeletionStrategy.skipDueToVersionConflict(error, delete.version(), false);
        } else {
            return planDeletionAsNonPrimary(delete);
        }
    }

    @Override
    protected Optional<Exception> preFlightCheckForNoOp(NoOp noOp) throws IOException {
        if (noOp.origin() == Operation.Origin.PRIMARY && hasBeenProcessedBefore(noOp)) {
            // See the comment in #indexingStrategyForOperation for the explanation why we can safely skip this operation.
            // final OptionalLong existingTerm = lookupPrimaryTerm(noOp.seqNo());
            return Optional.of(new AlreadyProcessedReplicaEngineException(shardId, noOp.seqNo(), OptionalLong.empty()));
        } else {
            return super.preFlightCheckForNoOp(noOp);
        }
    }

    @Override
    protected long generateSeqNoForOperationOnPrimary(final Operation operation) {
        assert operation.origin() == Operation.Origin.PRIMARY;
        assert operation.seqNo() >= 0 : "ops should have an assigned seq no. but was: " + operation.seqNo();
        markSeqNoAsSeen(operation.seqNo()); // even though we're not generating a sequence number, we mark it as seen
        return operation.seqNo();
    }

    @Override
    protected void advanceMaxSeqNoOfUpdatesOrDeletesOnPrimary(long seqNo) {
        if (Assertions.ENABLED) {
            final long localCheckpoint = getProcessedLocalCheckpoint();
            final long maxSeqNoOfUpdates = getMaxSeqNoOfUpdatesOrDeletes();
            assert localCheckpoint < maxSeqNoOfUpdates || maxSeqNoOfUpdates >= seqNo :
                "maxSeqNoOfUpdates is not advanced local_checkpoint=" + localCheckpoint + " msu=" + maxSeqNoOfUpdates + " seq_no=" + seqNo;
        }
        super.advanceMaxSeqNoOfUpdatesOrDeletesOnPrimary(seqNo); // extra safe in production code
    }

    @Override
    public int fillSeqNoGaps(long primaryTerm) throws IOException {
        // a noop implementation, because replica shard does not own the history but the leader shard does.
        return 0;
    }

    @Override
    protected boolean assertPrimaryIncomingSequenceNumber(final Operation.Origin origin, final long seqNo) {
        assert seqNo != SequenceNumbers.UNASSIGNED_SEQ_NO : "primary operations on a replica index must have an assigned sequence number";
        return true;
    }

    @Override
    protected boolean assertNonPrimaryOrigin(final Operation operation) {
        return true;
    }

    @Override
    protected boolean assertPrimaryCanOptimizeAddDocument(final Index index) {
        assert index.version() == 1 && index.versionType() == VersionType.EXTERNAL
                : "version [" + index.version() + "], type [" + index.versionType() + "]";
        return true;
    }

    @Override
    public void verifyEngineBeforeIndexClosing() throws IllegalStateException {
    }
}
