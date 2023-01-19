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

package org.elasticsearch.search.sort;

import org.elasticsearch.common.io.stream.BytesStreamOutput;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.test.ESTestCase;

import static org.hamcrest.Matchers.equalTo;

public class SortOrderTests extends ESTestCase {

    /** Check that ordinals remain stable as we rely on them for serialisation. */
    public void testDistanceUnitNames() {
        assertEquals(0, SortOrder.ASC.ordinal());
        assertEquals(1, SortOrder.DESC.ordinal());
    }

    public void testReadWrite() throws Exception {
        for (SortOrder unit : SortOrder.values()) {
          try (BytesStreamOutput out = new BytesStreamOutput()) {
              unit.writeTo(out);
              try (StreamInput in = out.bytes().streamInput()) {
                  assertThat("Roundtrip serialisation failed.", SortOrder.readFromStream(in), equalTo(unit));
              }
          }
        }
    }

    public void testFromString() {
        for (SortOrder unit : SortOrder.values()) {
            assertThat("Roundtrip string parsing failed.", SortOrder.fromString(unit.toString()), equalTo(unit));
        }
    }
}
