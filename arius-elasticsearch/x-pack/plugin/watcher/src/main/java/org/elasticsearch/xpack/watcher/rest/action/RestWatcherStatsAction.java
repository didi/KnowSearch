/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */

package org.elasticsearch.xpack.watcher.rest.action;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.elasticsearch.common.logging.DeprecationLogger;
import org.elasticsearch.common.Strings;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.RestActions;
import org.elasticsearch.xpack.core.watcher.client.WatcherClient;
import org.elasticsearch.xpack.core.watcher.transport.actions.stats.WatcherStatsRequest;
import org.elasticsearch.xpack.watcher.rest.WatcherRestHandler;

import java.util.Collections;
import java.util.Set;

import static org.elasticsearch.rest.RestRequest.Method.GET;

public class RestWatcherStatsAction extends WatcherRestHandler {
    private static final Logger logger = LogManager.getLogger(RestWatcherStatsAction.class);
    private static final DeprecationLogger deprecationLogger = new DeprecationLogger(logger);

    public RestWatcherStatsAction(RestController controller) {
        // TODO: remove deprecated endpoint in 8.0.0
        controller.registerWithDeprecatedHandler(
            GET, "/_watcher/stats", this,
            GET, URI_BASE + "/watcher/stats", deprecationLogger);
        controller.registerWithDeprecatedHandler(
            GET, "/_watcher/stats/{metric}", this,
            GET, URI_BASE + "/watcher/stats/{metric}", deprecationLogger);
    }

    @Override
    public String getName() {
        return "watcher_stats";
    }

    @Override
    protected RestChannelConsumer doPrepareRequest(final RestRequest restRequest, WatcherClient client) {
        Set<String> metrics = Strings.tokenizeByCommaToSet(restRequest.param("metric", ""));

        WatcherStatsRequest request = new WatcherStatsRequest();
        if (metrics.contains("_all")) {
            request.includeCurrentWatches(true);
            request.includeQueuedWatches(true);
        } else {
            request.includeCurrentWatches(metrics.contains("current_watches"));
            request.includeQueuedWatches(metrics.contains("queued_watches") || metrics.contains("pending_watches"));
        }

        if (metrics.contains("pending_watches")) {
            deprecationLogger.deprecated("The pending_watches parameter is deprecated, use queued_watches instead");
        }


        return channel -> client.watcherStats(request, new RestActions.NodesResponseRestListener<>(channel));
    }

    private static final Set<String> RESPONSE_PARAMS = Collections.singleton("emit_stacktraces");

    @Override
    protected Set<String> responseParams() {
        // this parameter is only needed when current watches are supposed to be returned
        // it's used in the WatchExecutionContext.toXContent() method
        return RESPONSE_PARAMS;
    }
}
