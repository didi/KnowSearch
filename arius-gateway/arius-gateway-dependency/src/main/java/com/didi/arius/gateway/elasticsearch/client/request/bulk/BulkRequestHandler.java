package com.didi.arius.gateway.elasticsearch.client.request.bulk;

import com.didi.arius.gateway.elasticsearch.client.request.batch.ESBatchRequest;
import com.didi.arius.gateway.elasticsearch.client.response.batch.ESBatchResponse;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Abstracts the low-level details of bulk request handling
 */
abstract class BulkRequestHandler {
    protected final ESLogger logger;
    protected final ESClient client;

    private static final String RETRY_ON_THROWABLE = "EsRejectedExecutionException";

    protected BulkRequestHandler(ESClient client) {
        this.client = client;
        this.logger = Loggers.getLogger(getClass());
    }


    public abstract void execute(ESBatchRequest bulkRequest, long executionId);

    public abstract boolean awaitClose(long timeout, TimeUnit unit) throws InterruptedException;


    public static BulkRequestHandler syncHandler(ESClient client, BackoffPolicy backoffPolicy, ESBulkProcessor.Listener listener) {
        return new SyncBulkRequestHandler(client, backoffPolicy, listener);
    }

    public static BulkRequestHandler asyncHandler(ESClient client, BackoffPolicy backoffPolicy, ESBulkProcessor.Listener listener, int concurrentRequests) {
        return new AsyncBulkRequestHandler(client, backoffPolicy, listener, concurrentRequests);
    }

    private static class SyncBulkRequestHandler extends BulkRequestHandler {
        private final ESBulkProcessor.Listener listener;
        private final BackoffPolicy backoffPolicy;

        public SyncBulkRequestHandler(ESClient client, BackoffPolicy backoffPolicy, ESBulkProcessor.Listener listener) {
            super(client);
            this.backoffPolicy = backoffPolicy;
            this.listener = listener;
        }

        @Override
        public void execute(ESBatchRequest bulkRequest, long executionId) {
            boolean afterCalled = false;
            try {
                listener.beforeBulk(executionId, bulkRequest);
                ESBatchResponse bulkResponse = Retry
                        .on(RETRY_ON_THROWABLE)
                        .policy(backoffPolicy)
                        .withSyncBackoff(client, bulkRequest);
                afterCalled = true;
                listener.afterBulk(executionId, bulkRequest, bulkResponse);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.info("Bulk request {} has been cancelled.", e, executionId);
                if (!afterCalled) {
                    listener.afterBulk(executionId, bulkRequest, e);
                }
            } catch (Throwable t) {
                logger.warn("Failed to execute bulk request {}.", t, executionId);
                if (!afterCalled) {
                    listener.afterBulk(executionId, bulkRequest, t);
                }
            }
        }

        @Override
        public boolean awaitClose(long timeout, TimeUnit unit) throws InterruptedException {
            // we are "closed" immediately as there is no request in flight
            return true;
        }
    }

    private static class AsyncBulkRequestHandler extends BulkRequestHandler {
        private final BackoffPolicy backoffPolicy;
        private final ESBulkProcessor.Listener listener;
        private final Semaphore semaphore;
        private final int concurrentRequests;

        private AsyncBulkRequestHandler(ESClient client, BackoffPolicy backoffPolicy, ESBulkProcessor.Listener listener, int concurrentRequests) {
            super(client);
            this.backoffPolicy = backoffPolicy;
            assert concurrentRequests > 0;
            this.listener = listener;
            this.concurrentRequests = concurrentRequests;
            this.semaphore = new Semaphore(concurrentRequests);
        }

        @Override
        public void execute(final ESBatchRequest bulkRequest, final long executionId) {
            boolean bulkRequestSetupSuccessful = false;
            boolean acquired = false;
            try {
                listener.beforeBulk(executionId, bulkRequest);
                semaphore.acquire();
                acquired = true;
                Retry.on(RETRY_ON_THROWABLE)
                        .policy(backoffPolicy)
                        .withAsyncBackoff(client, bulkRequest, new ActionListener<ESBatchResponse>() {
                            @Override
                            public void onResponse(ESBatchResponse response) {
                                try {
                                    listener.afterBulk(executionId, bulkRequest, response);
                                } finally {
                                    semaphore.release();
                                }
                            }

                            @Override
                            public void onFailure(Throwable e) {
                                try {
                                    listener.afterBulk(executionId, bulkRequest, e);
                                } finally {
                                    semaphore.release();
                                }
                            }
                        });
                bulkRequestSetupSuccessful = true;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.info("Bulk request {} has been cancelled.", e, executionId);
                listener.afterBulk(executionId, bulkRequest, e);
            } catch (Throwable t) {
                logger.warn("Failed to execute bulk request {}.", t, executionId);
                listener.afterBulk(executionId, bulkRequest, t);
            } finally {
                if (!bulkRequestSetupSuccessful && acquired) {  // if we fail on client.bulk() release the semaphore
                    semaphore.release();
                }
            }
        }

        @Override
        public boolean awaitClose(long timeout, TimeUnit unit) throws InterruptedException {
            if (semaphore.tryAcquire(this.concurrentRequests, timeout, unit)) {
                semaphore.release(this.concurrentRequests);
                return true;
            }
            return false;
        }
    }
}
