/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.security.support;

import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.support.PlainActionFuture;
import org.elasticsearch.common.settings.SecureString;
import org.elasticsearch.common.util.concurrent.AbstractRunnable;
import org.elasticsearch.test.SecurityIntegTestCase;
import org.elasticsearch.xpack.core.security.action.user.PutUserRequest;
import org.elasticsearch.xpack.core.security.action.user.PutUserResponse;
import org.hamcrest.Matchers;
import org.junit.After;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SecurityIndexManagerIntegTests extends SecurityIntegTestCase {

    public void testConcurrentOperationsTryingToCreateSecurityIndexAndAlias() throws Exception {
        assertSecurityIndexActive();
        final int processors = Runtime.getRuntime().availableProcessors();
        final int numThreads = scaledRandomIntBetween((processors + 1) / 2, 4 * processors);
        final int maxNumRequests = 100 / numThreads; // bound to a maximum of 100 requests
        final int numRequests = scaledRandomIntBetween(Math.min(4, maxNumRequests), maxNumRequests);

        final List<ActionFuture<PutUserResponse>> futures = new CopyOnWriteArrayList<>();
        final List<Exception> exceptions = new CopyOnWriteArrayList<>();
        final Thread[] threads = new Thread[numThreads];
        final CyclicBarrier barrier = new CyclicBarrier(threads.length);
        final AtomicInteger userNumber = new AtomicInteger(0);
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new AbstractRunnable() {
                @Override
                public void onFailure(Exception e) {
                    exceptions.add(e);
                }

                @Override
                protected void doRun() throws Exception {
                    final List<PutUserRequest> requests = new ArrayList<>(numRequests);
                    final SecureString password = new SecureString("password".toCharArray());
                    for (int i = 0; i < numRequests; i++) {
                        requests.add(securityClient()
                                .preparePutUser("user" + userNumber.getAndIncrement(), password,
                                    getFastStoredHashAlgoForTests(),
                                    randomAlphaOfLengthBetween(1, 16))
                                .request());
                    }

                    barrier.await(10L, TimeUnit.SECONDS);

                    for (PutUserRequest request : requests) {
                        PlainActionFuture<PutUserResponse> responsePlainActionFuture = new PlainActionFuture<>();
                        securityClient().putUser(request, responsePlainActionFuture);
                        futures.add(responsePlainActionFuture);
                    }
                }
            }, "create_users_thread" + i);
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        assertThat(exceptions, Matchers.empty());
        assertEquals(futures.size(), numRequests * numThreads);
        for (ActionFuture<PutUserResponse> future : futures) {
            assertTrue(future.actionGet().created());
        }
    }

    @After
    public void cleanupSecurityIndex() throws Exception {
        super.deleteSecurityIndex();
    }
}
