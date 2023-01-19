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
package org.elasticsearch.gateway;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FilterDirectory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.SimpleFSDirectory;
import org.elasticsearch.Version;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.coordination.CoordinationMetaData;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.common.UUIDs;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.ClusterSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.util.BigArrays;
import org.elasticsearch.common.util.MockBigArrays;
import org.elasticsearch.common.util.MockPageCacheRecycler;
import org.elasticsearch.core.internal.io.IOUtils;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.NodeEnvironment;
import org.elasticsearch.env.NodeMetaData;
import org.elasticsearch.gateway.PersistedClusterStateService.Writer;
import org.elasticsearch.index.Index;
import org.elasticsearch.indices.breaker.NoneCircuitBreakerService;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.test.MockLogAppender;
import org.elasticsearch.test.junit.annotations.TestLogging;

import java.io.IOError;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.nullValue;

public class PersistedClusterStateServiceTests extends ESTestCase {

    private PersistedClusterStateService newPersistedClusterStateService(NodeEnvironment nodeEnvironment) {
        return new PersistedClusterStateService(nodeEnvironment, xContentRegistry(),
            usually()
                ? BigArrays.NON_RECYCLING_INSTANCE
                : new MockBigArrays(new MockPageCacheRecycler(Settings.EMPTY), new NoneCircuitBreakerService()),
            new ClusterSettings(Settings.EMPTY, ClusterSettings.BUILT_IN_CLUSTER_SETTINGS),
            () -> 0L);
    }

    public void testPersistsAndReloadsTerm() throws IOException {
        try (NodeEnvironment nodeEnvironment = newNodeEnvironment(createDataPaths())) {
            final PersistedClusterStateService persistedClusterStateService = newPersistedClusterStateService(nodeEnvironment);
            final long newTerm = randomNonNegativeLong();

            assertThat(persistedClusterStateService.loadBestOnDiskState().currentTerm, equalTo(0L));
            try (Writer writer = persistedClusterStateService.createWriter()) {
                writer.writeFullStateAndCommit(newTerm, ClusterState.EMPTY_STATE);
                assertThat(persistedClusterStateService.loadBestOnDiskState().currentTerm, equalTo(newTerm));
            }

            assertThat(persistedClusterStateService.loadBestOnDiskState().currentTerm, equalTo(newTerm));
        }
    }

    public void testPersistsAndReloadsGlobalMetadata() throws IOException {
        try (NodeEnvironment nodeEnvironment = newNodeEnvironment(createDataPaths())) {
            final PersistedClusterStateService persistedClusterStateService = newPersistedClusterStateService(nodeEnvironment);
            final String clusterUUID = UUIDs.randomBase64UUID(random());
            final long version = randomLongBetween(1L, Long.MAX_VALUE);

            ClusterState clusterState = loadPersistedClusterState(persistedClusterStateService);
            try (Writer writer = persistedClusterStateService.createWriter()) {
                writer.writeFullStateAndCommit(0L, ClusterState.builder(clusterState)
                    .metaData(MetaData.builder(clusterState.metaData())
                        .clusterUUID(clusterUUID)
                        .clusterUUIDCommitted(true)
                        .version(version))
                    .incrementVersion().build());
                clusterState = loadPersistedClusterState(persistedClusterStateService);
                assertThat(clusterState.metaData().clusterUUID(), equalTo(clusterUUID));
                assertTrue(clusterState.metaData().clusterUUIDCommitted());
                assertThat(clusterState.metaData().version(), equalTo(version));
            }

            try (Writer writer = persistedClusterStateService.createWriter()) {
                writer.writeFullStateAndCommit(0L, ClusterState.builder(clusterState)
                    .metaData(MetaData.builder(clusterState.metaData())
                        .clusterUUID(clusterUUID)
                        .clusterUUIDCommitted(true)
                        .version(version + 1))
                    .incrementVersion().build());
            }

            clusterState = loadPersistedClusterState(persistedClusterStateService);
            assertThat(clusterState.metaData().clusterUUID(), equalTo(clusterUUID));
            assertTrue(clusterState.metaData().clusterUUIDCommitted());
            assertThat(clusterState.metaData().version(), equalTo(version + 1));
        }
    }

    private static void writeState(Writer writer, long currentTerm, ClusterState clusterState,
                                   ClusterState previousState) throws IOException {
        if (randomBoolean() || clusterState.term() != previousState.term() || writer.fullStateWritten == false) {
            writer.writeFullStateAndCommit(currentTerm, clusterState);
        } else {
            writer.writeIncrementalStateAndCommit(currentTerm, previousState, clusterState);
        }
    }

    public void testLoadsFreshestState() throws IOException {
        final Path[] dataPaths = createDataPaths();
        final long freshTerm = randomLongBetween(1L, Long.MAX_VALUE);
        final long staleTerm = randomBoolean() ? freshTerm : randomLongBetween(1L, freshTerm);
        final long freshVersion = randomLongBetween(2L, Long.MAX_VALUE);
        final long staleVersion = staleTerm == freshTerm ? randomLongBetween(1L, freshVersion - 1) : randomLongBetween(1L, Long.MAX_VALUE);

        final HashSet<Path> unimportantPaths = Arrays.stream(dataPaths).collect(Collectors.toCollection(HashSet::new));

        try (NodeEnvironment nodeEnvironment = newNodeEnvironment(dataPaths)) {
            final ClusterState clusterState = loadPersistedClusterState(newPersistedClusterStateService(nodeEnvironment));
            try (Writer writer = newPersistedClusterStateService(nodeEnvironment).createWriter()) {
                writeState(writer, staleTerm,
                    ClusterState.builder(clusterState).version(staleVersion)
                        .metaData(MetaData.builder(clusterState.metaData()).coordinationMetaData(
                            CoordinationMetaData.builder(clusterState.coordinationMetaData()).term(staleTerm).build())).build(),
                    clusterState);
            }
        }

        final Path freshPath = randomFrom(dataPaths);
        try (NodeEnvironment nodeEnvironment = newNodeEnvironment(new Path[]{freshPath})) {
            unimportantPaths.remove(freshPath);
            try (Writer writer = newPersistedClusterStateService(nodeEnvironment).createWriter()) {
                final ClusterState clusterState = loadPersistedClusterState(newPersistedClusterStateService(nodeEnvironment));
                writeState(writer, freshTerm,
                    ClusterState.builder(clusterState).version(freshVersion)
                        .metaData(MetaData.builder(clusterState.metaData()).coordinationMetaData(
                            CoordinationMetaData.builder(clusterState.coordinationMetaData()).term(freshTerm).build())).build(),
                    clusterState);
            }
        }

        if (randomBoolean() && unimportantPaths.isEmpty() == false) {
            IOUtils.rm(randomFrom(unimportantPaths));
        }

        // verify that the freshest state is chosen
        try (NodeEnvironment nodeEnvironment = newNodeEnvironment(dataPaths)) {
            final PersistedClusterStateService.OnDiskState onDiskState = newPersistedClusterStateService(nodeEnvironment)
                .loadBestOnDiskState();
            final ClusterState clusterState = clusterStateFromMetadata(onDiskState.lastAcceptedVersion, onDiskState.metaData);
            assertThat(clusterState.term(), equalTo(freshTerm));
            assertThat(clusterState.version(), equalTo(freshVersion));
        }
    }

