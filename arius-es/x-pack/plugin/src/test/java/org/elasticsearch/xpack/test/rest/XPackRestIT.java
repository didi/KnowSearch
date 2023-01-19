/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.test.rest;

import com.carrotsearch.randomizedtesting.annotations.ParametersFactory;

import org.apache.http.HttpStatus;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.common.CheckedFunction;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.util.concurrent.ThreadContext;
import org.elasticsearch.common.xcontent.support.XContentMapValues;
import org.elasticsearch.plugins.MetaDataUpgrader;
import org.elasticsearch.test.SecuritySettingsSourceField;
import org.elasticsearch.test.rest.ESRestTestCase;
import org.elasticsearch.test.rest.yaml.ClientYamlTestCandidate;
import org.elasticsearch.test.rest.yaml.ClientYamlTestResponse;
import org.elasticsearch.test.rest.yaml.ESClientYamlSuiteTestCase;
import org.elasticsearch.test.rest.yaml.ObjectPath;
import org.elasticsearch.xpack.core.ml.MlMetaIndex;
import org.elasticsearch.xpack.core.ml.integration.MlRestTestStateCleaner;
import org.elasticsearch.xpack.core.ml.job.persistence.AnomalyDetectorsIndex;
import org.elasticsearch.xpack.core.ml.job.persistence.AnomalyDetectorsIndexFields;
import org.elasticsearch.xpack.core.ml.notifications.AuditorField;
import org.elasticsearch.xpack.core.rollup.job.RollupJob;
import org.elasticsearch.xpack.core.watcher.support.WatcherIndexTemplateRegistryField;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.elasticsearch.common.xcontent.support.XContentMapValues.extractValue;
import static org.elasticsearch.rest.action.search.RestSearchAction.TOTAL_HITS_AS_INT_PARAM;
import static org.elasticsearch.xpack.core.security.authc.support.UsernamePasswordToken.basicAuthHeaderValue;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

/** Runs rest tests against external cluster */
public class XPackRestIT extends ESClientYamlSuiteTestCase {
    private static final String BASIC_AUTH_VALUE =
            basicAuthHeaderValue("x_pack_rest_user", SecuritySettingsSourceField.TEST_PASSWORD_SECURE_STRING);

    public XPackRestIT(ClientYamlTestCandidate testCandidate) {
        super(testCandidate);
    }

    @ParametersFactory
    public static Iterable<Object[]> parameters() throws Exception {
        return ESClientYamlSuiteTestCase.createParameters();
    }

    @Override
    protected Settings restClientSettings() {
        return Settings.builder()
                .put(ThreadContext.PREFIX + ".Authorization", BASIC_AUTH_VALUE)
                .build();
    }


    @Before
    public void setupForTests() throws Exception {
        waitForTemplates();
        waitForWatcher();
        enableMonitoring();
    }

    /**
     * Waits for the Security template and the Machine Learning templates to be created by the {@link MetaDataUpgrader}
     */
    private void waitForTemplates() throws Exception {
        if (installTemplates()) {
            List<String> templates = new ArrayList<>();
            templates.addAll(
                Arrays.asList(
                    AuditorField.NOTIFICATIONS_INDEX,
                    MlMetaIndex.INDEX_NAME,
                    AnomalyDetectorsIndexFields.STATE_INDEX_PREFIX,
                    AnomalyDetectorsIndex.jobResultsIndexPrefix(),
                    AnomalyDetectorsIndex.configIndexName()));

            for (String template : templates) {
                awaitCallApi("indices.exists_template", singletonMap("name", template), emptyList(),
                        response -> true,
                        () -> "Exception when waiting for [" + template + "] template to be created");
            }
        }
    }

