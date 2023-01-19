/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.core.ml.utils;

import org.elasticsearch.test.ESTestCase;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

public class MapHelperTests extends ESTestCase {

    public void testAbsolutePathStringAsKey() {
        String path = "a.b.c.d";
        Map<String, Object> map = Collections.singletonMap(path, 2);
        assertThat(MapHelper.dig(path, map), equalTo(2));
        assertThat(MapHelper.dig(path, Collections.emptyMap()), is(nullValue()));
    }

    public void testSimplePath() {
        String path = "a.b.c.d";
        Map<String, Object> map = Collections.singletonMap("a",
            Collections.singletonMap("b",
                Collections.singletonMap("c",
                    Collections.singletonMap("d", 2))));
        assertThat(MapHelper.dig(path, map), equalTo(2));

        map = Collections.singletonMap("a",
            Collections.singletonMap("b",
                Collections.singletonMap("e", // Not part of path
                    Collections.singletonMap("d", 2))));
        assertThat(MapHelper.dig(path, map), is(nullValue()));
    }

    public void testSimplePathReturningMap() {
        String path = "a.b.c";
        Map<String, Object> map = Collections.singletonMap("a",
            Collections.singletonMap("b",
                Collections.singletonMap("c",
                    Collections.singletonMap("d", 2))));
        assertThat(MapHelper.dig(path, map), equalTo(Collections.singletonMap("d", 2)));
    }

    public void testSimpleMixedPath() {
        String path = "a.b.c.d";
        Map<String, Object> map = Collections.singletonMap("a",
            Collections.singletonMap("b.c",
                    Collections.singletonMap("d", 2)));
        assertThat(MapHelper.dig(path, map), equalTo(2));

        map = Collections.singletonMap("a.b",
            Collections.singletonMap("c",
                Collections.singletonMap("d", 2)));
        assertThat(MapHelper.dig(path, map), equalTo(2));

        map = Collections.singletonMap("a.b.c",
                Collections.singletonMap("d", 2));
        assertThat(MapHelper.dig(path, map), equalTo(2));

        map = Collections.singletonMap("a",
            Collections.singletonMap("b",
                Collections.singletonMap("c.d", 2)));
        assertThat(MapHelper.dig(path, map), equalTo(2));

        map = Collections.singletonMap("a",
            Collections.singletonMap("b.c.d", 2));
        assertThat(MapHelper.dig(path, map), equalTo(2));

        map = Collections.singletonMap("a.b",
            Collections.singletonMap("c.d", 2));
        assertThat(MapHelper.dig(path, map), equalTo(2));

        map = Collections.singletonMap("a",
            Collections.singletonMap("b.foo",
                Collections.singletonMap("d", 2)));
        assertThat(MapHelper.dig(path, map), is(nullValue()));

        map = Collections.singletonMap("a",
            Collections.singletonMap("b.c",
                Collections.singletonMap("foo", 2)));
        assertThat(MapHelper.dig(path, map), is(nullValue()));

        map = Collections.singletonMap("x",
            Collections.singletonMap("b.c",
                Collections.singletonMap("d", 2)));
        assertThat(MapHelper.dig(path, map), is(nullValue()));
    }

    public void testSimpleMixedPathReturningMap() {
        String path = "a.b.c";
        Map<String, Object> map = Collections.singletonMap("a",
            Collections.singletonMap("b.c",
                Collections.singletonMap("d", 2)));
        assertThat(MapHelper.dig(path, map), equalTo(Collections.singletonMap("d", 2)));

        map = Collections.singletonMap("a",
            Collections.singletonMap("b.foo",
                Collections.singletonMap("d", 2)));
        assertThat(MapHelper.dig(path, map), is(nullValue()));

        map = Collections.singletonMap("a",
            Collections.singletonMap("b.not_c",
                Collections.singletonMap("foo", 2)));
        assertThat(MapHelper.dig(path, map), is(nullValue()));

        map = Collections.singletonMap("x",
            Collections.singletonMap("b.c",
                Collections.singletonMap("d", 2)));
        assertThat(MapHelper.dig(path, map), is(nullValue()));
    }

    public void testMultiplePotentialPaths() {
        String path = "a.b.c.d";
        Map<String, Object> map = new LinkedHashMap<String, Object>() {{
            put("a", Collections.singletonMap("b",
                Collections.singletonMap("c",
                    Collections.singletonMap("not_d", 5))));
            put("a.b", Collections.singletonMap("c", Collections.singletonMap("d", 2)));
        }};
        assertThat(MapHelper.dig(path, map), equalTo(2));

        map = new LinkedHashMap<String, Object>() {{
            put("a", Collections.singletonMap("b",
                Collections.singletonMap("c",
                    Collections.singletonMap("d", 2))));
            put("a.b", Collections.singletonMap("c", Collections.singletonMap("not_d", 5)));
        }};
        assertThat(MapHelper.dig(path, map), equalTo(2));

        map = new LinkedHashMap<String, Object>() {{
            put("a", Collections.singletonMap("b",
                new HashMap<String, Object>() {{
                    put("c", Collections.singletonMap("not_d", 5));
                    put("c.d", 2);
                }}));
        }};
        assertThat(MapHelper.dig(path, map), equalTo(2));

        map = new LinkedHashMap<String, Object>() {{
            put("a", Collections.singletonMap("b",
                new HashMap<String, Object>() {{
                    put("c", Collections.singletonMap("d", 2));
                    put("c.not_d", 5);
                }}));
        }};
        assertThat(MapHelper.dig(path, map), equalTo(2));

        map = new LinkedHashMap<String, Object>() {{
            put("a", Collections.singletonMap("b",
                Collections.singletonMap("c",
                    Collections.singletonMap("not_d", 5))));
            put("a.b", Collections.singletonMap("c", Collections.singletonMap("not_d", 2)));
        }};

        assertThat(MapHelper.dig(path, map), is(nullValue()));
    }

    public void testMultiplePotentialPathsReturningMap() {
        String path = "a.b.c";
        Map<String, Object> map = new LinkedHashMap<String, Object>() {{
            put("a", Collections.singletonMap("b",
                Collections.singletonMap("c",
                    Collections.singletonMap("d", 2))));
            put("a.b", Collections.singletonMap("not_c", Collections.singletonMap("d", 2)));
        }};
        assertThat(MapHelper.dig(path, map), equalTo(Collections.singletonMap("d", 2)));

        map = new LinkedHashMap<String, Object>() {{
            put("a", Collections.singletonMap("b",
                Collections.singletonMap("not_c",
                    Collections.singletonMap("d", 2))));
            put("a.b", Collections.singletonMap("c", Collections.singletonMap("d", 2)));
        }};
        assertThat(MapHelper.dig(path, map), equalTo(Collections.singletonMap("d", 2)));

        map = new LinkedHashMap<String, Object>() {{
            put("a", Collections.singletonMap("b",
                Collections.singletonMap("not_c",
                    Collections.singletonMap("d", 2))));
            put("a.b", Collections.singletonMap("not_c", Collections.singletonMap("d", 2)));
        }};
        assertThat(MapHelper.dig(path, map), is(nullValue()));
    }

}
