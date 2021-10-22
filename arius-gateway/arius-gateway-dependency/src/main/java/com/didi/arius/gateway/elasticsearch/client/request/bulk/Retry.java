package com.didi.arius.gateway.elasticsearch.client.request.bulk;

import com.didi.arius.gateway.elasticsearch.client.request.batch.ESBatchRequest;
import com.didi.arius.gateway.elasticsearch.client.response.batch.ESBatchResponse;
import com.didi.arius.gateway.elasticsearch.client.response.batch.IndexResultItemNode;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.support.PlainActionFuture;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.util.concurrent.FutureUtils;
import org.elasticsearch.threadpool.ThreadPool;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

/**
 * Encapsulates synchronous and asynchronous retry logic. While this was designed for use with ESBulkProcessor it is public so it can be used
 * by other code. Specifically it is used by the reindex module.
 */
public class Retry {
    private final String retryOnThrowable;

    private BackoffPolicy backoffPolicy;

    public static Retry on(String retryOnThrowable) {
        return new Retry(retryOnThrowable);
    }

    /**
     * @param backoffPolicy The backoff policy that defines how long and how often to wait for retries.
     */
    public Retry policy(BackoffPolicy backoffPolicy) {
        this.backoffPolicy = backoffPolicy;
        return this;
    }

    Retry(String retryOnThrowable) {
        this.retryOnThrowable = retryOnThrowable;
    }

    /**
     * Invokes #bulk(BulkRequest, ActionListener) on the provided client. Backs off on the provided exception and delegates results to the
     * provided listener.
     *
     * @param client      Client invoking the bulk request.
     * @param bulkRequest The bulk request that should be executed.
     * @param listener    A listener that is invoked when the bulk request finishes or completes with an exception. The listener is not
     */
    public void withAsyncBackoff(ESClient client, ESBatchRequest bulkRequest, ActionListener<ESBatchResponse> listener) {
        AsyncRetryHandler r = new AsyncRetryHandler(retryOnThrowable, backoffPolicy, client, listener);
        r.execute(bulkRequest);

    }

    /**
     * Invokes #bulk(BulkRequest) on the provided client. Backs off on the provided exception.
     *
     * @param client      Client invoking the bulk request.
     * @param bulkRequest The bulk request that should be executed.
     * @return the bulk response as returned by the client.
     * @throws Exception Any exception thrown by the callable.
     */
    public ESBatchResponse withSyncBackoff(ESClient client, ESBatchRequest bulkRequest) throws Exception {
        return SyncRetryHandler
                .create(retryOnThrowable, backoffPolicy, client)
                .executeBlocking(bulkRequest)
                .actionGet();
    }

    static class AbstractRetryHandler implements ActionListener<ESBatchResponse> {
        private final ESLogger logger;
        private final ESClient client;
        private final ActionListener<ESBatchResponse> listener;
        private final Iterator<TimeValue> backoff;
        private final String retryOnThrowable;
        // Access only when holding a client-side lock, see also #addResponses()
        private final List<IndexResultItemNode> responses = new ArrayList<>();
        private final long startTimestampNanos;
        // needed to construct the next bulk request based on the response to the previous one
        // volatile as we're called from a scheduled thread
        private volatile ESBatchRequest currentBulkRequest;
        private volatile ScheduledFuture<?> scheduledRequestFuture;

        public AbstractRetryHandler(String retryOnThrowable, BackoffPolicy backoffPolicy, ESClient client, ActionListener<ESBatchResponse> listener) {
            this.retryOnThrowable = retryOnThrowable;
            this.backoff = backoffPolicy.iterator();
            this.client = client;
            this.listener = listener;
            this.logger = Loggers.getLogger(getClass());
            // in contrast to System.currentTimeMillis(), nanoTime() uses a monotonic clock under the hood
            this.startTimestampNanos = System.nanoTime();
        }

        @Override
        public void onResponse(ESBatchResponse bulkItemResponses) {
            if (!bulkItemResponses.hasFailures()) {
                // we're done here, include all responses
                addResponses(bulkItemResponses, TruePredicate.INSTANCE);
                finishHim();
            } else {
                if (canRetry(bulkItemResponses)) {
                    addResponses(bulkItemResponses, new BulkItemResponsePredicate() {
                        @Override
                        public boolean test(IndexResultItemNode response) {
                            return !response.isFailed();
                        }
                    });
                    retry(createBulkRequestForRetry(bulkItemResponses));
                } else {
                    addResponses(bulkItemResponses, TruePredicate.INSTANCE);
                    finishHim();
                }
            }
        }