    private void waitForWatcher() throws Exception {
        // ensure watcher is started, so that a test can stop watcher and everything still works fine
        if (isWatcherTest()) {
            assertBusy(() -> {
                ClientYamlTestResponse response =
                    getAdminExecutionContext().callApi("watcher.stats", emptyMap(), emptyList(), emptyMap());
                String state = (String) response.evaluate("stats.0.watcher_state");

                switch (state) {
                    case "stopped":
                        ClientYamlTestResponse startResponse =
                            getAdminExecutionContext().callApi("watcher.start", emptyMap(), emptyList(), emptyMap());
                        boolean isAcknowledged = (boolean) startResponse.evaluate("acknowledged");
                        assertThat(isAcknowledged, is(true));
                        throw new AssertionError("waiting until stopped state reached started state");
                    case "stopping":
                        throw new AssertionError("waiting until stopping state reached stopped state to start again");
                    case "starting":
                        throw new AssertionError("waiting until starting state reached started state");
                    case "started":
                        // all good here, we are done
                        break;
                    default:
                        throw new AssertionError("unknown state[" + state + "]");
                }
            });

            for (String template : WatcherIndexTemplateRegistryField.TEMPLATE_NAMES) {
                awaitCallApi("indices.exists_template", singletonMap("name", template), emptyList(),
                    response -> true,
                    () -> "Exception when waiting for [" + template + "] template to be created");
            }

            boolean existsWatcherIndex = adminClient()
                    .performRequest(new Request("HEAD", ".watches"))
                    .getStatusLine().getStatusCode() == 200;
            if (existsWatcherIndex == false) {
                return;
            }
            Request searchWatchesRequest = new Request("GET", ".watches/_search");
            searchWatchesRequest.addParameter(TOTAL_HITS_AS_INT_PARAM, "true");
            searchWatchesRequest.addParameter("size", "1000");
            Response response = adminClient().performRequest(searchWatchesRequest);
            ObjectPath objectPathResponse = ObjectPath.createFromResponse(response);
            int totalHits = objectPathResponse.evaluate("hits.total");
            if (totalHits > 0) {
                List<Map<String, Object>> hits = objectPathResponse.evaluate("hits.hits");
                for (Map<String, Object> hit : hits) {
                    String id = (String) hit.get("_id");
                    adminClient().performRequest(new Request("DELETE", "_watcher/watch/" + id));
                }
            }
        }
    }

    /**
     * Enable monitoring and waits for monitoring documents to be collected and indexed in
     * monitoring indices.This is the signal that the local exporter is started and ready
     * for the tests.
     */
    private void enableMonitoring() throws Exception {
        if (isMonitoringTest()) {
            final ClientYamlTestResponse xpackUsage =
                    callApi("xpack.usage", singletonMap("filter_path", "monitoring.enabled_exporters"), emptyList(), getApiCallHeaders());

            @SuppressWarnings("unchecked")
            final Map<String, Object> exporters = (Map<String, Object>) xpackUsage.evaluate("monitoring.enabled_exporters");
            assertNotNull("List of monitoring exporters must not be null", exporters);
            assertThat("List of enabled exporters must be empty before enabling monitoring",
                    XContentMapValues.extractRawValues("monitoring.enabled_exporters", exporters), hasSize(0));

            final Map<String, Object> settings = new HashMap<>();
            settings.put("xpack.monitoring.collection.enabled", true);
            settings.put("xpack.monitoring.collection.interval", "1s");
            settings.put("xpack.monitoring.exporters._local.enabled", true);

            awaitCallApi("cluster.put_settings", emptyMap(),
                    singletonList(singletonMap("transient", settings)),
                    response -> {
                        Object acknowledged = response.evaluate("acknowledged");
                        return acknowledged != null && (Boolean) acknowledged;
                    },
                    () -> "Exception when enabling monitoring");
            Map<String, String> searchParams = new HashMap<>();
            searchParams.put("index", ".monitoring-*");
            searchParams.put(TOTAL_HITS_AS_INT_PARAM, "true");
            awaitCallApi("search", searchParams, emptyList(),
                    response -> ((Number) response.evaluate("hits.total")).intValue() > 0,
                    () -> "Exception when waiting for monitoring documents to be indexed");
        }
    }