    public void testFailsOnMismatchedNodeIds() throws IOException {
        final Path[] dataPaths1 = createDataPaths();
        final Path[] dataPaths2 = createDataPaths();

        final String[] nodeIds = new String[2];

        try (NodeEnvironment nodeEnvironment = newNodeEnvironment(dataPaths1)) {
            nodeIds[0] = nodeEnvironment.nodeId();
            try (Writer writer = newPersistedClusterStateService(nodeEnvironment).createWriter()) {
                final ClusterState clusterState = loadPersistedClusterState(newPersistedClusterStateService(nodeEnvironment));
                writer.writeFullStateAndCommit(0L,
                    ClusterState.builder(clusterState).version(randomLongBetween(1L, Long.MAX_VALUE)).build());
            }
        }

        try (NodeEnvironment nodeEnvironment = newNodeEnvironment(dataPaths2)) {
            nodeIds[1] = nodeEnvironment.nodeId();
            try (Writer writer = newPersistedClusterStateService(nodeEnvironment).createWriter()) {
                final ClusterState clusterState = loadPersistedClusterState(newPersistedClusterStateService(nodeEnvironment));
                writer.writeFullStateAndCommit(0L,
                    ClusterState.builder(clusterState).version(randomLongBetween(1L, Long.MAX_VALUE)).build());
            }
        }

        NodeMetaData.FORMAT.cleanupOldFiles(Long.MAX_VALUE, dataPaths2);

        final Path[] combinedPaths = Stream.concat(Arrays.stream(dataPaths1), Arrays.stream(dataPaths2)).toArray(Path[]::new);

        final String failure = expectThrows(IllegalStateException.class, () -> newNodeEnvironment(combinedPaths)).getMessage();
        assertThat(failure,
            allOf(containsString("unexpected node ID in metadata"), containsString(nodeIds[0]), containsString(nodeIds[1])));
        assertTrue("[" + failure + "] should match " + Arrays.toString(dataPaths2),
            Arrays.stream(dataPaths2).anyMatch(p -> failure.contains(p.toString())));

        // verify that loadBestOnDiskState has same check
        final String message = expectThrows(IllegalStateException.class,
            () -> new PersistedClusterStateService(Stream.of(combinedPaths).map(path -> NodeEnvironment.resolveNodePath(path, 0))
                .toArray(Path[]::new), nodeIds[0], xContentRegistry(), BigArrays.NON_RECYCLING_INSTANCE,
                new ClusterSettings(Settings.EMPTY, ClusterSettings.BUILT_IN_CLUSTER_SETTINGS), () -> 0L,
                randomBoolean()).loadBestOnDiskState()).getMessage();
        assertThat(message,
            allOf(containsString("unexpected node ID in metadata"), containsString(nodeIds[0]), containsString(nodeIds[1])));
        assertTrue("[" + message + "] should match " + Arrays.toString(dataPaths2),
            Arrays.stream(dataPaths2).anyMatch(p -> message.contains(p.toString())));
    }

    public void testFailsOnMismatchedCommittedClusterUUIDs() throws IOException {
        final Path[] dataPaths1 = createDataPaths();
        final Path[] dataPaths2 = createDataPaths();
        final Path[] combinedPaths = Stream.concat(Arrays.stream(dataPaths1), Arrays.stream(dataPaths2)).toArray(Path[]::new);

        final String clusterUUID1 = UUIDs.randomBase64UUID(random());
        final String clusterUUID2 = UUIDs.randomBase64UUID(random());

        // first establish consistent node IDs and write initial metadata
        try (NodeEnvironment nodeEnvironment = newNodeEnvironment(combinedPaths)) {
            try (Writer writer = newPersistedClusterStateService(nodeEnvironment).createWriter()) {
                final ClusterState clusterState = loadPersistedClusterState(newPersistedClusterStateService(nodeEnvironment));
                assertFalse(clusterState.metaData().clusterUUIDCommitted());
                writer.writeFullStateAndCommit(0L, clusterState);
            }
        }

        try (NodeEnvironment nodeEnvironment = newNodeEnvironment(dataPaths1)) {
            try (Writer writer = newPersistedClusterStateService(nodeEnvironment).createWriter()) {
                final ClusterState clusterState = loadPersistedClusterState(newPersistedClusterStateService(nodeEnvironment));
                assertFalse(clusterState.metaData().clusterUUIDCommitted());
                writer.writeFullStateAndCommit(0L, ClusterState.builder(clusterState)
                    .metaData(MetaData.builder(clusterState.metaData())
                        .clusterUUID(clusterUUID1)
                        .clusterUUIDCommitted(true)
                        .version(1))
                    .incrementVersion().build());
            }
        }

        try (NodeEnvironment nodeEnvironment = newNodeEnvironment(dataPaths2)) {
            try (Writer writer = newPersistedClusterStateService(nodeEnvironment).createWriter()) {
                final ClusterState clusterState = loadPersistedClusterState(newPersistedClusterStateService(nodeEnvironment));
                assertFalse(clusterState.metaData().clusterUUIDCommitted());
                writer.writeFullStateAndCommit(0L, ClusterState.builder(clusterState)
                    .metaData(MetaData.builder(clusterState.metaData())
                        .clusterUUID(clusterUUID2)
                        .clusterUUIDCommitted(true)
                        .version(1))
                    .incrementVersion().build());
            }
        }

        try (NodeEnvironment nodeEnvironment = newNodeEnvironment(combinedPaths)) {
            final String message = expectThrows(IllegalStateException.class,
                () -> newPersistedClusterStateService(nodeEnvironment).loadBestOnDiskState()).getMessage();
            assertThat(message,
                allOf(containsString("mismatched cluster UUIDs in metadata"), containsString(clusterUUID1), containsString(clusterUUID2)));
            assertTrue("[" + message + "] should match " + Arrays.toString(dataPaths1),
                Arrays.stream(dataPaths1).anyMatch(p -> message.contains(p.toString())));
            assertTrue("[" + message + "] should match " + Arrays.toString(dataPaths2),
                Arrays.stream(dataPaths2).anyMatch(p -> message.contains(p.toString())));
        }
    }