        @Override
        public void onFailure(Throwable e) {
            try {
                listener.onFailure(e);
            } finally {
                FutureUtils.cancel(scheduledRequestFuture);
            }
        }

        private void retry(final ESBatchRequest bulkRequestForRetry) {
            assert backoff.hasNext();
            TimeValue next = backoff.next();
            logger.trace("Retry of bulk request scheduled in {} ms.", next.millis());
            scheduledRequestFuture = client.threadPool().schedule(next, ThreadPool.Names.SAME, new Runnable() {
                @Override
                public void run() {
                    AbstractRetryHandler.this.execute(bulkRequestForRetry);
                }
            });
        }

        private ESBatchRequest createBulkRequestForRetry(ESBatchResponse bulkItemResponses) {
            ESBatchRequest requestToReissue = new ESBatchRequest(currentBulkRequest);
            int index = 0;
            for (IndexResultItemNode bulkItemResponse : bulkItemResponses.getItems()) {
                if (bulkItemResponse.isFailed()) {
                    requestToReissue.addNode(currentBulkRequest.requests().get(index));
                }
                index++;
            }
            return requestToReissue;
        }

        private boolean canRetry(ESBatchResponse bulkItemResponses) {
            if (!backoff.hasNext()) {
                return false;
            }
            for (IndexResultItemNode bulkItemResponse : bulkItemResponses.getItems()) {
                if (bulkItemResponse.isFailed()) {
                    String type = bulkItemResponse.getIndex().getError().getType();
                    if (!Strings.toCamelCase(type).equals(retryOnThrowable)) {
                        return false;
                    }
                }
            }
            return true;
        }

        private void finishHim() {
            try {
                listener.onResponse(getAccumulatedResponse());
            } finally {
                FutureUtils.cancel(scheduledRequestFuture);
            }
        }

        private void addResponses(ESBatchResponse response, BulkItemResponsePredicate filter) {
            for (IndexResultItemNode bulkItemResponse : response.getItems()) {
                if (filter.test(bulkItemResponse)) {
                    // Use client-side lock here to avoid visibility issues. This method may be called multiple times
                    // (based on how many retries we have to issue) and relying that the response handling code will be
                    // scheduled on the same thread is fragile.
                    synchronized (responses) {
                        responses.add(bulkItemResponse);
                    }
                }
            }
        }

        private ESBatchResponse getAccumulatedResponse() {
            long stopTimestamp = System.nanoTime();
            long totalLatencyMs = TimeValue.timeValueNanos(stopTimestamp - startTimestampNanos).millis();
            return new ESBatchResponse(responses, totalLatencyMs);
        }

        public void execute(ESBatchRequest bulkRequest) {
            this.currentBulkRequest = bulkRequest;
            client.batch(bulkRequest, this);
        }
    }

    static class AsyncRetryHandler extends AbstractRetryHandler {
        public AsyncRetryHandler(String retryOnThrowable, BackoffPolicy backoffPolicy, ESClient client, ActionListener<ESBatchResponse> listener) {
            super(retryOnThrowable, backoffPolicy, client, listener);
        }
    }

    static class SyncRetryHandler extends AbstractRetryHandler {
        private final PlainActionFuture<ESBatchResponse> actionFuture;

        public static SyncRetryHandler create(String retryOnThrowable, BackoffPolicy backoffPolicy, ESClient client) {
            PlainActionFuture<ESBatchResponse> actionFuture = PlainActionFuture.newFuture();
            return new SyncRetryHandler(retryOnThrowable, backoffPolicy, client, actionFuture);
        }

        public SyncRetryHandler(String retryOnThrowable, BackoffPolicy backoffPolicy, ESClient client, PlainActionFuture<ESBatchResponse> actionFuture) {
            super(retryOnThrowable, backoffPolicy, client, actionFuture);
            this.actionFuture = actionFuture;
        }

        public ActionFuture<ESBatchResponse> executeBlocking(ESBatchRequest bulkRequest) {
            super.execute(bulkRequest);
            return actionFuture;
        }
    }

    private interface BulkItemResponsePredicate {
        boolean test(IndexResultItemNode response);
    }

    private static class TruePredicate implements BulkItemResponsePredicate {
        private static final TruePredicate INSTANCE = new TruePredicate();

        @Override
        public boolean test(IndexResultItemNode response) {
            return true;
        }
    }
}