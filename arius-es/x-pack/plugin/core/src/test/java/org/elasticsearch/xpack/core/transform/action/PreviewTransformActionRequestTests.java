/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */

package org.elasticsearch.xpack.core.transform.action;

import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.common.xcontent.DeprecationHandler;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.xpack.core.transform.action.PreviewTransformAction.Request;
import org.elasticsearch.xpack.core.transform.transforms.TransformConfig;
import org.elasticsearch.xpack.core.transform.transforms.TransformConfigTests;
import org.elasticsearch.xpack.core.transform.transforms.DestConfig;
import org.elasticsearch.xpack.core.transform.transforms.pivot.PivotConfigTests;

import java.io.IOException;

import static org.elasticsearch.xpack.core.transform.transforms.SourceConfigTests.randomSourceConfig;

public class PreviewTransformActionRequestTests extends AbstractSerializingTransformTestCase<Request> {

    @Override
    protected Request doParseInstance(XContentParser parser) throws IOException {
        return Request.fromXContent(parser);
    }

    @Override
    protected Writeable.Reader<Request> instanceReader() {
        return Request::new;
    }

    @Override
    protected boolean supportsUnknownFields() {
        return false;
    }

    @Override
    protected Request createTestInstance() {
        TransformConfig config = new TransformConfig(
                "transform-preview",
                randomSourceConfig(),
                new DestConfig("unused-transform-preview-index", null),
                null,
                randomBoolean() ? TransformConfigTests.randomSyncConfig() : null,
                null,
                PivotConfigTests.randomPivotConfig(),
                null);
        return new Request(config);
    }

    public void testParsingOverwritesIdAndDestFields() throws IOException {
        // id & dest fields will be set by the parser
        BytesArray json = new BytesArray(
                "{ " +
                    "\"source\":{" +
                    "   \"index\":\"foo\", " +
                    "   \"query\": {\"match_all\": {}}}," +
                    "\"pivot\": {" +
                        "\"group_by\": {\"destination-field2\": {\"terms\": {\"field\": \"term-field\"}}}," +
                        "\"aggs\": {\"avg_response\": {\"avg\": {\"field\": \"responsetime\"}}}" +
                    "}" +
                "}");

        try (XContentParser parser = JsonXContent.jsonXContent
                .createParser(xContentRegistry(), DeprecationHandler.THROW_UNSUPPORTED_OPERATION, json.streamInput())) {

            Request request = Request.fromXContent(parser);
            assertEquals("transform-preview", request.getConfig().getId());
            assertEquals("unused-transform-preview-index", request.getConfig().getDestination().getIndex());
        }
    }
}
