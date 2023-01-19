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

package org.elasticsearch.action.fieldcaps;

import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Writeable;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * Response for {@link FieldCapabilitiesIndexRequest} requests.
 */
public class FieldCapabilitiesIndexResponse extends ActionResponse implements Writeable {
    private String indexName;
    private Map<String, FieldCapabilities> responseMap;

    FieldCapabilitiesIndexResponse(String indexName, Map<String, FieldCapabilities> responseMap) {
        this.indexName = indexName;
        this.responseMap = responseMap;
    }

    FieldCapabilitiesIndexResponse(StreamInput in) throws IOException {
        super(in);
        this.indexName = in.readString();
        this.responseMap =
            in.readMap(StreamInput::readString, FieldCapabilities::new);
    }


    /**
     * Get the index name
     */
    public String getIndexName() {
        return indexName;
    }

    /**
     * Get the field capabilities map
     */
    public Map<String, FieldCapabilities> get() {
        return responseMap;
    }

    /**
     *
     * Get the field capabilities for the provided {@code field}
     */
    public FieldCapabilities getField(String field) {
        return responseMap.get(field);
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeString(indexName);
        out.writeMap(responseMap,
            StreamOutput::writeString, (valueOut, fc) -> fc.writeTo(valueOut));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldCapabilitiesIndexResponse that = (FieldCapabilitiesIndexResponse) o;
        return Objects.equals(indexName, that.indexName) &&
            Objects.equals(responseMap, that.responseMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(indexName, responseMap);
    }
}
