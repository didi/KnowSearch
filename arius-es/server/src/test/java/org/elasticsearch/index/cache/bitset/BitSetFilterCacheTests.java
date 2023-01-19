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

package org.elasticsearch.index.cache.bitset;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.LogByteSizeMergePolicy;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.join.BitSetProducer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Accountable;
import org.apache.lucene.util.BitSet;
import org.elasticsearch.common.lucene.index.ElasticsearchDirectoryReader;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.core.internal.io.IOUtils;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.test.IndexSettingsModule;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.Matchers.equalTo;

public class BitSetFilterCacheTests extends ESTestCase {

    private static final IndexSettings INDEX_SETTINGS = IndexSettingsModule.newIndexSettings("test", Settings.EMPTY);

    private static int matchCount(BitSetProducer producer, IndexReader reader) throws IOException {
        int count = 0;
        for (LeafReaderContext ctx : reader.leaves()) {
            final BitSet bitSet = producer.getBitSet(ctx);
            if (bitSet != null) {
                count += bitSet.cardinality();
            }
        }
        return count;
    }

    public void testInvalidateEntries() throws Exception {
        IndexWriter writer = new IndexWriter(
                new RAMDirectory(),
                new IndexWriterConfig(new StandardAnalyzer()).setMergePolicy(new LogByteSizeMergePolicy())
        );
        Document document = new Document();
        document.add(new StringField("field", "value", Field.Store.NO));
        writer.addDocument(document);
        writer.commit();

        document = new Document();
        document.add(new StringField("field", "value", Field.Store.NO));
        writer.addDocument(document);
        writer.commit();

        document = new Document();
        document.add(new StringField("field", "value", Field.Store.NO));
        writer.addDocument(document);
        writer.commit();

        DirectoryReader reader = DirectoryReader.open(writer);
        reader = ElasticsearchDirectoryReader.wrap(reader, new ShardId("test", "_na_", 0));

        BitsetFilterCache cache = new BitsetFilterCache(INDEX_SETTINGS, new BitsetFilterCache.Listener() {
            @Override
            public void onCache(ShardId shardId, Accountable accountable) {

            }

            @Override
            public void onRemoval(ShardId shardId, Accountable accountable) {

            }
        });
        BitSetProducer filter = cache.getBitSetProducer(new TermQuery(new Term("field", "value")));
        assertThat(matchCount(filter, reader), equalTo(3));

        // now cached
        assertThat(matchCount(filter, reader), equalTo(3));
        // There are 3 segments
        assertThat(cache.getLoadedFilters().weight(), equalTo(3L));

        writer.forceMerge(1);
        reader.close();
        reader = DirectoryReader.open(writer);
        reader = ElasticsearchDirectoryReader.wrap(reader, new ShardId("test", "_na_", 0));

        assertThat(matchCount(filter, reader), equalTo(3));

        // now cached
        assertThat(matchCount(filter, reader), equalTo(3));
        // Only one segment now, so the size must be 1
        assertThat(cache.getLoadedFilters().weight(), equalTo(1L));

        reader.close();
        writer.close();
        // There is no reference from readers and writer to any segment in the test index, so the size in the fbs cache must be 0
        assertThat(cache.getLoadedFilters().weight(), equalTo(0L));
    }

    public void testListener() throws IOException {
        IndexWriter writer = new IndexWriter(
                new RAMDirectory(),
                new IndexWriterConfig(new StandardAnalyzer()).setMergePolicy(new LogByteSizeMergePolicy())
        );
        Document document = new Document();
        document.add(new StringField("field", "value", Field.Store.NO));
        writer.addDocument(document);
        writer.commit();
        final DirectoryReader writerReader = DirectoryReader.open(writer);
        final IndexReader reader = ElasticsearchDirectoryReader.wrap(writerReader, new ShardId("test", "_na_", 0));

        final AtomicLong stats = new AtomicLong();
        final AtomicInteger onCacheCalls = new AtomicInteger();
        final AtomicInteger onRemoveCalls = new AtomicInteger();

        final BitsetFilterCache cache = new BitsetFilterCache(INDEX_SETTINGS, new BitsetFilterCache.Listener() {
            @Override
            public void onCache(ShardId shardId, Accountable accountable) {
                onCacheCalls.incrementAndGet();
                stats.addAndGet(accountable.ramBytesUsed());
                if (writerReader != reader) {
                    assertNotNull(shardId);
                    assertEquals("test", shardId.getIndexName());
                    assertEquals(0, shardId.id());
                } else {
                    assertNull(shardId);
                }
            }

            @Override
            public void onRemoval(ShardId shardId, Accountable accountable) {
                onRemoveCalls.incrementAndGet();
                stats.addAndGet(-accountable.ramBytesUsed());
                if (writerReader != reader) {
                    assertNotNull(shardId);
                    assertEquals("test", shardId.getIndexName());
                    assertEquals(0, shardId.id());
                } else {
                    assertNull(shardId);
                }
            }
        });
        BitSetProducer filter = cache.getBitSetProducer(new TermQuery(new Term("field", "value")));
        assertThat(matchCount(filter, reader), equalTo(1));
        assertTrue(stats.get() > 0);
        assertEquals(1, onCacheCalls.get());
        assertEquals(0, onRemoveCalls.get());
        IOUtils.close(reader, writer);
        assertEquals(1, onRemoveCalls.get());
        assertEquals(0, stats.get());
    }

    public void testSetNullListener() {
        try {
            new BitsetFilterCache(INDEX_SETTINGS, null);
            fail("listener can't be null");
        } catch (IllegalArgumentException ex) {
            assertEquals("listener must not be null", ex.getMessage());
            // all is well
        }
    }

    public void testRejectOtherIndex() throws IOException {
        BitsetFilterCache cache = new BitsetFilterCache(INDEX_SETTINGS, new BitsetFilterCache.Listener() {
            @Override
            public void onCache(ShardId shardId, Accountable accountable) {

            }

            @Override
            public void onRemoval(ShardId shardId, Accountable accountable) {

            }
        });

        Directory dir = newDirectory();
        IndexWriter writer = new IndexWriter(
                dir,
                newIndexWriterConfig()
        );
        writer.addDocument(new Document());
        DirectoryReader reader = DirectoryReader.open(writer);
        writer.close();
        reader = ElasticsearchDirectoryReader.wrap(reader, new ShardId("test2", "_na_", 0));

        BitSetProducer producer = cache.getBitSetProducer(new MatchAllDocsQuery());

        try {
            producer.getBitSet(reader.leaves().get(0));
            fail();
        } catch (IllegalStateException expected) {
            assertEquals("Trying to load bit set for index [test2] with cache of index [test]", expected.getMessage());
        } finally {
            IOUtils.close(reader, dir);
        }
    }

}
