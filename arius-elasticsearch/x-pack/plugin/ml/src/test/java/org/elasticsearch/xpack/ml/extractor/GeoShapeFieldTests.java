/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.ml.extractor;

import org.elasticsearch.search.SearchHit;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.xpack.ml.test.SearchHitBuilder;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

public class GeoShapeFieldTests extends ESTestCase {

    public void testObjectFormat() {
        double lat = 38.897676;
        double lon = -77.03653;
        String[] expected = new String[] {lat + "," + lon};

        SearchHit hit = new SearchHitBuilder(42)
            .setSource("{\"geo\":{\"type\":\"point\", \"coordinates\": [" + lon + ", " + lat + "]}}")
            .build();

        ExtractedField geo = new GeoShapeField("geo");

        assertThat(geo.value(hit), equalTo(expected));
        assertThat(geo.getName(), equalTo("geo"));
        assertThat(geo.getSearchField(), equalTo("geo"));
        assertThat(geo.getTypes(), contains("geo_shape"));
        assertThat(geo.getMethod(), equalTo(ExtractedField.Method.SOURCE));
        assertThat(geo.supportsFromSource(), is(true));
        assertThat(geo.newFromSource(), sameInstance(geo));
        expectThrows(UnsupportedOperationException.class, () -> geo.getDocValueFormat());
        assertThat(geo.isMultiField(), is(false));
        expectThrows(UnsupportedOperationException.class, () -> geo.getParentField());
    }

    public void testWKTFormat() {
        double lat = 38.897676;
        double lon = -77.03653;
        String[] expected = new String[] {lat + "," + lon};

        SearchHit hit = new SearchHitBuilder(42).setSource("{\"geo\":\"POINT ("+ lon + " " + lat + ")\"}").build();

        ExtractedField geo = new GeoShapeField("geo");

        assertThat(geo.value(hit), equalTo(expected));
        assertThat(geo.getName(), equalTo("geo"));
        assertThat(geo.getSearchField(), equalTo("geo"));
        assertThat(geo.getTypes(), contains("geo_shape"));
        assertThat(geo.getMethod(), equalTo(ExtractedField.Method.SOURCE));
        assertThat(geo.supportsFromSource(), is(true));
        assertThat(geo.newFromSource(), sameInstance(geo));
        expectThrows(UnsupportedOperationException.class, () -> geo.getDocValueFormat());
        assertThat(geo.isMultiField(), is(false));
        expectThrows(UnsupportedOperationException.class, () -> geo.getParentField());
    }
}