    public void testFailsIfFreshestStateIsInStaleTerm() throws IOException {
        final Path[] dataPaths1 = createDataPaths();
        final Path[] dataPaths2 = createDataPaths();
        final Path[] combinedPaths = Stream.concat(Arrays.stream(dataPaths1), Arrays.stream(dataPaths2)).toArray(Path[]::new);

        final long staleCurrentTerm = randomLongBetween(1L, Long.MAX_VALUE - 1);
        final long freshCurrentTerm = randomLongBetween(staleCurrentTerm + 1, Long.MAX_VALUE);

        final long freshTerm = randomLongBetween(1L, Long.MAX_VALUE);
        final long staleTerm = randomBoolean() ? freshTerm : randomLongBetween(1L, freshTerm);
        final long freshVersion = randomLongBetween(2L, Long.MAX_VALUE);
        final long staleVersion = staleTerm == freshTerm ? randomLongBetween(1L, freshVersion - 1) : randomLongBetween(1L, Long.MAX_VALUE);

        try (NodeEnvironment nodeEnvironment = newNodeEnvironment(combinedPaths)) {
            try (Writer writer = newPersistedClusterStateService(nodeEnvironment).createWriter()) {
                final ClusterState clusterState = loadPersistedClusterState(newPersistedClusterStateService(nodeEnvironment));
                assertFalse(clusterState.metaData().clusterUUIDCommitted());
                writeState(writer, staleCurrentTerm, ClusterState.builder(clusterState)
                    .metaData(MetaData.builder(clusterState.metaData()).version(1)
                        .coordinationMetaData(CoordinationMetaData.builder(clusterState.coordinationMetaData()).term(staleTerm).build()))
                    .version(staleVersion)
                    .build(),
                    clusterState);
            }
        }

        try (NodeEnvironment nodeEnvironment = newNodeEnvironment(dataPaths1)) {
            try (Writer writer = newPersistedClusterStateService(nodeEnvironment).createWriter()) {
                final ClusterState clusterState = loadPersistedClusterState(newPersistedClusterStateService(nodeEnvironment));
                writeState(writer, freshCurrentTerm, clusterState, clusterState);
            }
        }

        try (NodeEnvironment nodeEnvironment = newNodeEnvironment(dataPaths2)) {
            try (Writer writer = newPersistedClusterStateService(nodeEnvironment).createWriter()) {
                final PersistedClusterStateService.OnDiskState onDiskState = newPersistedClusterStateService(nodeEnvironment)
                    .loadBestOnDiskState();
                final ClusterState clusterState = clusterStateFromMetadata(onDiskState.lastAcceptedVersion, onDiskState.metaData);
                writeState(writer, onDiskState.currentTerm, ClusterState.builder(clusterState)
                    .metaData(MetaData.builder(clusterState.metaData()).version(2)
                        .coordinationMetaData(CoordinationMetaData.builder(clusterState.coordinationMetaData()).term(freshTerm).build()))
                    .version(freshVersion)
                    .build(), clusterState);
            }
        }

        try (NodeEnvironment nodeEnvironment = newNodeEnvironment(combinedPaths)) {
            final String message = expectThrows(IllegalStateException.class,
                () -> newPersistedClusterStateService(nodeEnvironment).loadBestOnDiskState()).getMessage();
            assertThat(message, allOf(
                    containsString("inconsistent terms found"),
                    containsString(Long.toString(staleCurrentTerm)),
                    containsString(Long.toString(freshCurrentTerm))));
            assertTrue("[" + message + "] should match " + Arrays.toString(dataPaths1),
                Arrays.stream(dataPaths1).anyMatch(p -> message.contains(p.toString())));
            assertTrue("[" + message + "] should match " + Arrays.toString(dataPaths2),
                Arrays.stream(dataPaths2).anyMatch(p -> message.contains(p.toString())));
        }
    }

    public void testFailsGracefullyOnExceptionDuringFlush() throws IOException {
        final AtomicBoolean throwException = new AtomicBoolean();

        try (NodeEnvironment nodeEnvironment = newNodeEnvironment(createDataPaths())) {
            final PersistedClusterStateService persistedClusterStateService
                = new PersistedClusterStateService(nodeEnvironment, xContentRegistry(), BigArrays.NON_RECYCLING_INSTANCE,
                new ClusterSettings(Settings.EMPTY, ClusterSettings.BUILT_IN_CLUSTER_SETTINGS), () -> 0L) {
                @Override
                Directory createDirectory(Path path) throws IOException {
                    return new FilterDirectory(super.createDirectory(path)) {
                        @Override
                        public IndexOutput createOutput(String name, IOContext context) throws IOException {
                            if (throwException.get()) {
                                throw new IOException("simulated");
                            }
                            return super.createOutput(name, context);
                        }
                    };
                }
            };

            try (Writer writer = persistedClusterStateService.createWriter()) {
                final ClusterState clusterState = loadPersistedClusterState(persistedClusterStateService);
                final long newTerm = randomNonNegativeLong();
                final ClusterState newState = ClusterState.builder(clusterState)
                    .metaData(MetaData.builder(clusterState.metaData())
                        .clusterUUID(UUIDs.randomBase64UUID(random()))
                        .clusterUUIDCommitted(true)
                        .version(randomLongBetween(1L, Long.MAX_VALUE)))
                    .incrementVersion().build();
                throwException.set(true);
                assertThat(expectThrows(IOException.class, () ->
                        writeState(writer, newTerm, newState, clusterState)).getMessage(),
                    containsString("simulated"));
            }
        }
    }

