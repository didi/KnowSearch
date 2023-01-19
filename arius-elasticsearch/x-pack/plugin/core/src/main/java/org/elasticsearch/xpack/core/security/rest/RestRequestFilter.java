/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.core.security.rest;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.common.xcontent.support.XContentMapValues;
import org.elasticsearch.rest.RestRequest;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Identifies an object that supplies a filter for the content of a {@link RestRequest}. This interface should be implemented by a
 * {@link org.elasticsearch.rest.RestHandler} that expects there will be sensitive content in the body of the request such as a password
 */
public interface RestRequestFilter {

    /**
     * Wraps the RestRequest and returns a version that provides the filtered content
     */
    default RestRequest getFilteredRequest(RestRequest restRequest) throws IOException {
        Set<String> fields = getFilteredFields();
        if (restRequest.hasContent() && fields.isEmpty() == false) {
            return new RestRequest(restRequest) {

                private BytesReference filteredBytes = null;

                @Override
                public boolean hasContent() {
                    return true;
                }

                @Override
                public BytesReference content() {
                    if (filteredBytes == null) {
                        BytesReference content = restRequest.content();
                        Tuple<XContentType, Map<String, Object>> result = XContentHelper.convertToMap(content, true);
                        Map<String, Object> transformedSource = XContentMapValues.filter(result.v2(), null,
                                fields.toArray(Strings.EMPTY_ARRAY));
                        try {
                            XContentBuilder xContentBuilder = XContentBuilder.builder(result.v1().xContent()).map(transformedSource);
                            filteredBytes = BytesReference.bytes(xContentBuilder);
                        } catch (IOException e) {
                            throw new ElasticsearchException("failed to parse request", e);
                        }
                    }
                    return filteredBytes;
                }
            };
        } else {
            return restRequest;
        }
    }

    /**
     * The list of fields that should be filtered. This can be a dot separated pattern to match sub objects and also supports wildcards
     */
    Set<String> getFilteredFields();
}
