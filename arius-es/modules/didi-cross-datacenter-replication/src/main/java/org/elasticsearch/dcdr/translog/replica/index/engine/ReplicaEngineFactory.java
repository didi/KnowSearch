package org.elasticsearch.dcdr.translog.replica.index.engine;

import org.elasticsearch.index.engine.Engine;
import org.elasticsearch.index.engine.EngineConfig;
import org.elasticsearch.index.engine.EngineFactory;

/**
 * An engine factory for replica engines.
 */
public final class ReplicaEngineFactory implements EngineFactory {

    @Override
    public Engine newReadWriteEngine(final EngineConfig config) {
        return new ReplicaEngine(config);
    }

}