    /**
     * Disable monitoring
     */
    private void disableMonitoring() throws Exception {
        if (isMonitoringTest()) {
            final Map<String, Object> settings = new HashMap<>();
            settings.put("xpack.monitoring.collection.enabled", null);
            settings.put("xpack.monitoring.collection.interval", null);
            settings.put("xpack.monitoring.exporters._local.enabled", null);

            awaitCallApi("cluster.put_settings", emptyMap(),
                    singletonList(singletonMap("transient", settings)),
                    response -> {
                        Object acknowledged = response.evaluate("acknowledged");
                        return acknowledged != null && (Boolean) acknowledged;
                    },
                    () -> "Exception when disabling monitoring");

            assertBusy(() -> {
                try {
                    ClientYamlTestResponse response =
                            callApi("xpack.usage", singletonMap("filter_path", "monitoring.enabled_exporters"), emptyList(),
                                    getApiCallHeaders());

                    @SuppressWarnings("unchecked")
                    final Map<String, ?> exporters = (Map<String, ?>) response.evaluate("monitoring.enabled_exporters");
                    if (exporters.isEmpty() == false) {
                        fail("Exporters were not found");
                    }

                    final Map<String, String> params = new HashMap<>();
                    params.put("node_id", "_local");
                    params.put("metric", "thread_pool");
                    params.put("filter_path", "nodes.*.thread_pool.write.active");
                    response = callApi("nodes.stats", params, emptyList(), getApiCallHeaders());

                    @SuppressWarnings("unchecked")
                    final Map<String, Object> nodes = (Map<String, Object>) response.evaluate("nodes");
                    @SuppressWarnings("unchecked")
                    final Map<String, Object> node = (Map<String, Object>) nodes.values().iterator().next();

                    final Number activeWrites = (Number) extractValue("thread_pool.write.active", node);
                    assertNotNull(activeWrites);
                    assertThat(activeWrites, equalTo(0));
                } catch (Exception e) {
                    throw new ElasticsearchException("Failed to wait for monitoring exporters to stop:", e);
                }
            });
        }
    }

    /**
     * Cleanup after tests.
     *
     * Feature-specific cleanup methods should be called from here rather than using
     * separate @After annotated methods to ensure there is a well-defined cleanup order.
     */
    @After
    public void cleanup() throws Exception {
        disableMonitoring();
        clearMlState();
        if (isWaitForPendingTasks()) {
            // This waits for pending tasks to complete, so must go last (otherwise
            // it could be waiting for pending tasks while monitoring is still running).
            ESRestTestCase.waitForPendingTasks(adminClient(), task -> {
                    // Don't check rollup jobs because we clear them in the superclass.
                    return task.contains(RollupJob.NAME);
            });
        }
    }

    /**
     * Delete any left over machine learning datafeeds and jobs.
     */
    private void clearMlState() throws Exception {
        if (isMachineLearningTest()) {
            new MlRestTestStateCleaner(logger, adminClient()).clearMlMetadata();
        }
    }

    /**
     * Executes an API call using the admin context, waiting for it to succeed.
     */
    private void awaitCallApi(String apiName,
                              Map<String, String> params,
                              List<Map<String, Object>> bodies,
                              CheckedFunction<ClientYamlTestResponse, Boolean, IOException> success,
                              Supplier<String> error) {
        try {
            // The actual method call that sends the API requests returns a Future, but we immediately
            // call .get() on it so there's no need for this method to do any other awaiting.
            ClientYamlTestResponse response = callApi(apiName, params, bodies, getApiCallHeaders());
            assertEquals(response.getStatusCode(), HttpStatus.SC_OK);
            success.apply(response);
        } catch (Exception e) {
            throw new IllegalStateException(error.get(), e);
        }
    }

    private ClientYamlTestResponse callApi(String apiName,
                                           Map<String, String> params,
                                           List<Map<String, Object>> bodies,
                                           Map<String, String> headers) throws IOException {
        return getAdminExecutionContext().callApi(apiName, params, bodies, headers);
    }

    protected Map<String, String> getApiCallHeaders() {
        return Collections.emptyMap();
    }

    protected boolean installTemplates() {
        return true;
    }

    protected boolean isMonitoringTest() {
        String testName = getTestName();
        return testName != null && (testName.contains("=monitoring/") || testName.contains("=monitoring\\"));
    }

    protected boolean isWatcherTest() {
        String testName = getTestName();
        return testName != null && (testName.contains("=watcher/") || testName.contains("=watcher\\"));
    }

    protected boolean isMachineLearningTest() {
        String testName = getTestName();
        return testName != null && (testName.contains("=ml/") || testName.contains("=ml\\"));
    }

    /**
     * Should each test wait for pending tasks to finish after execution?
     * @return Wait for pending tasks
     */
    protected boolean isWaitForPendingTasks() {
        return true;
    }

}
