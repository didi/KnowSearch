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

package org.elasticsearch.action.bulk;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.ExceptionsHelper;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.util.concurrent.ThreadContext;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.threadpool.Scheduler;
import org.elasticsearch.threadpool.TestThreadPool;
import org.elasticsearch.threadpool.ThreadPool;
import org.junit.After;
import org.junit.Before;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

public class BulkProcessorTests extends ESTestCase {

    private ThreadPool threadPool;
    private final Logger logger = LogManager.getLogger(BulkProcessorTests.class);

    @Before
    public void startThreadPool() {
        threadPool = new TestThreadPool("BulkProcessorTests");
    }

    @After
    public void stopThreadPool() throws InterruptedException {
        terminate(threadPool);
    }

    public void testBulkProcessorFlushPreservesContext() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final String headerKey = randomAlphaOfLengthBetween(1, 8);
        final String transientKey = randomAlphaOfLengthBetween(1, 8);
        final String headerValue = randomAlphaOfLengthBetween(1, 32);
        final Object transientValue = new Object();

        BiConsumer<BulkRequest, ActionListener<BulkResponse>> consumer = (request, listener) -> {
            ThreadContext threadContext = threadPool.getThreadContext();
            assertEquals(headerValue, threadContext.getHeader(headerKey));
            assertSame(transientValue, threadContext.getTransient(transientKey));
            latch.countDown();
        };

        final int bulkSize = randomIntBetween(2, 32);
        final TimeValue flushInterval = TimeValue.timeValueSeconds(1L);
        final BulkProcessor bulkProcessor;
        assertNull(threadPool.getThreadContext().getHeader(headerKey));
        assertNull(threadPool.getThreadContext().getTransient(transientKey));
        try (ThreadContext.StoredContext ignore = threadPool.getThreadContext().stashContext()) {
            threadPool.getThreadContext().putHeader(headerKey, headerValue);
            threadPool.getThreadContext().putTransient(transientKey, transientValue);
            bulkProcessor = new BulkProcessor(consumer, BackoffPolicy.noBackoff(), emptyListener(),
                1, bulkSize, new ByteSizeValue(5, ByteSizeUnit.MB), flushInterval,
                threadPool, () -> {}, BulkRequest::new);
        }
        assertNull(threadPool.getThreadContext().getHeader(headerKey));
        assertNull(threadPool.getThreadContext().getTransient(transientKey));

        // add a single item which won't be over the size or number of items
        bulkProcessor.add(new IndexRequest());

        // wait for flush to execute
        latch.await();

