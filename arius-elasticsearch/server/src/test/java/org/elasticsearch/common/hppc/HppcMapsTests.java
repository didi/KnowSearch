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
package org.elasticsearch.common.hppc;

import com.carrotsearch.hppc.ObjectHashSet;
import org.elasticsearch.Assertions;
import org.elasticsearch.common.collect.HppcMaps;
import org.elasticsearch.test.ESTestCase;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;

public class HppcMapsTests extends ESTestCase {
    public void testIntersection() throws Exception {
        assumeTrue("assertions enabled", Assertions.ENABLED);
        ObjectHashSet<String> set1 = ObjectHashSet.from("1", "2", "3");
        ObjectHashSet<String> set2 = ObjectHashSet.from("1", "2", "3");
        List<String> values = toList(HppcMaps.intersection(set1, set2));
        assertThat(values.size(), equalTo(3));
        assertThat(values.contains("1"), equalTo(true));
        assertThat(values.contains("2"), equalTo(true));
        assertThat(values.contains("3"), equalTo(true));

        set1 = ObjectHashSet.from("1", "2", "3");
        set2 = ObjectHashSet.from("3", "4", "5");
        values = toList(HppcMaps.intersection(set1, set2));
        assertThat(values.size(), equalTo(1));
        assertThat(values.get(0), equalTo("3"));

        set1 = ObjectHashSet.from("1", "2", "3");
        set2 = ObjectHashSet.from("4", "5", "6");
        values = toList(HppcMaps.intersection(set1, set2));
        assertThat(values.size(), equalTo(0));

        set1 = ObjectHashSet.from();
        set2 = ObjectHashSet.from("3", "4", "5");
        values = toList(HppcMaps.intersection(set1, set2));
        assertThat(values.size(), equalTo(0));

        set1 = ObjectHashSet.from("1", "2", "3");
        set2 = ObjectHashSet.from();
        values = toList(HppcMaps.intersection(set1, set2));
        assertThat(values.size(), equalTo(0));

        set1 = ObjectHashSet.from();
        set2 = ObjectHashSet.from();
        values = toList(HppcMaps.intersection(set1, set2));
        assertThat(values.size(), equalTo(0));

        set1 = null;
        set2 = ObjectHashSet.from();
        try {
            toList(HppcMaps.intersection(set1, set2));
            fail();
        } catch (AssertionError e) {}

        set1 = ObjectHashSet.from();
        set2 = null;
        try {
            toList(HppcMaps.intersection(set1, set2));
            fail();
        } catch (AssertionError e) {}

        set1 = null;
        set2 = null;
        try {
            toList(HppcMaps.intersection(set1, set2));
            fail();
        } catch (AssertionError e) {}
    }

    private List<String> toList(Iterable<String> iterable) {
        List<String> list = new ArrayList<>();
        for (String s : iterable) {
            list.add(s);
        }
        return list;
    }


}
