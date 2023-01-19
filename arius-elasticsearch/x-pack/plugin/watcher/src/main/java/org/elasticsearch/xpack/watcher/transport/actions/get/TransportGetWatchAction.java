/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.watcher.transport.actions.get;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.routing.Preference;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.license.XPackLicenseState;
import org.elasticsearch.transport.TransportService;
import org.elasticsearch.xpack.core.watcher.support.xcontent.WatcherParams;
import org.elasticsearch.xpack.core.watcher.support.xcontent.XContentSource;
import org.elasticsearch.xpack.core.watcher.transport.actions.get.GetWatchAction;
import org.elasticsearch.xpack.core.watcher.transport.actions.get.GetWatchRequest;
import org.elasticsearch.xpack.core.watcher.transport.actions.get.GetWatchResponse;
import org.elasticsearch.xpack.core.watcher.watch.Watch;
import org.elasticsearch.xpack.watcher.transport.actions.WatcherTransportAction;
import org.elasticsearch.xpack.watcher.watch.WatchParser;

import java.time.Clock;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.xpack.core.ClientHelper.WATCHER_ORIGIN;
import static org.elasticsearch.xpack.core.ClientHelper.executeAsyncWithOrigin;

public class TransportGetWatchAction extends WatcherTransportAction<GetWatchRequest, GetWatchResponse> {

    private final WatchParser parser;
    private final Clock clock;
    private final Client client;

    @Inject
    public TransportGetWatchAction(TransportService transportService, ActionFilters actionFilters,
                                   XPackLicenseState licenseState, WatchParser parser, Clock clock, Client client) {
        super(GetWatchAction.NAME, transportService, actionFilters, licenseState, GetWatchRequest::new);
        this.parser = parser;
        this.clock = clock;
        this.client = client;
    }

    @Override
    protected void doExecute(GetWatchRequest request, ActionListener<GetWatchResponse> listener) {
        GetRequest getRequest = new GetRequest(Watch.INDEX, request.getId())
                .preference(Preference.LOCAL.type()).realtime(true);

        executeAsyncWithOrigin(client.threadPool().getThreadContext(), WATCHER_ORIGIN, getRequest,
                ActionListener.<GetResponse>wrap(getResponse -> {
                    if (getResponse.isExists()) {
                        try (XContentBuilder builder = jsonBuilder()) {
                            // When we return the watch via the Get Watch REST API, we want to return the watch as was specified in
                            // the put api, we don't include the status in the watch source itself, but as a separate top level field,
                            // so that it indicates the status is managed by watcher itself.
                            ZonedDateTime now = clock.instant().atZone(ZoneOffset.UTC);
                            Watch watch = parser.parseWithSecrets(request.getId(), true, getResponse.getSourceAsBytesRef(), now,
                                    XContentType.JSON, getResponse.getSeqNo(), getResponse.getPrimaryTerm());
                            watch.toXContent(builder, WatcherParams.builder()
                                    .hideSecrets(true)
                                    .includeStatus(false)
                                    .build());
                            watch.status().version(getResponse.getVersion());
                            listener.onResponse(new GetWatchResponse(watch.id(), getResponse.getVersion(),
                                watch.getSourceSeqNo(), watch.getSourcePrimaryTerm(),
                                watch.status(), new XContentSource(BytesReference.bytes(builder), XContentType.JSON)));
                        }
                    } else {
                        listener.onResponse(new GetWatchResponse(request.getId()));
                    }
                }, e -> {
                    // special case. This API should not care if the index is missing or not,
                    // it should respond with the watch not being found
                    if (e instanceof IndexNotFoundException) {
                        listener.onResponse(new GetWatchResponse(request.getId()));
                    } else {
                        listener.onFailure(e);
                    }
                }), client::get);
    }
}