        assertNull(threadPool.getThreadContext().getHeader(headerKey));
        assertNull(threadPool.getThreadContext().getTransient(transientKey));
        bulkProcessor.close();
    }

    public void testConcurrentExecutions() throws Exception {
        final AtomicBoolean called = new AtomicBoolean(false);
        final AtomicReference<Throwable> exceptionRef = new AtomicReference<>();
        int estimatedTimeForTest = Integer.MAX_VALUE;
        final int simulateWorkTimeInMillis = 5;
        int concurrentClients = 0;
        int concurrentBulkRequests = 0;
        int expectedExecutions = 0;
        int maxBatchSize = 0;
        int maxDocuments = 0;
        int iterations = 0;
        boolean runTest = true;
        //find some randoms that allow this test to take under ~ 10 seconds
        while (estimatedTimeForTest > 10_000) {
            if (iterations++ > 1_000) { //extremely unlikely
                runTest = false;
                break;
            }
            maxBatchSize = randomIntBetween(1, 100);
            maxDocuments = randomIntBetween(maxBatchSize, 1_000_000);
            concurrentClients = randomIntBetween(1, 20);
            concurrentBulkRequests = randomIntBetween(0, 20);
            expectedExecutions = maxDocuments / maxBatchSize;
            estimatedTimeForTest = (expectedExecutions * simulateWorkTimeInMillis) /
                Math.min(concurrentBulkRequests + 1, concurrentClients);
        }
        assumeTrue("failed to find random values that allows test to run quickly", runTest);
        BulkResponse bulkResponse = new BulkResponse(new BulkItemResponse[]{ new BulkItemResponse() }, 0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger requestCount = new AtomicInteger(0);
        AtomicInteger docCount = new AtomicInteger(0);
        BiConsumer<BulkRequest, ActionListener<BulkResponse>> consumer = (request, listener) ->
        {
            try {
                Thread.sleep(simulateWorkTimeInMillis); //simulate work
                listener.onResponse(bulkResponse);
            } catch (InterruptedException e) {
                //should never happen
                Thread.currentThread().interrupt();
                failureCount.getAndIncrement();
                exceptionRef.set(ExceptionsHelper.useOrSuppress(exceptionRef.get(), e));
            }
        };
        try (BulkProcessor bulkProcessor = new BulkProcessor(consumer, BackoffPolicy.noBackoff(),
            countingListener(requestCount, successCount, failureCount, docCount, exceptionRef),
            concurrentBulkRequests, maxBatchSize, new ByteSizeValue(Integer.MAX_VALUE), null,
            (command, delay, executor) -> null, () -> called.set(true), BulkRequest::new)) {

            ExecutorService executorService = Executors.newFixedThreadPool(concurrentClients);
            CountDownLatch startGate = new CountDownLatch(1 + concurrentClients);

            IndexRequest indexRequest = new IndexRequest();
            String bulkRequest = "{ \"index\" : { \"_index\" : \"test\", \"_id\" : \"1\" } }\n" + "{ \"field1\" : \"value1\" }\n";
            BytesReference bytesReference =
                BytesReference.fromByteBuffers(new ByteBuffer[]{ ByteBuffer.wrap(bulkRequest.getBytes(StandardCharsets.UTF_8)) });
            List<Future> futures = new ArrayList<>();
            for (final AtomicInteger i = new AtomicInteger(0); i.getAndIncrement() < maxDocuments; ) {
                futures.add(executorService.submit(() -> {
                    try {
                        //don't start any work until all tasks are submitted
                        startGate.countDown();
                        startGate.await();
                        //alternate between ways to add to the bulk processor
                        if (randomBoolean()) {
                            bulkProcessor.add(indexRequest);
                        } else {
                            bulkProcessor.add(bytesReference, null, null, XContentType.JSON);
                        }
                    } catch (Exception e) {
                        throw ExceptionsHelper.convertToRuntime(e);
                    }
                }));
            }
            startGate.countDown();
            startGate.await();

            for (Future f : futures) {
                try {
                    f.get();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    exceptionRef.set(ExceptionsHelper.useOrSuppress(exceptionRef.get(), e));
                }
            }
            executorService.shutdown();
            executorService.awaitTermination(10, TimeUnit.SECONDS);

            if (failureCount.get() > 0 || successCount.get() != expectedExecutions || requestCount.get() != successCount.get()) {
                if (exceptionRef.get() != null) {
                    logger.error("exception(s) caught during test", exceptionRef.get());
                }
                fail("\nExpected Bulks: " + expectedExecutions + "\n" +
                    "Requested Bulks: " + requestCount.get() + "\n" +
                    "Successful Bulks: " + successCount.get() + "\n" +
                    "Failed Bulks: " + failureCount.get() + "\n" +
                    "Max Documents: " + maxDocuments + "\n" +
                    "Max Batch Size: " + maxBatchSize + "\n" +
                    "Concurrent Clients: " + concurrentClients + "\n" +
                    "Concurrent Bulk Requests: " + concurrentBulkRequests + "\n"
                );
            }
        }
        //count total docs after processor is closed since there may have been partial batches that are flushed on close.
        assertEquals(docCount.get(), maxDocuments);
    }

    public void testConcurrentExecutionsWithFlush() throws Exception {
        final AtomicReference<Throwable> exceptionRef = new AtomicReference<>();
        final int maxDocuments = 100_000;
        final int concurrentClients = 2;
        final int maxBatchSize = Integer.MAX_VALUE; //don't flush based on size
        final int concurrentBulkRequests = randomIntBetween(0, 20);
        final int simulateWorkTimeInMillis = 5;
        BulkResponse bulkResponse = new BulkResponse(new BulkItemResponse[]{ new BulkItemResponse() }, 0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger requestCount = new AtomicInteger(0);
        AtomicInteger docCount = new AtomicInteger(0);
        BiConsumer<BulkRequest, ActionListener<BulkResponse>> consumer = (request, listener) ->
        {
            try {
                Thread.sleep(simulateWorkTimeInMillis); //simulate work
                listener.onResponse(bulkResponse);
            } catch (InterruptedException e) {
                //should never happen
                Thread.currentThread().interrupt();
                failureCount.getAndIncrement();
                exceptionRef.set(ExceptionsHelper.useOrSuppress(exceptionRef.get(), e));
            }
        };
        ScheduledExecutorService flushExecutor = Executors.newScheduledThreadPool(1);
        try (BulkProcessor bulkProcessor = new BulkProcessor(consumer, BackoffPolicy.noBackoff(),
            countingListener(requestCount, successCount, failureCount, docCount, exceptionRef),
            concurrentBulkRequests, maxBatchSize, new ByteSizeValue(Integer.MAX_VALUE),
            TimeValue.timeValueMillis(simulateWorkTimeInMillis * 2),
            (command, delay, executor) ->
                Scheduler.wrapAsScheduledCancellable(flushExecutor.schedule(command, delay.millis(), TimeUnit.MILLISECONDS)),
            () ->
            {
                flushExecutor.shutdown();
                try {
                    flushExecutor.awaitTermination(10L, TimeUnit.SECONDS);
                    if (flushExecutor.isTerminated() == false) {
                        flushExecutor.shutdownNow();
                    }
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            },
            BulkRequest::new)) {

            ExecutorService executorService = Executors.newFixedThreadPool(concurrentClients);
            IndexRequest indexRequest = new IndexRequest();
            String bulkRequest = "{ \"index\" : { \"_index\" : \"test\", \"_id\" : \"1\" } }\n" + "{ \"field1\" : \"value1\" }\n";
            BytesReference bytesReference =
                BytesReference.fromByteBuffers(new ByteBuffer[]{ ByteBuffer.wrap(bulkRequest.getBytes(StandardCharsets.UTF_8)) });
            List<Future> futures = new ArrayList<>();
            CountDownLatch startGate = new CountDownLatch(1 + concurrentClients);
            for (final AtomicInteger i = new AtomicInteger(0); i.getAndIncrement() < maxDocuments; ) {
                futures.add(executorService.submit(() -> {
                    try {
                        //don't start any work until all tasks are submitted
                        startGate.countDown();
                        startGate.await();
                        //alternate between ways to add to the bulk processor
                        if (randomBoolean()) {
                            bulkProcessor.add(indexRequest);
                        } else {
                            bulkProcessor.add(bytesReference, null, null, XContentType.JSON);
                        }
                    } catch (Exception e) {
                        throw ExceptionsHelper.convertToRuntime(e);
                    }
                }));
            }
            startGate.countDown();
            startGate.await();

            for (Future f : futures) {
                try {
                    f.get();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    exceptionRef.set(ExceptionsHelper.useOrSuppress(exceptionRef.get(), e));
                }
            }
            executorService.shutdown();
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        }

        if (failureCount.get() > 0 || requestCount.get() != successCount.get() || maxDocuments != docCount.get()) {
            if (exceptionRef.get() != null) {
                logger.error("exception(s) caught during test", exceptionRef.get());
            }
            fail("\nRequested Bulks: " + requestCount.get() + "\n" +
                "Successful Bulks: " + successCount.get() + "\n" +
                "Failed Bulks: " + failureCount.get() + "\n" +
                "Total Documents: " + docCount.get() + "\n" +
                "Max Documents: " + maxDocuments + "\n" +   
                "Max Batch Size: " + maxBatchSize + "\n" +
                "Concurrent Clients: " + concurrentClients + "\n" +
                "Concurrent Bulk Requests: " + concurrentBulkRequests + "\n"
            );
        }
    }

    public void testAwaitOnCloseCallsOnClose() throws Exception {
        final AtomicBoolean called = new AtomicBoolean(false);
        BiConsumer<BulkRequest, ActionListener<BulkResponse>> consumer = (request, listener) -> { };
        BulkProcessor bulkProcessor = new BulkProcessor(consumer, BackoffPolicy.noBackoff(), emptyListener(),
            0, 10, new ByteSizeValue(1000), null,
            (command, delay, executor) -> null, () -> called.set(true), BulkRequest::new);

        assertFalse(called.get());
        bulkProcessor.awaitClose(100, TimeUnit.MILLISECONDS);
        assertTrue(called.get());
    }

    private BulkProcessor.Listener emptyListener() {
        return new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId, BulkRequest request) {
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
            }
        };
    }

    private BulkProcessor.Listener countingListener(AtomicInteger requestCount, AtomicInteger successCount, AtomicInteger failureCount,
                                                    AtomicInteger docCount, AtomicReference<Throwable> exceptionRef) {

        return new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId, BulkRequest request) {
                requestCount.incrementAndGet();
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
                successCount.incrementAndGet();
                docCount.addAndGet(request.requests().size());
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                if (failure != null) {
                    failureCount.incrementAndGet();
                    exceptionRef.set(ExceptionsHelper.useOrSuppress(exceptionRef.get(), failure));

                }
            }
        };
    }
}
