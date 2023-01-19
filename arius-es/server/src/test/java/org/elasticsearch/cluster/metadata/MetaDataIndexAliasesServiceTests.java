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

package org.elasticsearch.cluster.metadata;

import org.elasticsearch.Version;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.util.set.Sets;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.test.VersionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MetaDataIndexAliasesServiceTests extends ESTestCase {
    private final AliasValidator aliasValidator = new AliasValidator();
    private final MetaDataDeleteIndexService deleteIndexService = mock(MetaDataDeleteIndexService.class);
    private final MetaDataIndexAliasesService service = new MetaDataIndexAliasesService(null, null, aliasValidator,
            deleteIndexService, xContentRegistry());

    public MetaDataIndexAliasesServiceTests() {
        // Mock any deletes so we don't need to worry about how MetaDataDeleteIndexService does its job
        when(deleteIndexService.deleteIndices(any(ClusterState.class), anySetOf(Index.class))).then(i -> {
            ClusterState state = (ClusterState) i.getArguments()[0];
            @SuppressWarnings("unchecked")
            Collection<Index> indices = (Collection<Index>) i.getArguments()[1];
            MetaData.Builder meta = MetaData.builder(state.metaData());
            for (Index index : indices) {
                assertTrue("index now found", state.metaData().hasConcreteIndex(index.getName()));
                meta.remove(index.getName()); // We only think about metadata for this test. Not routing or any other fun stuff.
            }
            return ClusterState.builder(state).metaData(meta).build();
        });
    }

    public void testAddAndRemove() {
        // Create a state with a single index
        String index = randomAlphaOfLength(5);
        ClusterState before = createIndex(ClusterState.builder(ClusterName.DEFAULT).build(), index);

        // Add an alias to it
        ClusterState after = service.applyAliasActions(before, singletonList(new AliasAction.Add(index, "test", null, null, null, null)));
        AliasOrIndex alias = after.metaData().getAliasAndIndexLookup().get("test");
        assertNotNull(alias);
        assertTrue(alias.isAlias());
        assertThat(alias.getIndices(), contains(after.metaData().index(index)));
        assertAliasesVersionIncreased(index, before, after);

        // Remove the alias from it while adding another one
        before = after;
        after = service.applyAliasActions(before, Arrays.asList(
                new AliasAction.Remove(index, "test"),
                new AliasAction.Add(index, "test_2", null, null, null, null)));
        assertNull(after.metaData().getAliasAndIndexLookup().get("test"));
        alias = after.metaData().getAliasAndIndexLookup().get("test_2");
        assertNotNull(alias);
        assertTrue(alias.isAlias());
        assertThat(alias.getIndices(), contains(after.metaData().index(index)));
        assertAliasesVersionIncreased(index, before, after);

        // Now just remove on its own
        before = after;
        after = service.applyAliasActions(before, singletonList(new AliasAction.Remove(index, "test_2")));
        assertNull(after.metaData().getAliasAndIndexLookup().get("test"));
        assertNull(after.metaData().getAliasAndIndexLookup().get("test_2"));
        assertAliasesVersionIncreased(index, before, after);
    }

    public void testMultipleIndices() {
        final int length = randomIntBetween(2, 8);
        final Set<String> indices = new HashSet<>(length);
        ClusterState before = ClusterState.builder(ClusterName.DEFAULT).build();
        final List<AliasAction> addActions = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            final String index = randomValueOtherThanMany(v -> indices.add(v) == false, () -> randomAlphaOfLength(8));
            before = createIndex(before, index);
            addActions.add(new AliasAction.Add(index, "alias-" + index, null, null, null, null));
        }
        final ClusterState afterAddingAliasesToAll = service.applyAliasActions(before, addActions);
        assertAliasesVersionIncreased(indices.toArray(new String[0]), before, afterAddingAliasesToAll);

        // now add some aliases randomly
        final Set<String> randomIndices = new HashSet<>(length);
        final List<AliasAction> randomAddActions = new ArrayList<>(length);
        for (String index : indices) {
            if (randomBoolean()) {
                randomAddActions.add(new AliasAction.Add(index, "random-alias-" + index, null, null, null, null));
                randomIndices.add(index);
            }
        }
        final ClusterState afterAddingRandomAliases = service.applyAliasActions(afterAddingAliasesToAll, randomAddActions);
        assertAliasesVersionIncreased(randomIndices.toArray(new String[0]), afterAddingAliasesToAll, afterAddingRandomAliases);
        assertAliasesVersionUnchanged(
                Sets.difference(indices, randomIndices).toArray(new String[0]),
                afterAddingAliasesToAll,
                afterAddingRandomAliases);
    }

    public void testChangingWriteAliasStateIncreasesAliasesVersion() {
        final String index = randomAlphaOfLength(8);
        final ClusterState before = createIndex(ClusterState.builder(ClusterName.DEFAULT).build(), index);

        final ClusterState afterAddWriteAlias =
                service.applyAliasActions(before, singletonList(new AliasAction.Add(index, "test", null, null, null, true)));
        assertAliasesVersionIncreased(index, before, afterAddWriteAlias);

        final ClusterState afterChangeWriteAliasToNonWriteAlias =
                service.applyAliasActions(afterAddWriteAlias, singletonList(new AliasAction.Add(index, "test", null, null, null, false)));
        assertAliasesVersionIncreased(index, afterAddWriteAlias, afterChangeWriteAliasToNonWriteAlias);

        final ClusterState afterChangeNonWriteAliasToWriteAlias =
                service.applyAliasActions(
                        afterChangeWriteAliasToNonWriteAlias,
                        singletonList(new AliasAction.Add(index, "test", null, null, null, true)));
        assertAliasesVersionIncreased(index, afterChangeWriteAliasToNonWriteAlias, afterChangeNonWriteAliasToWriteAlias);
    }

    public void testAddingAliasMoreThanOnceShouldOnlyIncreaseAliasesVersionByOne() {
        final String index = randomAlphaOfLength(8);
        final ClusterState before = createIndex(ClusterState.builder(ClusterName.DEFAULT).build(), index);

        // add an alias to the index multiple times
        final int length = randomIntBetween(2, 8);
        final List<AliasAction> addActions = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            addActions.add(new AliasAction.Add(index, "test", null, null, null, null));
        }
        final ClusterState afterAddingAliases = service.applyAliasActions(before, addActions);

        assertAliasesVersionIncreased(index, before, afterAddingAliases);
    }

    public void testAliasesVersionUnchangedWhenActionsAreIdempotent() {
        final String index = randomAlphaOfLength(8);
        final ClusterState before = createIndex(ClusterState.builder(ClusterName.DEFAULT).build(), index);

        // add some aliases to the index
        final int length = randomIntBetween(1, 8);
        final Set<String> aliasNames = new HashSet<>();
        final List<AliasAction> addActions = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            final String aliasName = randomValueOtherThanMany(v -> aliasNames.add(v) == false, () -> randomAlphaOfLength(8));
            addActions.add(new AliasAction.Add(index, aliasName, null, null, null, null));
        }
        final ClusterState afterAddingAlias = service.applyAliasActions(before, addActions);

        // now perform a remove and add for each alias which is idempotent, the resulting aliases are unchanged
        final List<AliasAction> removeAndAddActions = new ArrayList<>(2 * length);
        for (final String aliasName : aliasNames) {
            removeAndAddActions.add(new AliasAction.Remove(index, aliasName));
            removeAndAddActions.add(new AliasAction.Add(index, aliasName, null, null, null, null));
        }
        final ClusterState afterRemoveAndAddAlias = service.applyAliasActions(afterAddingAlias, removeAndAddActions);
        assertAliasesVersionUnchanged(index, afterAddingAlias, afterRemoveAndAddAlias);
    }

    public void testSwapIndexWithAlias() {
        // Create "test" and "test_2"
        ClusterState before = createIndex(ClusterState.builder(ClusterName.DEFAULT).build(), "test");
        before = createIndex(before, "test_2");

        // Now remove "test" and add an alias to "test" to "test_2" in one go
        ClusterState after = service.applyAliasActions(before, Arrays.asList(
                new AliasAction.Add("test_2", "test", null, null, null, null),
                new AliasAction.RemoveIndex("test")));
        AliasOrIndex alias = after.metaData().getAliasAndIndexLookup().get("test");
        assertNotNull(alias);
        assertTrue(alias.isAlias());
        assertThat(alias.getIndices(), contains(after.metaData().index("test_2")));
        assertAliasesVersionIncreased("test_2", before, after);
    }

    public void testAddAliasToRemovedIndex() {
        // Create "test"
        ClusterState before = createIndex(ClusterState.builder(ClusterName.DEFAULT).build(), "test");

        // Attempt to add an alias to "test" at the same time as we remove it
        IndexNotFoundException e = expectThrows(IndexNotFoundException.class, () -> service.applyAliasActions(before, Arrays.asList(
                new AliasAction.Add("test", "alias", null, null, null, null),
                new AliasAction.RemoveIndex("test"))));
        assertEquals("test", e.getIndex().getName());
    }

    public void testRemoveIndexTwice() {
        // Create "test"
        ClusterState before = createIndex(ClusterState.builder(ClusterName.DEFAULT).build(), "test");

        // Try to remove an index twice. This should just remove the index once....
        ClusterState after = service.applyAliasActions(before, Arrays.asList(
                new AliasAction.RemoveIndex("test"),
                new AliasAction.RemoveIndex("test")));
        assertNull(after.metaData().getAliasAndIndexLookup().get("test"));
    }

    public void testAddWriteOnlyWithNoExistingAliases() {
        ClusterState before = createIndex(ClusterState.builder(ClusterName.DEFAULT).build(), "test");

        ClusterState after = service.applyAliasActions(before, Arrays.asList(
            new AliasAction.Add("test", "alias", null, null, null, false)));
        assertFalse(after.metaData().index("test").getAliases().get("alias").writeIndex());
        assertNull(((AliasOrIndex.Alias) after.metaData().getAliasAndIndexLookup().get("alias")).getWriteIndex());
        assertAliasesVersionIncreased("test", before, after);

        after = service.applyAliasActions(before, Arrays.asList(
            new AliasAction.Add("test", "alias", null, null, null, null)));
        assertNull(after.metaData().index("test").getAliases().get("alias").writeIndex());
        assertThat(((AliasOrIndex.Alias) after.metaData().getAliasAndIndexLookup().get("alias")).getWriteIndex(),
            equalTo(after.metaData().index("test")));
        assertAliasesVersionIncreased("test", before, after);

        after = service.applyAliasActions(before, Arrays.asList(
            new AliasAction.Add("test", "alias", null, null, null, true)));
        assertTrue(after.metaData().index("test").getAliases().get("alias").writeIndex());
        assertThat(((AliasOrIndex.Alias) after.metaData().getAliasAndIndexLookup().get("alias")).getWriteIndex(),
            equalTo(after.metaData().index("test")));
        assertAliasesVersionIncreased("test", before, after);
    }

    public void testAddWriteOnlyWithExistingWriteIndex() {
        IndexMetaData.Builder indexMetaData = IndexMetaData.builder("test")
            .settings(settings(Version.CURRENT)).numberOfShards(1).numberOfReplicas(1);
        IndexMetaData.Builder indexMetaData2 = IndexMetaData.builder("test2")
            .putAlias(AliasMetaData.builder("alias").writeIndex(true).build())
            .settings(settings(Version.CURRENT)).numberOfShards(1).numberOfReplicas(1);
        ClusterState before = ClusterState.builder(ClusterName.DEFAULT)
            .metaData(MetaData.builder().put(indexMetaData).put(indexMetaData2)).build();

        ClusterState after = service.applyAliasActions(before, Arrays.asList(
            new AliasAction.Add("test", "alias", null, null, null, null)));
        assertNull(after.metaData().index("test").getAliases().get("alias").writeIndex());
        assertThat(((AliasOrIndex.Alias) after.metaData().getAliasAndIndexLookup().get("alias")).getWriteIndex(),
            equalTo(after.metaData().index("test2")));
        assertAliasesVersionIncreased("test", before, after);
        assertAliasesVersionUnchanged("test2", before, after);

        Exception exception = expectThrows(IllegalStateException.class, () -> service.applyAliasActions(before, Arrays.asList(
            new AliasAction.Add("test", "alias", null, null, null, true))));
        assertThat(exception.getMessage(), startsWith("alias [alias] has more than one write index ["));
    }

    public void testSwapWriteOnlyIndex() {
        IndexMetaData.Builder indexMetaData = IndexMetaData.builder("test")
            .putAlias(AliasMetaData.builder("alias").writeIndex(true).build())
            .settings(settings(Version.CURRENT)).numberOfShards(1).numberOfReplicas(1);
        IndexMetaData.Builder indexMetaData2 = IndexMetaData.builder("test2")
            .settings(settings(Version.CURRENT)).numberOfShards(1).numberOfReplicas(1);
        ClusterState before = ClusterState.builder(ClusterName.DEFAULT)
            .metaData(MetaData.builder().put(indexMetaData).put(indexMetaData2)).build();

        Boolean unsetValue = randomBoolean() ? null : false;
        List<AliasAction> swapActions = Arrays.asList(
            new AliasAction.Add("test", "alias", null, null, null, unsetValue),
            new AliasAction.Add("test2", "alias", null, null, null, true)
        );
        Collections.shuffle(swapActions, random());
        ClusterState after = service.applyAliasActions(before, swapActions);
        assertThat(after.metaData().index("test").getAliases().get("alias").writeIndex(), equalTo(unsetValue));
        assertTrue(after.metaData().index("test2").getAliases().get("alias").writeIndex());
        assertThat(((AliasOrIndex.Alias) after.metaData().getAliasAndIndexLookup().get("alias")).getWriteIndex(),
            equalTo(after.metaData().index("test2")));
        assertAliasesVersionIncreased("test", before, after);
        assertAliasesVersionIncreased("test2", before, after);
    }

    public void testAddWriteOnlyWithExistingNonWriteIndices() {
        IndexMetaData.Builder indexMetaData = IndexMetaData.builder("test")
            .putAlias(AliasMetaData.builder("alias").writeIndex(randomBoolean() ? null : false).build())
            .settings(settings(Version.CURRENT)).numberOfShards(1).numberOfReplicas(1);
        IndexMetaData.Builder indexMetaData2 = IndexMetaData.builder("test2")
            .putAlias(AliasMetaData.builder("alias").writeIndex(randomBoolean() ? null : false).build())
            .settings(settings(Version.CURRENT)).numberOfShards(1).numberOfReplicas(1);
        IndexMetaData.Builder indexMetaData3 = IndexMetaData.builder("test3")
            .settings(settings(Version.CURRENT)).numberOfShards(1).numberOfReplicas(1);
        ClusterState before = ClusterState.builder(ClusterName.DEFAULT)
            .metaData(MetaData.builder().put(indexMetaData).put(indexMetaData2).put(indexMetaData3)).build();

        assertNull(((AliasOrIndex.Alias) before.metaData().getAliasAndIndexLookup().get("alias")).getWriteIndex());

        ClusterState after = service.applyAliasActions(before, Arrays.asList(
            new AliasAction.Add("test3", "alias", null, null, null, true)));
        assertTrue(after.metaData().index("test3").getAliases().get("alias").writeIndex());
        assertThat(((AliasOrIndex.Alias) after.metaData().getAliasAndIndexLookup().get("alias")).getWriteIndex(),
            equalTo(after.metaData().index("test3")));
        assertAliasesVersionUnchanged("test", before, after);
        assertAliasesVersionUnchanged("test2", before, after);
        assertAliasesVersionIncreased("test3", before, after);
    }

    public void testAddWriteOnlyWithIndexRemoved() {
        IndexMetaData.Builder indexMetaData = IndexMetaData.builder("test")
            .putAlias(AliasMetaData.builder("alias").build())
            .settings(settings(Version.CURRENT)).numberOfShards(1).numberOfReplicas(1);
        IndexMetaData.Builder indexMetaData2 = IndexMetaData.builder("test2")
            .putAlias(AliasMetaData.builder("alias").build())
            .settings(settings(Version.CURRENT)).numberOfShards(1).numberOfReplicas(1);
        ClusterState before = ClusterState.builder(ClusterName.DEFAULT)
            .metaData(MetaData.builder().put(indexMetaData).put(indexMetaData2)).build();

        assertNull(before.metaData().index("test").getAliases().get("alias").writeIndex());
        assertNull(before.metaData().index("test2").getAliases().get("alias").writeIndex());
        assertNull(((AliasOrIndex.Alias) before.metaData().getAliasAndIndexLookup().get("alias")).getWriteIndex());

        ClusterState after = service.applyAliasActions(before, Collections.singletonList(new AliasAction.RemoveIndex("test")));
        assertNull(after.metaData().index("test2").getAliases().get("alias").writeIndex());
        assertThat(((AliasOrIndex.Alias) after.metaData().getAliasAndIndexLookup().get("alias")).getWriteIndex(),
            equalTo(after.metaData().index("test2")));
        assertAliasesVersionUnchanged("test2", before, after);
    }

    public void testAddWriteOnlyValidatesAgainstMetaDataBuilder() {
        IndexMetaData.Builder indexMetaData = IndexMetaData.builder("test")
            .settings(settings(Version.CURRENT)).numberOfShards(1).numberOfReplicas(1);
        IndexMetaData.Builder indexMetaData2 = IndexMetaData.builder("test2")
            .settings(settings(Version.CURRENT)).numberOfShards(1).numberOfReplicas(1);
        ClusterState before = ClusterState.builder(ClusterName.DEFAULT)
            .metaData(MetaData.builder().put(indexMetaData).put(indexMetaData2)).build();

        Exception exception = expectThrows(IllegalStateException.class, () -> service.applyAliasActions(before, Arrays.asList(
            new AliasAction.Add("test", "alias", null, null, null, true),
            new AliasAction.Add("test2", "alias", null, null, null, true)
        )));
        assertThat(exception.getMessage(), startsWith("alias [alias] has more than one write index ["));
    }

    private ClusterState createIndex(ClusterState state, String index) {
        IndexMetaData indexMetaData = IndexMetaData.builder(index)
                .settings(Settings.builder().put("index.version.created", VersionUtils.randomVersion(random())))
                .numberOfShards(1)
                .numberOfReplicas(1)
                .build();
        return ClusterState.builder(state)
                .metaData(MetaData.builder(state.metaData()).put(indexMetaData, false))
                .build();
    }

    private void assertAliasesVersionUnchanged(final String index, final ClusterState before, final ClusterState after) {
        assertAliasesVersionUnchanged(new String[]{index}, before, after);
    }

    private void assertAliasesVersionUnchanged(final String[] indices, final ClusterState before, final ClusterState after) {
        for (final String index : indices) {
            final long expected = before.metaData().index(index).getAliasesVersion();
            final long actual = after.metaData().index(index).getAliasesVersion();
            assertThat("index metadata aliases version mismatch", actual, equalTo(expected));
        }
    }

    private void assertAliasesVersionIncreased(final String index, final ClusterState before, final ClusterState after) {
        assertAliasesVersionIncreased(new String[]{index}, before, after);
    }

    private void assertAliasesVersionIncreased(final String[] indices, final ClusterState before, final ClusterState after) {
        for (final String index : indices) {
            final long expected = 1 + before.metaData().index(index).getAliasesVersion();
            final long actual = after.metaData().index(index).getAliasesVersion();
            assertThat("index metadata aliases version mismatch", actual, equalTo(expected));
        }
    }

}