    public void testClosesWriterOnFatalError() throws IOException {
        final AtomicBoolean throwException = new AtomicBoolean();

        try (NodeEnvironment nodeEnvironment = newNodeEnvironment(createDataPaths())) {
            final PersistedClusterStateService persistedClusterStateService
                = new PersistedClusterStateService(nodeEnvironment, xContentRegistry(), BigArrays.NON_RECYCLING_INSTANCE,
                new ClusterSettings(Settings.EMPTY, ClusterSettings.BUILT_IN_CLUSTER_SETTINGS), () -> 0L) {
                @Override
                Directory createDirectory(Path path) throws IOException {
                    return new FilterDirectory(super.createDirectory(path)) {
                        @Override
                        public void sync(Collection<String> names) {
                            throw new OutOfMemoryError("simulated");
                        }
                    };
                }
            };

            try (Writer writer = persistedClusterStateService.createWriter()) {
                final ClusterState clusterState = loadPersistedClusterState(persistedClusterStateService);
                final long newTerm = randomNonNegativeLong();
                final ClusterState newState = ClusterState.builder(clusterState)
                    .metaData(MetaData.builder(clusterState.metaData())
                        .clusterUUID(UUIDs.randomBase64UUID(random()))
                        .clusterUUIDCommitted(true)
                        .version(randomLongBetween(1L, Long.MAX_VALUE)))
                    .incrementVersion().build();
                throwException.set(true);
                assertThat(expectThrows(OutOfMemoryError.class, () -> {
                        if (randomBoolean()) {
                            writeState(writer, newTerm, newState, clusterState);
                        } else {
                            writer.commit(newTerm, newState.version());
                        }
                    }).getMessage(),
                    containsString("simulated"));
                assertFalse(writer.isOpen());
            }

            // check if we can open writer again
            try (Writer ignored = persistedClusterStateService.createWriter()) {

            }
        }
    }

    public void testCrashesWithIOErrorOnCommitFailure() throws IOException {
        final AtomicBoolean throwException = new AtomicBoolean();

        try (NodeEnvironment nodeEnvironment = newNodeEnvironment(createDataPaths())) {
            final PersistedClusterStateService persistedClusterStateService
                = new PersistedClusterStateService(nodeEnvironment, xContentRegistry(), BigArrays.NON_RECYCLING_INSTANCE,
                new ClusterSettings(Settings.EMPTY, ClusterSettings.BUILT_IN_CLUSTER_SETTINGS), () -> 0L) {
                @Override
                Directory createDirectory(Path path) throws IOException {
                    return new FilterDirectory(super.createDirectory(path)) {
                        @Override
                        public void rename(String source, String dest) throws IOException {
                            if (throwException.get() && dest.startsWith("segments")) {
                                throw new IOException("simulated");
                            }
                        }
                    };
                }
            };

            try (Writer writer = persistedClusterStateService.createWriter()) {
                final ClusterState clusterState = loadPersistedClusterState(persistedClusterStateService);
                final long newTerm = randomNonNegativeLong();
                final ClusterState newState = ClusterState.builder(clusterState)
                    .metaData(MetaData.builder(clusterState.metaData())
                        .clusterUUID(UUIDs.randomBase64UUID(random()))
                        .clusterUUIDCommitted(true)
                        .version(randomLongBetween(1L, Long.MAX_VALUE)))
                    .incrementVersion().build();
                throwException.set(true);
                assertThat(expectThrows(IOError.class, () -> {
                        if (randomBoolean()) {
                            writeState(writer, newTerm, newState, clusterState);
                        } else {
                            writer.commit(newTerm, newState.version());
                        }
                    }).getMessage(),
                    containsString("simulated"));
                assertFalse(writer.isOpen());
            }

            // check if we can open writer again
            try (Writer ignored = persistedClusterStateService.createWriter()) {

            }
        }
    }

