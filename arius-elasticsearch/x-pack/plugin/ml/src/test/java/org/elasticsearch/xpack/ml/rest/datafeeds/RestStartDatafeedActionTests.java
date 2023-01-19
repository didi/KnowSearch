/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.ml.rest.datafeeds;

import org.elasticsearch.ElasticsearchParseException;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.test.rest.FakeRestRequest;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

public class RestStartDatafeedActionTests extends ESTestCase {

    public void testPrepareRequest() throws Exception {
        RestStartDatafeedAction action = new RestStartDatafeedAction(
            mock(RestController.class));

        Map<String, String> params = new HashMap<>();
        params.put("start", "not-a-date");
        params.put("datafeed_id", "foo-datafeed");
        RestRequest restRequest1 = new FakeRestRequest.Builder(NamedXContentRegistry.EMPTY)
                .withParams(params).build();
        ElasticsearchParseException e =  expectThrows(ElasticsearchParseException.class,
                () -> action.prepareRequest(restRequest1, mock(NodeClient.class)));
        assertEquals("Query param [start] with value [not-a-date] cannot be parsed as a date or " +
                        "converted to a number (epoch).",
                e.getMessage());

        params = new HashMap<>();
        params.put("start", "now");
        params.put("end", "not-a-date");
        params.put("datafeed_id", "foo-datafeed");
        RestRequest restRequest2 = new FakeRestRequest.Builder(NamedXContentRegistry.EMPTY)
                .withParams(params).build();
        e =  expectThrows(ElasticsearchParseException.class,
                () -> action.prepareRequest(restRequest2, mock(NodeClient.class)));
        assertEquals("Query param [end] with value [not-a-date] cannot be parsed as a date or " +
                        "converted to a number (epoch).", e.getMessage());
    }
}
