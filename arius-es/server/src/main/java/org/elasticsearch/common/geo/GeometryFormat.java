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

package org.elasticsearch.common.geo;

import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.geometry.Geometry;

import java.io.IOException;
import java.text.ParseException;

/**
 * Geometry serializer/deserializer
 */
public interface GeometryFormat {

    /**
     * Parser JSON representation of a geometry
     */
    Geometry fromXContent(XContentParser parser) throws IOException, ParseException;

    /**
     * Serializes the geometry into its JSON representation
     */
    XContentBuilder toXContent(Geometry geometry, XContentBuilder builder, ToXContent.Params params) throws IOException;

}