    public void testFailsIfGlobalMetadataIsMissing() throws IOException {
        // if someone attempted surgery on the metadata index by hand, e.g. deleting broken segments, then maybe the global metadata
        // isn't there any more

        try (NodeEnvironment nodeEnvironment = newNodeEnvironment(createDataPaths())) {
            try (Writer writer = newPersistedClusterStateService(nodeEnvironment).createWriter()) {
                final ClusterState clusterState = loadPersistedClusterState(newPersistedClusterStateService(nodeEnvironment));
                writeState(writer, 0L, ClusterState.builder(clusterState).version(randomLongBetween(1L, Long.MAX_VALUE)).build(),
                    clusterState);
            }

            final Path brokenPath = randomFrom(nodeEnvironment.nodeDataPaths());
            try (Directory directory = new SimpleFSDirectory(brokenPath.resolve(PersistedClusterStateService.METADATA_DIRECTORY_NAME))) {
                final IndexWriterConfig indexWriterConfig = new IndexWriterConfig();
                indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
                try (IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig)) {
                    indexWriter.commit();
                }
            }

            final String message = expectThrows(IllegalStateException.class,
                () -> newPersistedClusterStateService(nodeEnvironment).loadBestOnDiskState()).getMessage();
            assertThat(message, allOf(containsString("no global metadata found"), containsString(brokenPath.toString())));
        }
    }

    public void testFailsIfGlobalMetadataIsDuplicated() throws IOException {
        // if someone attempted surgery on the metadata index by hand, e.g. deleting broken segments, then maybe the global metadata
        // is duplicated

        final Path[] dataPaths1 = createDataPaths();
        final Path[] dataPaths2 = createDataPaths();
        final Path[] combinedPaths = Stream.concat(Arrays.stream(dataPaths1), Arrays.stream(dataPaths2)).toArray(Path[]::new);

        try (NodeEnvironment nodeEnvironment = newNodeEnvironment(combinedPaths)) {
            try (Writer writer = newPersistedClusterStateService(nodeEnvironment).createWriter()) {
                final ClusterState clusterState = loadPersistedClusterState(newPersistedClusterStateService(nodeEnvironment));
                writeState(writer, 0L, ClusterState.builder(clusterState).version(randomLongBetween(1L, Long.MAX_VALUE)).build(),
                    clusterState);
            }

            final Path brokenPath = randomFrom(nodeEnvironment.nodeDataPaths());
            final Path dupPath = randomValueOtherThan(brokenPath, () -> randomFrom(nodeEnvironment.nodeDataPaths()));
            try (Directory directory = new SimpleFSDirectory(brokenPath.resolve(PersistedClusterStateService.METADATA_DIRECTORY_NAME));
                 Directory dupDirectory = new SimpleFSDirectory(dupPath.resolve(PersistedClusterStateService.METADATA_DIRECTORY_NAME))) {
                try (IndexWriter indexWriter = new IndexWriter(directory, new IndexWriterConfig())) {
                    indexWriter.addIndexes(dupDirectory);
                    indexWriter.commit();
                }
            }

            final String message = expectThrows(IllegalStateException.class,
                () -> newPersistedClusterStateService(nodeEnvironment).loadBestOnDiskState()).getMessage();
            assertThat(message, allOf(containsString("duplicate global metadata found"), containsString(brokenPath.toString())));
        }
    }

    public void testFailsIfIndexMetadataIsDuplicated() throws IOException {
        // if someone attempted surgery on the metadata index by hand, e.g. deleting broken segments, then maybe some index metadata
        // is duplicated

        final Path[] dataPaths1 = createDataPaths();
        final Path[] dataPaths2 = createDataPaths();
        final Path[] combinedPaths = Stream.concat(Arrays.stream(dataPaths1), Arrays.stream(dataPaths2)).toArray(Path[]::new);

        try (NodeEnvironment nodeEnvironment = newNodeEnvironment(combinedPaths)) {
            final String indexUUID = UUIDs.randomBase64UUID(random());
            final String indexName = randomAlphaOfLength(10);

            try (Writer writer = newPersistedClusterStateService(nodeEnvironment).createWriter()) {
                final ClusterState clusterState = loadPersistedClusterState(newPersistedClusterStateService(nodeEnvironment));
                writeState(writer, 0L, ClusterState.builder(clusterState)
                        .metaData(MetaData.builder(clusterState.metaData())
                            .version(1L)
                            .coordinationMetaData(CoordinationMetaData.builder(clusterState.coordinationMetaData()).term(1L).build())
                            .put(IndexMetaData.builder(indexName)
                                .version(1L)
                                .settings(Settings.builder()
                                    .put(IndexMetaData.INDEX_NUMBER_OF_SHARDS_SETTING.getKey(), 1)
                                    .put(IndexMetaData.INDEX_NUMBER_OF_REPLICAS_SETTING.getKey(), 0)
                                    .put(IndexMetaData.SETTING_INDEX_VERSION_CREATED.getKey(), Version.CURRENT)
                                    .put(IndexMetaData.SETTING_INDEX_UUID, indexUUID))))
                        .incrementVersion().build(),
                    clusterState);
            }

            final Path brokenPath = randomFrom(nodeEnvironment.nodeDataPaths());
            final Path dupPath = randomValueOtherThan(brokenPath, () -> randomFrom(nodeEnvironment.nodeDataPaths()));
            try (Directory directory = new SimpleFSDirectory(brokenPath.resolve(PersistedClusterStateService.METADATA_DIRECTORY_NAME));
                 Directory dupDirectory = new SimpleFSDirectory(dupPath.resolve(PersistedClusterStateService.METADATA_DIRECTORY_NAME))) {
                try (IndexWriter indexWriter = new IndexWriter(directory, new IndexWriterConfig())) {
                    indexWriter.deleteDocuments(new Term("type", "global")); // do not duplicate global metadata
                    indexWriter.addIndexes(dupDirectory);
                    indexWriter.commit();
                }
            }

            final String message = expectThrows(IllegalStateException.class,
                () -> newPersistedClusterStateService(nodeEnvironment).loadBestOnDiskState()).getMessage();
            assertThat(message, allOf(
                containsString("duplicate metadata found"),
                containsString(brokenPath.toString()),
                containsString(indexName),
                containsString(indexUUID)));
        }
    }

    public void testPersistsAndReloadsIndexMetadataIffVersionOrTermChanges() throws IOException {
        try (NodeEnvironment nodeEnvironment = newNodeEnvironment(createDataPaths())) {
            final PersistedClusterStateService persistedClusterStateService = newPersistedClusterStateService(nodeEnvironment);
            final long globalVersion = randomLongBetween(1L, Long.MAX_VALUE);
            final String indexUUID = UUIDs.randomBase64UUID(random());
            final long indexMetaDataVersion = randomLongBetween(1L, Long.MAX_VALUE);

            final long oldTerm = randomLongBetween(1L, Long.MAX_VALUE - 1);
            final long newTerm = randomLongBetween(oldTerm + 1, Long.MAX_VALUE);

            try (Writer writer = persistedClusterStateService.createWriter()) {
                ClusterState clusterState = loadPersistedClusterState(persistedClusterStateService);
                writeState(writer, 0L, ClusterState.builder(clusterState)
                        .metaData(MetaData.builder(clusterState.metaData())
                            .version(globalVersion)
                            .coordinationMetaData(CoordinationMetaData.builder(clusterState.coordinationMetaData()).term(oldTerm).build())
                            .put(IndexMetaData.builder("test")
                                .version(indexMetaDataVersion - 1) // -1 because it's incremented in .put()
                                .settings(Settings.builder()
                                    .put(IndexMetaData.INDEX_NUMBER_OF_SHARDS_SETTING.getKey(), 1)
                                    .put(IndexMetaData.INDEX_NUMBER_OF_REPLICAS_SETTING.getKey(), 0)
                                    .put(IndexMetaData.SETTING_INDEX_VERSION_CREATED.getKey(), Version.CURRENT)
                                    .put(IndexMetaData.SETTING_INDEX_UUID, indexUUID))))
                        .incrementVersion().build(),
                    clusterState);


                clusterState = loadPersistedClusterState(persistedClusterStateService);
                IndexMetaData indexMetaData = clusterState.metaData().index("test");
                assertThat(indexMetaData.getIndexUUID(), equalTo(indexUUID));
                assertThat(indexMetaData.getVersion(), equalTo(indexMetaDataVersion));
                assertThat(IndexMetaData.INDEX_NUMBER_OF_REPLICAS_SETTING.get(indexMetaData.getSettings()), equalTo(0));
                // ensure we do not wastefully persist the same index metadata version by making a bad update with the same version
                writer.writeIncrementalStateAndCommit(0L, clusterState, ClusterState.builder(clusterState)
                        .metaData(MetaData.builder(clusterState.metaData())
                            .put(IndexMetaData.builder(indexMetaData).settings(Settings.builder()
                                .put(indexMetaData.getSettings())
                                .put(IndexMetaData.INDEX_NUMBER_OF_REPLICAS_SETTING.getKey(), 1)).build(), false))
                        .incrementVersion().build());

                clusterState = loadPersistedClusterState(persistedClusterStateService);
                indexMetaData = clusterState.metaData().index("test");
                assertThat(indexMetaData.getIndexUUID(), equalTo(indexUUID));
                assertThat(indexMetaData.getVersion(), equalTo(indexMetaDataVersion));
                assertThat(IndexMetaData.INDEX_NUMBER_OF_REPLICAS_SETTING.get(indexMetaData.getSettings()), equalTo(0));
                // ensure that we do persist the same index metadata version by making an update with a higher version
                writeState(writer, 0L, ClusterState.builder(clusterState)
                        .metaData(MetaData.builder(clusterState.metaData())
                            .put(IndexMetaData.builder(indexMetaData).settings(Settings.builder()
                                .put(indexMetaData.getSettings())
                                .put(IndexMetaData.INDEX_NUMBER_OF_REPLICAS_SETTING.getKey(), 2)).build(), true))
                        .incrementVersion().build(),
                    clusterState);

                clusterState = loadPersistedClusterState(persistedClusterStateService);
                indexMetaData = clusterState.metaData().index("test");
                assertThat(indexMetaData.getVersion(), equalTo(indexMetaDataVersion + 1));
                assertThat(IndexMetaData.INDEX_NUMBER_OF_REPLICAS_SETTING.get(indexMetaData.getSettings()), equalTo(2));
                // ensure that we also persist the index metadata when the term changes
                writeState(writer, 0L, ClusterState.builder(clusterState)
                        .metaData(MetaData.builder(clusterState.metaData())
                            .coordinationMetaData(CoordinationMetaData.builder(clusterState.coordinationMetaData()).term(newTerm).build())
                            .put(IndexMetaData.builder(indexMetaData).settings(Settings.builder()
                                .put(indexMetaData.getSettings())
                                .put(IndexMetaData.INDEX_NUMBER_OF_REPLICAS_SETTING.getKey(), 3)).build(), false))
                        .incrementVersion().build(),
                    clusterState);
            }

            final ClusterState clusterState = loadPersistedClusterState(persistedClusterStateService);
            final IndexMetaData indexMetaData = clusterState.metaData().index("test");
            assertThat(indexMetaData.getIndexUUID(), equalTo(indexUUID));
            assertThat(indexMetaData.getVersion(), equalTo(indexMetaDataVersion + 1));
            assertThat(IndexMetaData.INDEX_NUMBER_OF_REPLICAS_SETTING.get(indexMetaData.getSettings()), equalTo(3));
        }
    }

    public void testPersistsAndReloadsIndexMetadataForMultipleIndices() throws IOException {
        try (NodeEnvironment nodeEnvironment = newNodeEnvironment(createDataPaths())) {
            final PersistedClusterStateService persistedClusterStateService = newPersistedClusterStateService(nodeEnvironment);

            final long term = randomLongBetween(1L, Long.MAX_VALUE);
            final String addedIndexUuid = UUIDs.randomBase64UUID(random());
            final String updatedIndexUuid = UUIDs.randomBase64UUID(random());
            final String deletedIndexUuid = UUIDs.randomBase64UUID(random());

            try (Writer writer = persistedClusterStateService.createWriter()) {
                final ClusterState clusterState = loadPersistedClusterState(persistedClusterStateService);
                writeState(writer, 0L, ClusterState.builder(clusterState)
                    .metaData(MetaData.builder(clusterState.metaData())
                        .version(clusterState.metaData().version() + 1)
                        .coordinationMetaData(CoordinationMetaData.builder(clusterState.coordinationMetaData()).term(term).build())
                        .put(IndexMetaData.builder("updated")
                            .version(randomLongBetween(0L, Long.MAX_VALUE - 1) - 1) // -1 because it's incremented in .put()
                            .settings(Settings.builder()
                                .put(IndexMetaData.INDEX_NUMBER_OF_REPLICAS_SETTING.getKey(), 1)
                                .put(IndexMetaData.INDEX_NUMBER_OF_SHARDS_SETTING.getKey(), 1)
                                .put(IndexMetaData.SETTING_INDEX_VERSION_CREATED.getKey(), Version.CURRENT)
                                .put(IndexMetaData.SETTING_INDEX_UUID, updatedIndexUuid)))
                    .put(IndexMetaData.builder("deleted")
                        .version(randomLongBetween(0L, Long.MAX_VALUE - 1) - 1) // -1 because it's incremented in .put()
                        .settings(Settings.builder()
                            .put(IndexMetaData.INDEX_NUMBER_OF_REPLICAS_SETTING.getKey(), 1)
                            .put(IndexMetaData.INDEX_NUMBER_OF_SHARDS_SETTING.getKey(), 1)
                            .put(IndexMetaData.SETTING_INDEX_VERSION_CREATED.getKey(), Version.CURRENT)
                            .put(IndexMetaData.SETTING_INDEX_UUID, deletedIndexUuid))))
                    .incrementVersion().build(),
                    clusterState);
            }

            try (Writer writer = persistedClusterStateService.createWriter()) {
                final ClusterState clusterState = loadPersistedClusterState(persistedClusterStateService);

                assertThat(clusterState.metaData().indices().size(), equalTo(2));
                assertThat(clusterState.metaData().index("updated").getIndexUUID(), equalTo(updatedIndexUuid));
                assertThat(IndexMetaData.INDEX_NUMBER_OF_REPLICAS_SETTING.get(clusterState.metaData().index("updated").getSettings()),
                    equalTo(1));
                assertThat(clusterState.metaData().index("deleted").getIndexUUID(), equalTo(deletedIndexUuid));

                writeState(writer, 0L, ClusterState.builder(clusterState)
                    .metaData(MetaData.builder(clusterState.metaData())
                        .version(clusterState.metaData().version() + 1)
                        .remove("deleted")
                        .put(IndexMetaData.builder("updated")
                            .settings(Settings.builder()
                                .put(clusterState.metaData().index("updated").getSettings())
                                .put(IndexMetaData.INDEX_NUMBER_OF_REPLICAS_SETTING.getKey(), 2)))
                        .put(IndexMetaData.builder("added")
                            .version(randomLongBetween(0L, Long.MAX_VALUE - 1) - 1) // -1 because it's incremented in .put()
                            .settings(Settings.builder()
                                .put(IndexMetaData.INDEX_NUMBER_OF_REPLICAS_SETTING.getKey(), 1)
                                .put(IndexMetaData.INDEX_NUMBER_OF_SHARDS_SETTING.getKey(), 1)
                                .put(IndexMetaData.SETTING_INDEX_VERSION_CREATED.getKey(), Version.CURRENT)
                                .put(IndexMetaData.SETTING_INDEX_UUID, addedIndexUuid))))
                    .incrementVersion().build(),
                    clusterState);
            }

            final ClusterState clusterState = loadPersistedClusterState(persistedClusterStateService);

            assertThat(clusterState.metaData().indices().size(), equalTo(2));
            assertThat(clusterState.metaData().index("updated").getIndexUUID(), equalTo(updatedIndexUuid));
            assertThat(IndexMetaData.INDEX_NUMBER_OF_REPLICAS_SETTING.get(clusterState.metaData().index("updated").getSettings()),
                equalTo(2));
            assertThat(clusterState.metaData().index("added").getIndexUUID(), equalTo(addedIndexUuid));
            assertThat(clusterState.metaData().index("deleted"), nullValue());
        }
    }

    public void testReloadsMetadataAcrossMultipleSegments() throws IOException {
        try (NodeEnvironment nodeEnvironment = newNodeEnvironment(createDataPaths())) {
            final PersistedClusterStateService persistedClusterStateService = newPersistedClusterStateService(nodeEnvironment);

            final int writes = between(5, 20);
            final List<Index> indices = new ArrayList<>(writes);

            try (Writer writer = persistedClusterStateService.createWriter()) {
                for (int i = 0; i < writes; i++) {
                    final Index index = new Index("test-" + i, UUIDs.randomBase64UUID(random()));
                    indices.add(index);
                    final ClusterState clusterState = loadPersistedClusterState(persistedClusterStateService);
                    writeState(writer, 0L, ClusterState.builder(clusterState)
                        .metaData(MetaData.builder(clusterState.metaData())
                            .version(i + 2)
                            .put(IndexMetaData.builder(index.getName())
                                .settings(Settings.builder()
                                    .put(IndexMetaData.INDEX_NUMBER_OF_SHARDS_SETTING.getKey(), 1)
                                    .put(IndexMetaData.INDEX_NUMBER_OF_REPLICAS_SETTING.getKey(), 0)
                                    .put(IndexMetaData.SETTING_INDEX_VERSION_CREATED.getKey(), Version.CURRENT)
                                    .put(IndexMetaData.SETTING_INDEX_UUID, index.getUUID()))))
                        .incrementVersion().build(),
                        clusterState);
                }
            }

            final ClusterState clusterState = loadPersistedClusterState(persistedClusterStateService);
            for (Index index : indices) {
                final IndexMetaData indexMetaData = clusterState.metaData().index(index.getName());
                assertThat(indexMetaData.getIndexUUID(), equalTo(index.getUUID()));
            }
        }
    }

    @TestLogging(value = "org.elasticsearch.gateway:WARN", reason = "to ensure that we log gateway events on WARN level")
    public void testSlowLogging() throws IOException, IllegalAccessException {
        final long slowWriteLoggingThresholdMillis;
        final Settings settings;
        if (randomBoolean()) {
            slowWriteLoggingThresholdMillis = PersistedClusterStateService.SLOW_WRITE_LOGGING_THRESHOLD.get(Settings.EMPTY).millis();
            settings = Settings.EMPTY;
        } else {
            slowWriteLoggingThresholdMillis = randomLongBetween(2, 100000);
            settings = Settings.builder()
                .put(PersistedClusterStateService.SLOW_WRITE_LOGGING_THRESHOLD.getKey(), slowWriteLoggingThresholdMillis + "ms")
                .build();
        }

        final DiscoveryNode localNode = new DiscoveryNode("node", buildNewFakeTransportAddress(), Version.CURRENT);
        final ClusterState clusterState = ClusterState.builder(ClusterName.DEFAULT)
            .nodes(DiscoveryNodes.builder().add(localNode).localNodeId(localNode.getId())).build();

        final long startTimeMillis = randomLongBetween(0L, Long.MAX_VALUE - slowWriteLoggingThresholdMillis * 10);
        final AtomicLong currentTime = new AtomicLong(startTimeMillis);
        final AtomicLong writeDurationMillis = new AtomicLong(slowWriteLoggingThresholdMillis);

        final ClusterSettings clusterSettings = new ClusterSettings(settings, ClusterSettings.BUILT_IN_CLUSTER_SETTINGS);
        try (NodeEnvironment nodeEnvironment = newNodeEnvironment(createDataPaths())) {
            PersistedClusterStateService persistedClusterStateService = new PersistedClusterStateService(nodeEnvironment,
                xContentRegistry(),
                usually()
                    ? BigArrays.NON_RECYCLING_INSTANCE
                    : new MockBigArrays(new MockPageCacheRecycler(Settings.EMPTY), new NoneCircuitBreakerService()),
                clusterSettings,
                () -> currentTime.getAndAdd(writeDurationMillis.get()));

            try (Writer writer = persistedClusterStateService.createWriter()) {
                assertExpectedLogs(1L, null, clusterState, writer, new MockLogAppender.SeenEventExpectation(
                    "should see warning at threshold",
                    PersistedClusterStateService.class.getCanonicalName(),
                    Level.WARN,
                    "writing cluster state took [*] which is above the warn threshold of [*]; " +
                        "wrote full state with [0] indices"));

                writeDurationMillis.set(randomLongBetween(slowWriteLoggingThresholdMillis, slowWriteLoggingThresholdMillis * 2));
                assertExpectedLogs(1L, null, clusterState, writer, new MockLogAppender.SeenEventExpectation(
                    "should see warning above threshold",
                    PersistedClusterStateService.class.getCanonicalName(),
                    Level.WARN,
                    "writing cluster state took [*] which is above the warn threshold of [*]; " +
                        "wrote full state with [0] indices"));

                writeDurationMillis.set(randomLongBetween(1, slowWriteLoggingThresholdMillis - 1));
                assertExpectedLogs(1L, null, clusterState, writer, new MockLogAppender.UnseenEventExpectation(
                    "should not see warning below threshold",
                    PersistedClusterStateService.class.getCanonicalName(),
                    Level.WARN,
                    "*"));

                clusterSettings.applySettings(Settings.builder()
                    .put(PersistedClusterStateService.SLOW_WRITE_LOGGING_THRESHOLD.getKey(), writeDurationMillis.get() + "ms")
                    .build());
                assertExpectedLogs(1L, null, clusterState, writer, new MockLogAppender.SeenEventExpectation(
                    "should see warning at reduced threshold",
                    PersistedClusterStateService.class.getCanonicalName(),
                    Level.WARN,
                    "writing cluster state took [*] which is above the warn threshold of [*]; " +
                        "wrote full state with [0] indices"));

                final ClusterState newClusterState = ClusterState.builder(clusterState)
                    .metaData(MetaData.builder(clusterState.metaData())
                        .version(clusterState.version())
                        .put(IndexMetaData.builder("test")
                            .settings(Settings.builder()
                                .put(IndexMetaData.INDEX_NUMBER_OF_SHARDS_SETTING.getKey(), 1)
                                .put(IndexMetaData.INDEX_NUMBER_OF_REPLICAS_SETTING.getKey(), 0)
                                .put(IndexMetaData.SETTING_INDEX_VERSION_CREATED.getKey(), Version.CURRENT)
                                .put(IndexMetaData.SETTING_INDEX_UUID, "test-uuid"))))
                    .incrementVersion().build();

                assertExpectedLogs(1L, clusterState, newClusterState, writer, new MockLogAppender.SeenEventExpectation(
                    "should see warning at threshold",
                    PersistedClusterStateService.class.getCanonicalName(),
                    Level.WARN,
                    "writing cluster state took [*] which is above the warn threshold of [*]; " +
                        "wrote global metadata [false] and metadata for [1] indices and skipped [0] unchanged indices"));

                writeDurationMillis.set(randomLongBetween(1, writeDurationMillis.get() - 1));
                assertExpectedLogs(1L, clusterState, newClusterState, writer, new MockLogAppender.UnseenEventExpectation(
                    "should not see warning below threshold",
                    PersistedClusterStateService.class.getCanonicalName(),
                    Level.WARN,
                    "*"));

                assertThat(currentTime.get(), lessThan(startTimeMillis + 14 * slowWriteLoggingThresholdMillis)); // ensure no overflow
            }
        }
    }

    private void assertExpectedLogs(long currentTerm, ClusterState previousState, ClusterState clusterState,
                                    PersistedClusterStateService.Writer writer, MockLogAppender.LoggingExpectation expectation)
        throws IllegalAccessException, IOException {
        MockLogAppender mockAppender = new MockLogAppender();
        mockAppender.start();
        mockAppender.addExpectation(expectation);
        Logger classLogger = LogManager.getLogger(PersistedClusterStateService.class);
        Loggers.addAppender(classLogger, mockAppender);

        try {
            if (previousState == null) {
                writer.writeFullStateAndCommit(currentTerm, clusterState);
            } else {
                writer.writeIncrementalStateAndCommit(currentTerm, previousState, clusterState);
            }
        } finally {
            Loggers.removeAppender(classLogger, mockAppender);
            mockAppender.stop();
        }
        mockAppender.assertAllExpectationsMatched();
    }

    @Override
    public Settings buildEnvSettings(Settings settings) {
        assertTrue(settings.hasValue(Environment.PATH_DATA_SETTING.getKey()));
        return Settings.builder()
            .put(settings)
            .put(Environment.PATH_HOME_SETTING.getKey(), createTempDir().toAbsolutePath()).build();
    }

    public static Path[] createDataPaths() {
        final Path[] dataPaths = new Path[randomIntBetween(1, 4)];
        for (int i = 0; i < dataPaths.length; i++) {
            dataPaths[i] = createTempDir();
        }
        return dataPaths;
    }

    private NodeEnvironment newNodeEnvironment(Path[] dataPaths) throws IOException {
        return newNodeEnvironment(Settings.builder()
            .putList(Environment.PATH_DATA_SETTING.getKey(), Arrays.stream(dataPaths).map(Path::toString).collect(Collectors.toList()))
            .build());
    }

    private static ClusterState loadPersistedClusterState(PersistedClusterStateService persistedClusterStateService) throws IOException {
        final PersistedClusterStateService.OnDiskState onDiskState = persistedClusterStateService.loadBestOnDiskState();
        return clusterStateFromMetadata(onDiskState.lastAcceptedVersion, onDiskState.metaData);
    }

    private static ClusterState clusterStateFromMetadata(long version, MetaData metaData) {
        return ClusterState.builder(ClusterName.DEFAULT).version(version).metaData(metaData).build();
    }


}
