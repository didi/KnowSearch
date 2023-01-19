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
package org.elasticsearch.search.aggregations.metrics;

import org.apache.lucene.geo.GeoEncodingUtils;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.search.aggregations.ParsedAggregation;
import org.elasticsearch.search.aggregations.metrics.InternalGeoCentroid;
import org.elasticsearch.search.aggregations.metrics.ParsedGeoCentroid;
import org.elasticsearch.search.aggregations.pipeline.PipelineAggregator;
import org.elasticsearch.test.InternalAggregationTestCase;
import org.elasticsearch.test.geo.RandomGeoGenerator;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InternalGeoCentroidTests extends InternalAggregationTestCase<InternalGeoCentroid> {

    @Override
    protected InternalGeoCentroid createTestInstance(String name, List<PipelineAggregator> pipelineAggregators,
                                                     Map<String, Object> metaData) {
        GeoPoint centroid = RandomGeoGenerator.randomPoint(random());

        // Re-encode lat/longs to avoid rounding issue when testing InternalGeoCentroid#hashCode() and
        // InternalGeoCentroid#equals()
        int encodedLon = GeoEncodingUtils.encodeLongitude(centroid.lon());
        centroid.resetLon(GeoEncodingUtils.decodeLongitude(encodedLon));
        int encodedLat = GeoEncodingUtils.encodeLatitude(centroid.lat());
        centroid.resetLat(GeoEncodingUtils.decodeLatitude(encodedLat));
        long count = randomIntBetween(0, 1000);
        if (count == 0) {
            centroid = null;
        }
        return new InternalGeoCentroid(name, centroid, count, Collections.emptyList(), Collections.emptyMap());
    }

    @Override
    protected Writeable.Reader<InternalGeoCentroid> instanceReader() {
        return InternalGeoCentroid::new;
    }

    @Override
    protected void assertReduced(InternalGeoCentroid reduced, List<InternalGeoCentroid> inputs) {
        double lonSum = 0;
        double latSum = 0;
        int totalCount = 0;
        for (InternalGeoCentroid input : inputs) {
            if (input.count() > 0) {
                lonSum += (input.count() * input.centroid().getLon());
                latSum += (input.count() * input.centroid().getLat());
            }
            totalCount += input.count();
        }
        if (totalCount > 0) {
            assertEquals(latSum/totalCount, reduced.centroid().getLat(), 1E-5D);
            assertEquals(lonSum/totalCount, reduced.centroid().getLon(), 1E-5D);
        }
        assertEquals(totalCount, reduced.count());
    }

    @Override
    protected void assertFromXContent(InternalGeoCentroid aggregation, ParsedAggregation parsedAggregation) {
        assertTrue(parsedAggregation instanceof ParsedGeoCentroid);
        ParsedGeoCentroid parsed = (ParsedGeoCentroid) parsedAggregation;

        assertEquals(aggregation.centroid(), parsed.centroid());
        assertEquals(aggregation.count(), parsed.count());
    }

    @Override
    protected InternalGeoCentroid mutateInstance(InternalGeoCentroid instance) {
        String name = instance.getName();
        GeoPoint centroid = instance.centroid();
        long count = instance.count();
        List<PipelineAggregator> pipelineAggregators = instance.pipelineAggregators();
        Map<String, Object> metaData = instance.getMetaData();
        switch (between(0, 2)) {
        case 0:
            name += randomAlphaOfLength(5);
            break;
        case 1:
            count += between(1, 100);
            if (centroid == null) {
                // if the new count is > 0 then we need to make sure there is a
                // centroid or the constructor will throw an exception
                centroid = new GeoPoint(randomDoubleBetween(-90, 90, false), randomDoubleBetween(-180, 180, false));
            }
            break;
        case 2:
            if (centroid == null) {
                centroid = new GeoPoint(randomDoubleBetween(-90, 90, false), randomDoubleBetween(-180, 180, false));
                count = between(1, 100);
            } else {
                GeoPoint newCentroid = new GeoPoint(centroid);
                if (randomBoolean()) {
                    newCentroid.resetLat(centroid.getLat() / 2.0);
                } else {
                    newCentroid.resetLon(centroid.getLon() / 2.0);
                }
                centroid = newCentroid;
            }
            break;
        case 3:
            if (metaData == null) {
                metaData = new HashMap<>(1);
            } else {
                metaData = new HashMap<>(instance.getMetaData());
            }
            metaData.put(randomAlphaOfLength(15), randomInt());
            break;
        default:
            throw new AssertionError("Illegal randomisation branch");
        }
        return new InternalGeoCentroid(name, centroid, count, pipelineAggregators, metaData);
    }
}
