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

import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.geometry.utils.Geohash;
import org.elasticsearch.search.aggregations.InternalAggregations;
import org.elasticsearch.search.aggregations.pipeline.PipelineAggregator;

import java.util.List;
import java.util.Map;

public class GeoHashGridTests extends GeoGridTestCase<InternalGeoHashGridBucket, InternalGeoHashGrid> {

    @Override
    protected InternalGeoHashGrid createInternalGeoGrid(String name, int size, List<InternalGeoGridBucket> buckets,
                                                        List<PipelineAggregator> pipelineAggregators, Map<String, Object> metaData) {
        return new InternalGeoHashGrid(name, size, buckets, pipelineAggregators, metaData);
    }

    @Override
    protected Writeable.Reader<InternalGeoHashGrid> instanceReader() {
        return InternalGeoHashGrid::new;
    }

    @Override
    protected InternalGeoHashGridBucket createInternalGeoGridBucket(Long key, long docCount, InternalAggregations aggregations) {
        return new InternalGeoHashGridBucket(key, docCount, aggregations);
    }

    @Override
    protected long longEncode(double lng, double lat, int precision) {
        return Geohash.longEncode(lng, lat, precision);
    }

    @Override
    protected int randomPrecision() {
        return randomIntBetween(4, 12);
    }
}
