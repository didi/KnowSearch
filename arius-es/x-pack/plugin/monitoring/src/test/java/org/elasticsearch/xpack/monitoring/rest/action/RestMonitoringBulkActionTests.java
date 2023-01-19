/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.monitoring.rest.action;

import org.elasticsearch.ElasticsearchParseException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.CheckedConsumer;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestResponse;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.rest.action.RestBuilderListener;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.test.rest.FakeRestRequest;
import org.elasticsearch.xpack.core.XPackClient;
import org.elasticsearch.xpack.core.monitoring.MonitoredSystem;
import org.elasticsearch.xpack.core.monitoring.action.MonitoringBulkRequestBuilder;
import org.elasticsearch.xpack.core.monitoring.action.MonitoringBulkResponse;
import org.elasticsearch.xpack.core.monitoring.client.MonitoringClient;
import org.elasticsearch.xpack.core.monitoring.exporter.MonitoringTemplateUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.elasticsearch.xpack.core.monitoring.exporter.MonitoringTemplateUtils.TEMPLATE_VERSION;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RestMonitoringBulkActionTests extends ESTestCase {

    private final RestController controller = mock(RestController.class);

    private final RestMonitoringBulkAction action = new RestMonitoringBulkAction(controller);

    public void testGetName() {
        // Are you sure that you want to change the name?
        assertThat(action.getName(), is("monitoring_bulk"));
    }

    public void testSupportsContentStream() {
        // if you change this, it's a very breaking change for Monitoring
        assertThat(action.supportsContentStream(), is(true));
    }

    public void testMissingSystemId() {
        final RestRequest restRequest = createRestRequest(null, TEMPLATE_VERSION, "10s");

        final IllegalArgumentException exception = expectThrows(IllegalArgumentException.class, () -> prepareRequest(restRequest));
        assertThat(exception.getMessage(), containsString("no [system_id] for monitoring bulk request"));
    }

    public void testMissingSystemApiVersion() {
        final RestRequest restRequest = createRestRequest(randomSystem().getSystem(), null, "10s");

        final IllegalArgumentException exception = expectThrows(IllegalArgumentException.class, () -> prepareRequest(restRequest));
        assertThat(exception.getMessage(), containsString("no [system_api_version] for monitoring bulk request"));
    }

    public void testMissingInterval() {
        final RestRequest restRequest = createRestRequest(randomSystem().getSystem(), TEMPLATE_VERSION, null);

        final IllegalArgumentException exception = expectThrows(IllegalArgumentException.class, () -> prepareRequest(restRequest));
        assertThat(exception.getMessage(), containsString("no [interval] for monitoring bulk request"));
    }

    public void testWrongInterval() {
        final RestRequest restRequest = createRestRequest(randomSystem().getSystem(), TEMPLATE_VERSION, "null");

        final IllegalArgumentException exception = expectThrows(IllegalArgumentException.class, () -> prepareRequest(restRequest));
        assertThat(exception.getMessage(), containsString("failed to parse setting [interval] with value [null]"));
    }

    public void testMissingContent() {
        final RestRequest restRequest = createRestRequest(0, randomSystem().getSystem(), TEMPLATE_VERSION, "30s");

        final ElasticsearchParseException exception = expectThrows(ElasticsearchParseException.class, () -> prepareRequest(restRequest));
        assertThat(exception.getMessage(), containsString("no body content for monitoring bulk request"));
    }

    public void testUnsupportedSystemVersion() {
        final String systemApiVersion = randomFrom(TEMPLATE_VERSION, MonitoringTemplateUtils.OLD_TEMPLATE_VERSION);
        final RestRequest restRequest = createRestRequest(MonitoredSystem.UNKNOWN.getSystem(), systemApiVersion, "30s");

        final IllegalArgumentException exception = expectThrows(IllegalArgumentException.class, () -> prepareRequest(restRequest));
        assertThat(exception.getMessage(),
                   containsString("system_api_version [" + systemApiVersion + "] is not supported by system_id [unknown]"));
    }

    public void testUnknownSystemVersion() {
        final MonitoredSystem system = randomSystem();
        final RestRequest restRequest = createRestRequest(system.getSystem(), "0", "30s");

        final IllegalArgumentException exception = expectThrows(IllegalArgumentException.class, () -> prepareRequest(restRequest));
        assertThat(exception.getMessage(),
                   containsString("system_api_version [0] is not supported by system_id [" + system.getSystem() + "]"));
    }

    public void testNoErrors() throws Exception {
        final MonitoringBulkResponse response = new MonitoringBulkResponse(randomLong(), false);
        final FakeRestRequest request = createRestRequest(randomSystemId(), TEMPLATE_VERSION, "10s");
        final RestResponse restResponse = getRestBuilderListener(request).buildResponse(response);

        assertThat(restResponse.status(), is(RestStatus.OK));
        assertThat(restResponse.content().utf8ToString(),
                   is("{\"took\":" + response.getTookInMillis() + ",\"ignored\":false,\"errors\":false}"));
    }

    public void testNoErrorsButIgnored() throws Exception {
        final MonitoringBulkResponse response = new MonitoringBulkResponse(randomLong(), true);
        final FakeRestRequest request = createRestRequest(randomSystemId(), TEMPLATE_VERSION, "10s");
        final RestResponse restResponse = getRestBuilderListener(request).buildResponse(response);

        assertThat(restResponse.status(), is(RestStatus.OK));
        assertThat(restResponse.content().utf8ToString(),
                is("{\"took\":" + response.getTookInMillis() + ",\"ignored\":true,\"errors\":false}"));
    }

    public void testWithErrors() throws Exception {
        final RuntimeException e = new RuntimeException("TEST - expected");
        final MonitoringBulkResponse.Error error = new MonitoringBulkResponse.Error(e);
        final MonitoringBulkResponse response = new MonitoringBulkResponse(randomLong(), error);
        final String errorJson;

        final FakeRestRequest request = createRestRequest(randomSystemId(), TEMPLATE_VERSION, "10s");
        final RestResponse restResponse = getRestBuilderListener(request).buildResponse(response);

        try (XContentBuilder builder = XContentBuilder.builder(XContentType.JSON.xContent())) {
            error.toXContent(builder, ToXContent.EMPTY_PARAMS);
            errorJson = Strings.toString(builder);
        }

        assertThat(restResponse.status(), is(RestStatus.INTERNAL_SERVER_ERROR));
        assertThat(restResponse.content().utf8ToString(),
                   is("{\"took\":" + response.getTookInMillis() + ",\"ignored\":false,\"errors\":true,\"error\":" + errorJson + "}"));
    }

    /**
     * Returns a {@link MonitoredSystem} supported by the Monitoring Bulk API
     */
    private static MonitoredSystem randomSystem() {
        return randomFrom(MonitoredSystem.LOGSTASH, MonitoredSystem.KIBANA, MonitoredSystem.BEATS);
    }

    /**
     * Returns a {@link String} representing a {@link MonitoredSystem} supported by the Monitoring Bulk API
     */
    private static String randomSystemId() {
        return randomSystem().getSystem();
    }

    private void prepareRequest(final RestRequest restRequest) throws Exception {
        getRestBuilderListener(restRequest);
    }

    private RestBuilderListener<MonitoringBulkResponse> getRestBuilderListener(final RestRequest restRequest) throws Exception {
        final Client client = mock(Client.class);
        final XPackClient xpackClient = mock(XPackClient.class);
        final MonitoringClient monitoringClient = mock(MonitoringClient.class);
        final AtomicReference<RestBuilderListener<MonitoringBulkResponse>> listenerReference = new AtomicReference<>();
        final MonitoringBulkRequestBuilder builder = new MonitoringBulkRequestBuilder(client){
            @SuppressWarnings("unchecked")
            @Override
            public void execute(ActionListener<MonitoringBulkResponse> listener) {
                listenerReference.set((RestBuilderListener)listener);
            }
        };
        when(monitoringClient.prepareMonitoringBulk()).thenReturn(builder);
        when(xpackClient.monitoring()).thenReturn(monitoringClient);

        final CheckedConsumer<RestChannel, Exception> consumer = action.doPrepareRequest(restRequest, xpackClient);

        final RestChannel channel = mock(RestChannel.class);
        when(channel.newBuilder()).thenReturn(JsonXContent.contentBuilder());

        // trigger/capture execution
        consumer.accept(channel);

        assertThat(listenerReference.get(), not(nullValue()));

        return listenerReference.get();
    }

    private static FakeRestRequest createRestRequest(final String systemId, final String systemApiVersion, final String interval) {
        return createRestRequest(randomIntBetween(1, 10), systemId, systemApiVersion, interval);
    }

    private static FakeRestRequest createRestRequest(final int nbDocs,
                                                     final String systemId,
                                                     final String systemApiVersion,
                                                     final String interval) {
        final FakeRestRequest.Builder builder = new FakeRestRequest.Builder(NamedXContentRegistry.EMPTY);
        if (nbDocs > 0) {
            final StringBuilder requestBody = new StringBuilder();
            for (int n = 0; n < nbDocs; n++) {
                requestBody.append("{\"index\":{\"_type\":\"_doc\"}}\n");
                requestBody.append("{\"field\":").append(n).append("}\n");
            }
            requestBody.append("\n");
            builder.withContent(new BytesArray(requestBody.toString()), XContentType.JSON);
        }

        final Map<String, String> parameters = new HashMap<>();
        if (systemId != null) {
            parameters.put(RestMonitoringBulkAction.MONITORING_ID, systemId);
        }
        if (systemApiVersion != null) {
            parameters.put(RestMonitoringBulkAction.MONITORING_VERSION, systemApiVersion);
        }
        if (interval != null) {
            parameters.put(RestMonitoringBulkAction.INTERVAL, interval);
        }
        builder.withParams(parameters);

        return builder.build();
    }

}
