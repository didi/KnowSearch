package org.elasticsearch.dcdr.translog.replica.index.engine;

import java.util.OptionalLong;

import org.elasticsearch.index.engine.VersionConflictEngineException;
import org.elasticsearch.index.shard.ShardId;

/**
 * An exception represents that an operation was processed before on the {@link ReplicaEngine} of the primary of a follower.
 * The field {@code existingPrimaryTerm} is empty only if the operation is below the global checkpoint; otherwise it should be non-empty.
 */
public final class AlreadyProcessedReplicaEngineException extends VersionConflictEngineException {
    private final long seqNo;
    private final OptionalLong existingPrimaryTerm;

    AlreadyProcessedReplicaEngineException(ShardId shardId, long seqNo, OptionalLong existingPrimaryTerm) {
        super(shardId, "operation [{}] was processed before with term [{}]", null, seqNo, existingPrimaryTerm);
        this.seqNo = seqNo;
        this.existingPrimaryTerm = existingPrimaryTerm;
    }

    public long getSeqNo() {
        return seqNo;
    }

    public OptionalLong getExistingPrimaryTerm() {
        return existingPrimaryTerm;
    }
}
