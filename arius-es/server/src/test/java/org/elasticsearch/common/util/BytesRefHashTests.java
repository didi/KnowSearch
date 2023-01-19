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

package org.elasticsearch.common.util;

import com.carrotsearch.hppc.ObjectLongHashMap;
import com.carrotsearch.hppc.ObjectLongMap;
import com.carrotsearch.hppc.cursors.ObjectLongCursor;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefBuilder;
import org.apache.lucene.util.TestUtil;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.indices.breaker.NoneCircuitBreakerService;
import org.elasticsearch.test.ESSingleNodeTestCase;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class BytesRefHashTests extends ESSingleNodeTestCase {

    BytesRefHash hash;

    private BigArrays randombigArrays() {
        return new MockBigArrays(new MockPageCacheRecycler(Settings.EMPTY), new NoneCircuitBreakerService());
    }

    private void newHash() {
        if (hash != null) {
            hash.close();
        }
        // Test high load factors to make sure that collision resolution works fine
        final float maxLoadFactor = 0.6f + randomFloat() * 0.39f;
        hash = new BytesRefHash(randomIntBetween(0, 100), maxLoadFactor, randombigArrays());
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        newHash();
    }

    public void testDuell() {
        final int len = randomIntBetween(1, 100000);
        final BytesRef[] values = new BytesRef[len];
        for (int i = 0; i < values.length; ++i) {
            values[i] = new BytesRef(randomAlphaOfLength(5));
        }
        final ObjectLongMap<BytesRef> valueToId = new ObjectLongHashMap<>();
        final BytesRef[] idToValue = new BytesRef[values.length];
        final int iters = randomInt(1000000);
        for (int i = 0; i < iters; ++i) {
            final BytesRef value = randomFrom(values);
            if (valueToId.containsKey(value)) {
                assertEquals(- 1 - valueToId.get(value), hash.add(value, value.hashCode()));
            } else {
                assertEquals(valueToId.size(), hash.add(value, value.hashCode()));
                idToValue[valueToId.size()] = value;
                valueToId.put(value, valueToId.size());
            }
        }

        assertEquals(valueToId.size(), hash.size());
        for (Iterator<ObjectLongCursor<BytesRef>> iterator = valueToId.iterator(); iterator.hasNext(); ) {
            final ObjectLongCursor<BytesRef> next = iterator.next();
            assertEquals(next.value, hash.find(next.key, next.key.hashCode()));
        }

        for (long i = 0; i < hash.capacity(); ++i) {
            final long id = hash.id(i);
            BytesRef spare = new BytesRef();
            if (id >= 0) {
                hash.get(id, spare);
                assertEquals(idToValue[(int) id], spare);
            }
        }
        hash.close();
    }

    // START - tests borrowed from LUCENE

    /**
     * Test method for {@link org.apache.lucene.util.BytesRefHash#size()}.
     */
    public void testSize() {
        BytesRefBuilder ref = new BytesRefBuilder();
        int num = scaledRandomIntBetween(2, 20);
        for (int j = 0; j < num; j++) {
            final int mod = 1+randomInt(40);
            for (int i = 0; i < 797; i++) {
                String str;
                do {
                    str = TestUtil.randomRealisticUnicodeString(random(), 1000);
                } while (str.length() == 0);
                ref.copyChars(str);
                long count = hash.size();
                long key = hash.add(ref.get());
                if (key < 0)
                    assertEquals(hash.size(), count);
                else
                    assertEquals(hash.size(), count + 1);
                if(i % mod == 0) {
                    newHash();
                }
            }
        }
        hash.close();
    }

    /**
     * Test method for
     * {@link org.apache.lucene.util.BytesRefHash#get(int, BytesRef)}
     * .
     */
    public void testGet() {
        BytesRefBuilder ref = new BytesRefBuilder();
        BytesRef scratch = new BytesRef();
        int num = scaledRandomIntBetween(2, 20);
        for (int j = 0; j < num; j++) {
            Map<String, Long> strings = new HashMap<>();
            int uniqueCount = 0;
            for (int i = 0; i < 797; i++) {
                String str;
                do {
                    str = TestUtil.randomRealisticUnicodeString(random(), 1000);
                } while (str.length() == 0);
                ref.copyChars(str);
                long count = hash.size();
                long key = hash.add(ref.get());
                if (key >= 0) {
                    assertNull(strings.put(str, Long.valueOf(key)));
                    assertEquals(uniqueCount, key);
                    uniqueCount++;
                    assertEquals(hash.size(), count + 1);
                } else {
                    assertTrue((-key)-1 < count);
                    assertEquals(hash.size(), count);
                }
            }
            for (Entry<String, Long> entry : strings.entrySet()) {
                ref.copyChars(entry.getKey());
                assertEquals(ref.get(), hash.get(entry.getValue().longValue(), scratch));
            }
            newHash();
        }
        hash.close();
    }

    /**
     * Test method for
     * {@link org.apache.lucene.util.BytesRefHash#add(org.apache.lucene.util.BytesRef)}
     * .
     */
    public void testAdd() {
        BytesRefBuilder ref = new BytesRefBuilder();
        BytesRef scratch = new BytesRef();
        int num = scaledRandomIntBetween(2, 20);
        for (int j = 0; j < num; j++) {
            Set<String> strings = new HashSet<>();
            int uniqueCount = 0;
            for (int i = 0; i < 797; i++) {
                String str;
                do {
                    str = TestUtil.randomRealisticUnicodeString(random(), 1000);
                } while (str.length() == 0);
                ref.copyChars(str);
                long count = hash.size();
                long key = hash.add(ref.get());

                if (key >=0) {
                    assertTrue(strings.add(str));
                    assertEquals(uniqueCount, key);
                    assertEquals(hash.size(), count + 1);
                    uniqueCount++;
                } else {
                    assertFalse(strings.add(str));
                    assertTrue((-key)-1 < count);
                    assertEquals(str, hash.get((-key)-1, scratch).utf8ToString());
                    assertEquals(count, hash.size());
                }
            }

            assertAllIn(strings, hash);
            newHash();
        }
        hash.close();
    }

    public void testFind() throws Exception {
        BytesRefBuilder ref = new BytesRefBuilder();
        BytesRef scratch = new BytesRef();
        int num = scaledRandomIntBetween(2, 20);
        for (int j = 0; j < num; j++) {
            Set<String> strings = new HashSet<>();
            int uniqueCount = 0;
            for (int i = 0; i < 797; i++) {
                String str;
                do {
                    str = TestUtil.randomRealisticUnicodeString(random(), 1000);
                } while (str.length() == 0);
                ref.copyChars(str);
                long count = hash.size();
                long key = hash.find(ref.get()); //hash.add(ref);
                if (key >= 0) { // string found in hash
                    assertFalse(strings.add(str));
                    assertTrue(key < count);
                    assertEquals(str, hash.get(key, scratch).utf8ToString());
                    assertEquals(count, hash.size());
                } else {
                    key = hash.add(ref.get());
                    assertTrue(strings.add(str));
                    assertEquals(uniqueCount, key);
                    assertEquals(hash.size(), count + 1);
                    uniqueCount++;
                }
            }

            assertAllIn(strings, hash);
            newHash();
        }
        hash.close();
    }

    private void assertAllIn(Set<String> strings, BytesRefHash hash) {
        BytesRefBuilder ref = new BytesRefBuilder();
        BytesRef scratch = new BytesRef();
        long count = hash.size();
        for (String string : strings) {
            ref.copyChars(string);
            long key  =  hash.add(ref.get()); // add again to check duplicates
            assertEquals(string, hash.get((-key)-1, scratch).utf8ToString());
            assertEquals(count, hash.size());
            assertTrue("key: " + key + " count: " + count + " string: " + string,
                    key < count);
        }
    }

    // END - tests borrowed from LUCENE

}
