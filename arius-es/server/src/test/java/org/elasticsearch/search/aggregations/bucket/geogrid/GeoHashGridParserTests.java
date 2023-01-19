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
package org.elasticsearch.search.aggregations.bucket.geogrid;

import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.common.xcontent.XContentParseException;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.geo.GeometryTestUtils;
import org.elasticsearch.geometry.Rectangle;
import org.elasticsearch.test.ESTestCase;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

public class GeoHashGridParserTests extends ESTestCase {
    public void testParseValidFromInts() throws Exception {
        int precision = randomIntBetween(1, 12);
        XContentParser stParser = createParser(JsonXContent.jsonXContent,
                "{\"field\":\"my_loc\", \"precision\":" + precision + ", \"size\": 500, \"shard_size\": 550}");
        XContentParser.Token token = stParser.nextToken();
        assertSame(XContentParser.Token.START_OBJECT, token);
        // can create a factory
        assertNotNull(GeoHashGridAggregationBuilder.parse("geohash_grid", stParser));
    }

    public void testParseValidFromStrings() throws Exception {
        int precision = randomIntBetween(1, 12);
        XContentParser stParser = createParser(JsonXContent.jsonXContent,
                "{\"field\":\"my_loc\", \"precision\":\"" + precision + "\", \"size\": \"500\", \"shard_size\": \"550\"}");
        XContentParser.Token token = stParser.nextToken();
        assertSame(XContentParser.Token.START_OBJECT, token);
        // can create a factory
        assertNotNull(GeoHashGridAggregationBuilder.parse("geohash_grid", stParser));
    }

    public void testParseDistanceUnitPrecision() throws Exception {
        double distance = randomDoubleBetween(10.0, 100.00, true);
        DistanceUnit unit = randomFrom(DistanceUnit.values());
        if (unit.equals(DistanceUnit.MILLIMETERS)) {
            distance = 5600 + randomDouble(); // 5.6cm is approx. smallest distance represented by precision 12
        }
        String distanceString = distance + unit.toString();
        XContentParser stParser = createParser(JsonXContent.jsonXContent,
                "{\"field\":\"my_loc\", \"precision\": \"" + distanceString + "\", \"size\": \"500\", \"shard_size\": \"550\"}");
        XContentParser.Token token = stParser.nextToken();
        assertSame(XContentParser.Token.START_OBJECT, token);
        // can create a factory
        GeoGridAggregationBuilder builder = GeoHashGridAggregationBuilder.parse("geohash_grid", stParser);
        assertNotNull(builder);
        assertThat(builder.precision(), greaterThanOrEqualTo(0));
        assertThat(builder.precision(), lessThanOrEqualTo(12));
    }

    public void testParseInvalidUnitPrecision() throws Exception {
        XContentParser stParser = createParser(JsonXContent.jsonXContent,
                "{\"field\":\"my_loc\", \"precision\": \"10kg\", \"size\": \"500\", \"shard_size\": \"550\"}");
        XContentParser.Token token = stParser.nextToken();
        assertSame(XContentParser.Token.START_OBJECT, token);
        XContentParseException ex = expectThrows(XContentParseException.class,
                () -> GeoHashGridAggregationBuilder.parse("geohash_grid", stParser));
        assertThat(ex.getMessage(), containsString("[geohash_grid] failed to parse field [precision]"));
        assertThat(ex.getCause(), instanceOf(NumberFormatException.class));
        assertEquals("For input string: \"10kg\"", ex.getCause().getMessage());
    }

    public void testParseDistanceUnitPrecisionTooSmall() throws Exception {
        XContentParser stParser = createParser(JsonXContent.jsonXContent,
                "{\"field\":\"my_loc\", \"precision\": \"1cm\", \"size\": \"500\", \"shard_size\": \"550\"}");
        XContentParser.Token token = stParser.nextToken();
        assertSame(XContentParser.Token.START_OBJECT, token);
        XContentParseException ex = expectThrows(XContentParseException.class,
                () -> GeoHashGridAggregationBuilder.parse("geohash_grid", stParser));
        assertThat(ex.getMessage(), containsString("[geohash_grid] failed to parse field [precision]"));
        assertThat(ex.getCause(), instanceOf(IllegalArgumentException.class));
        assertEquals("precision too high [1cm]", ex.getCause().getMessage());
    }

    public void testParseErrorOnBooleanPrecision() throws Exception {
        XContentParser stParser = createParser(JsonXContent.jsonXContent, "{\"field\":\"my_loc\", \"precision\":false}");
        XContentParser.Token token = stParser.nextToken();
        assertSame(XContentParser.Token.START_OBJECT, token);
        XContentParseException e = expectThrows(XContentParseException.class,
                () -> GeoHashGridAggregationBuilder.parse("geohash_grid", stParser));
        assertThat(e.getMessage(), containsString("[geohash_grid] precision doesn't support values of type: VALUE_BOOLEAN"));
    }

    public void testParseErrorOnPrecisionOutOfRange() throws Exception {
        XContentParser stParser = createParser(JsonXContent.jsonXContent, "{\"field\":\"my_loc\", \"precision\":\"13\"}");
        XContentParser.Token token = stParser.nextToken();
        assertSame(XContentParser.Token.START_OBJECT, token);
        try {
            GeoHashGridAggregationBuilder.parse("geohash_grid", stParser);
            fail();
        } catch (XContentParseException ex) {
            assertThat(ex.getCause(), instanceOf(IllegalArgumentException.class));
            assertEquals("Invalid geohash aggregation precision of 13. Must be between 1 and 12.", ex.getCause().getMessage());
        }
    }

    public void testParseValidBounds() throws Exception {
        Rectangle bbox = GeometryTestUtils.randomRectangle();
        XContentParser stParser = createParser(JsonXContent.jsonXContent,
            "{\"field\":\"my_loc\", \"precision\": 5, \"size\": 500, \"shard_size\": 550," + "\"bounds\": { "
                + "\"top\": " + bbox.getMaxY() + ","
                + "\"bottom\": " + bbox.getMinY() + ","
                + "\"left\": " + bbox.getMinX() + ","
                + "\"right\": " + bbox.getMaxX() + "}"
                + "}");
        XContentParser.Token token = stParser.nextToken();
        assertSame(XContentParser.Token.START_OBJECT, token);
        // can create a factory
        assertNotNull(GeoHashGridAggregationBuilder.parse("geohash_grid", stParser));
    }
}
