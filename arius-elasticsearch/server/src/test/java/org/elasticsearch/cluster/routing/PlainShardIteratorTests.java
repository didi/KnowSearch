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

package org.elasticsearch.cluster.routing;

import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.test.EqualsHashCodeTestUtils;
import org.hamcrest.Matchers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlainShardIteratorTests extends ESTestCase {

    public void testEqualsAndHashCode() {
        EqualsHashCodeTestUtils.checkEqualsAndHashCode(randomPlainShardIterator(),
            i -> new PlainShardIterator(i.shardId(), i.getShardRoutings()),
            i -> {
                ShardId shardId;
                switch(randomIntBetween(0, 2)) {
                    case 0:
                        shardId = new ShardId(i.shardId().getIndex(), i.shardId().getId() + randomIntBetween(1, 1000));
                        break;
                    case 1:
                        shardId = new ShardId(i.shardId().getIndexName(),
                            i.shardId().getIndex().getUUID() + randomAlphaOfLengthBetween(1, 3), i.shardId().getId());
                        break;
                    case 2:
                        shardId = new ShardId(i.shardId().getIndexName() + randomAlphaOfLengthBetween(1, 3),
                            i.shardId().getIndex().getUUID(), i.shardId().getId());
                        break;
                    default:
                        throw new UnsupportedOperationException();
                }
                return new PlainShardIterator(shardId, i.getShardRoutings());
            });
    }

    public void testCompareTo() {
        String[] indices = generateRandomStringArray(3, 10, false, false);
        Arrays.sort(indices);
        String[] uuids = generateRandomStringArray(3, 10, false, false);
        Arrays.sort(uuids);
        List<PlainShardIterator> shardIterators = new ArrayList<>();
        int numShards = randomIntBetween(1, 5);
        for (int i = 0; i < numShards; i++) {
            for (String index : indices) {
                for (String uuid : uuids) {
                    ShardId shardId = new ShardId(index, uuid, i);
                    shardIterators.add(new PlainShardIterator(shardId, GroupShardsIteratorTests.randomShardRoutings(shardId)));
                }
            }
        }
        for (int i = 0; i < shardIterators.size(); i++) {
            PlainShardIterator currentIterator = shardIterators.get(i);
            for (int j = i + 1; j < shardIterators.size(); j++) {
                PlainShardIterator greaterIterator = shardIterators.get(j);
                assertThat(currentIterator, Matchers.lessThan(greaterIterator));
                assertThat(greaterIterator, Matchers.greaterThan(currentIterator));
                assertNotEquals(currentIterator, greaterIterator);
            }
            for (int j = i - 1; j >= 0; j--) {
                PlainShardIterator smallerIterator = shardIterators.get(j);
                assertThat(smallerIterator, Matchers.lessThan(currentIterator));
                assertThat(currentIterator, Matchers.greaterThan(smallerIterator));
                assertNotEquals(currentIterator, smallerIterator);
            }
        }
    }

    public void testCompareToEqualItems() {
        PlainShardIterator shardIterator1 = randomPlainShardIterator();
        PlainShardIterator shardIterator2 = new PlainShardIterator(shardIterator1.shardId(), shardIterator1.getShardRoutings());
        assertEquals(shardIterator1, shardIterator2);
        assertEquals(0, shardIterator1.compareTo(shardIterator2));
        assertEquals(0, shardIterator2.compareTo(shardIterator1));
    }

    private static PlainShardIterator randomPlainShardIterator() {
        ShardId shardId = new ShardId(randomAlphaOfLengthBetween(5, 10), randomAlphaOfLength(10), randomIntBetween(1, Integer.MAX_VALUE));
        return new PlainShardIterator(shardId, GroupShardsIteratorTests.randomShardRoutings(shardId));
    }
}
