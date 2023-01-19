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

package org.elasticsearch.discovery;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.Version;
import org.elasticsearch.action.support.PlainActionFuture;
import org.elasticsearch.common.io.stream.NamedWriteableRegistry;
import org.elasticsearch.common.network.NetworkAddress;
import org.elasticsearch.common.network.NetworkService;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.BoundTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.util.CancellableThreads;
import org.elasticsearch.common.util.PageCacheRecycler;
import org.elasticsearch.common.util.concurrent.EsExecutors;
import org.elasticsearch.common.util.concurrent.FutureUtils;
import org.elasticsearch.core.internal.io.IOUtils;
import org.elasticsearch.indices.breaker.NoneCircuitBreakerService;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.threadpool.TestThreadPool;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.Transport;
import org.elasticsearch.transport.TransportService;
import org.elasticsearch.transport.nio.MockNioTransport;
import org.junit.After;
import org.junit.Before;
import org.mockito.Matchers;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class SeedHostsResolverTests extends ESTestCase {

    private List<TransportAddress> transportAddresses;
    private SeedHostsResolver seedHostsResolver;
    private ThreadPool threadPool;
    private ExecutorService executorService;
    // close in reverse order as opened
    private Stack<Closeable> closeables;


    @Before
    public void startResolver() {
        threadPool = new TestThreadPool("node");
        transportAddresses = new ArrayList<>();

        TransportService transportService = mock(TransportService.class);
        when(transportService.getThreadPool()).thenReturn(threadPool);

        seedHostsResolver = new SeedHostsResolver("test_node", Settings.EMPTY, transportService, hostsResolver -> transportAddresses);
        seedHostsResolver.start();

        final ThreadFactory threadFactory = EsExecutors.daemonThreadFactory("[" + getClass().getName() + "]");
        executorService =
            EsExecutors.newScaling(
                getClass().getName() + "/" + getTestName(), 0, 2, 60, TimeUnit.SECONDS, threadFactory, threadPool.getThreadContext());
        closeables = new Stack<>();
    }

    private void recreateSeedHostsResolver(TransportService transportService, Settings settings) {
        if (seedHostsResolver != null) {
            seedHostsResolver.stop();
        }
        seedHostsResolver = new SeedHostsResolver("test_node", settings, transportService, hostsResolver -> transportAddresses);
        seedHostsResolver.start();
    }

    @After
    public void stopResolver() throws IOException {
        seedHostsResolver.stop();
        try {
            logger.info("shutting down...");
            // JDK stack is broken, it does not iterate in the expected order (http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4475301)
            final List<Closeable> reverse = new ArrayList<>();
            while (!closeables.isEmpty()) {
                reverse.add(closeables.pop());
            }
            IOUtils.close(reverse);
        } finally {
            terminate(executorService);
            terminate(threadPool);
        }
    }

    public void testResolvesAddressesInBackgroundAndIgnoresConcurrentCalls() throws Exception {
        final AtomicReference<List<TransportAddress>> resolvedAddressesRef = new AtomicReference<>();
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch endLatch = new CountDownLatch(1);

        final int addressCount = randomIntBetween(0, 5);
        for (int i = 0; i < addressCount; i++) {
            transportAddresses.add(buildNewFakeTransportAddress());
        }

        seedHostsResolver.resolveConfiguredHosts(resolvedAddresses -> {
            try {
                assertTrue(startLatch.await(30, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                throw new AssertionError(e);
            }
            resolvedAddressesRef.set(resolvedAddresses);
            endLatch.countDown();
        });

        seedHostsResolver.resolveConfiguredHosts(resolvedAddresses -> {
            throw new AssertionError("unexpected concurrent resolution");
        });

        assertThat(resolvedAddressesRef.get(), nullValue());
        startLatch.countDown();
        assertTrue(endLatch.await(30, TimeUnit.SECONDS));
        assertThat(resolvedAddressesRef.get(), equalTo(transportAddresses));
    }

    public void testRemovingLocalAddresses() {
        final NetworkService networkService = new NetworkService(Collections.emptyList());
        final InetAddress loopbackAddress = InetAddress.getLoopbackAddress();
        final Transport transport = new MockNioTransport(
            Settings.EMPTY,
            Version.CURRENT,
            threadPool,
            networkService,
            PageCacheRecycler.NON_RECYCLING_INSTANCE,
            new NamedWriteableRegistry(Collections.emptyList()),
            new NoneCircuitBreakerService()) {

            @Override
            public BoundTransportAddress boundAddress() {
                return new BoundTransportAddress(
                    new TransportAddress[]{
                        new TransportAddress(loopbackAddress, 9300),
                        new TransportAddress(loopbackAddress, 9301)
                    },
                    new TransportAddress(loopbackAddress, 9302)
                );
            }
        };
        closeables.push(transport);
        final TransportService transportService =
            new TransportService(Settings.EMPTY, transport, threadPool, TransportService.NOOP_TRANSPORT_INTERCEPTOR, x -> null, null,
                Collections.emptySet());
        closeables.push(transportService);
        final List<TransportAddress> transportAddresses = SeedHostsResolver.resolveHostsLists(
            new CancellableThreads(),
            executorService,
            logger,
            IntStream.range(9300, 9310)
                .mapToObj(port -> NetworkAddress.format(loopbackAddress) + ":" + port)
                .collect(Collectors.toList()),
            transportService,
            TimeValue.timeValueSeconds(30));
        assertThat(transportAddresses, hasSize(7));
        final Set<Integer> ports = new HashSet<>();
        for (final TransportAddress address : transportAddresses) {
            assertTrue(address.address().getAddress().isLoopbackAddress());
            ports.add(address.getPort());
        }
        assertThat(ports, equalTo(IntStream.range(9303, 9310).mapToObj(m -> m).collect(Collectors.toSet())));
    }

    public void testUnknownHost() {
        final Logger logger = mock(Logger.class);
        final NetworkService networkService = new NetworkService(Collections.emptyList());
        final String hostname = randomAlphaOfLength(8);
        final UnknownHostException unknownHostException = new UnknownHostException(hostname);
        final Transport transport = new MockNioTransport(
            Settings.EMPTY,
            Version.CURRENT,
            threadPool,
            networkService,
            PageCacheRecycler.NON_RECYCLING_INSTANCE,
            new NamedWriteableRegistry(Collections.emptyList()),
            new NoneCircuitBreakerService()) {

            @Override
            public BoundTransportAddress boundAddress() {
                return new BoundTransportAddress(
                    new TransportAddress[]{new TransportAddress(InetAddress.getLoopbackAddress(), 9300)},
                    new TransportAddress(InetAddress.getLoopbackAddress(), 9300)
                );
            }

            @Override
            public TransportAddress[] addressesFromString(String address) throws UnknownHostException {
                throw unknownHostException;
            }

        };
        closeables.push(transport);

        final TransportService transportService =
            new TransportService(Settings.EMPTY, transport, threadPool, TransportService.NOOP_TRANSPORT_INTERCEPTOR, x -> null, null,
                Collections.emptySet());
        closeables.push(transportService);

        final List<TransportAddress> transportAddresses = SeedHostsResolver.resolveHostsLists(
            new CancellableThreads(),
            executorService,
            logger,
            Arrays.asList(hostname),
            transportService,
            TimeValue.timeValueSeconds(30)
        );

        assertThat(transportAddresses, empty());
        verify(logger).warn("failed to resolve host [" + hostname + "]", unknownHostException);
    }

    public void testResolveTimeout() {
        final Logger logger = mock(Logger.class);
        final NetworkService networkService = new NetworkService(Collections.emptyList());
        final CountDownLatch latch = new CountDownLatch(1);
        final Transport transport = new MockNioTransport(
            Settings.EMPTY,
            Version.CURRENT,
            threadPool,
            networkService,
            PageCacheRecycler.NON_RECYCLING_INSTANCE,
            new NamedWriteableRegistry(Collections.emptyList()),
            new NoneCircuitBreakerService()) {

            @Override
            public BoundTransportAddress boundAddress() {
                return new BoundTransportAddress(
                    new TransportAddress[]{new TransportAddress(InetAddress.getLoopbackAddress(), 9500)},
                    new TransportAddress(InetAddress.getLoopbackAddress(), 9500)
                );
            }

            @Override
            public TransportAddress[] addressesFromString(String address) throws UnknownHostException {
                if ("hostname1".equals(address)) {
                    return new TransportAddress[]{new TransportAddress(TransportAddress.META_ADDRESS, 9300)};
                } else if ("hostname2".equals(address)) {
                    try {
                        latch.await();
                        return new TransportAddress[]{new TransportAddress(TransportAddress.META_ADDRESS, 9300)};
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    throw new UnknownHostException(address);
                }
            }

        };
        closeables.push(transport);

        final TransportService transportService =
            new TransportService(Settings.EMPTY, transport, threadPool, TransportService.NOOP_TRANSPORT_INTERCEPTOR, x -> null, null,
                Collections.emptySet());
        closeables.push(transportService);
        final TimeValue resolveTimeout = TimeValue.timeValueSeconds(randomIntBetween(3, 5));
        try {
            final List<TransportAddress> transportAddresses = SeedHostsResolver.resolveHostsLists(
                new CancellableThreads(),
                executorService,
                logger,
                Arrays.asList("hostname1", "hostname2"),
                transportService,
                resolveTimeout);

            assertThat(transportAddresses, hasSize(1));
            verify(logger).trace(
                "resolved host [{}] to {}", "hostname1",
                new TransportAddress[]{new TransportAddress(TransportAddress.META_ADDRESS, 9300)});
            verify(logger).warn("timed out after [{}] resolving host [{}]", resolveTimeout, "hostname2");
            verifyNoMoreInteractions(logger);
        } finally {
            latch.countDown();
        }
    }

    public void testCancellationOnClose() throws InterruptedException {
        final NetworkService networkService = new NetworkService(Collections.emptyList());
        final CountDownLatch latch = new CountDownLatch(1);
        final CountDownLatch conditionLatch = new CountDownLatch(1);
        final Transport transport = new MockNioTransport(
            Settings.EMPTY,
            Version.CURRENT,
            threadPool,
            networkService,
            PageCacheRecycler.NON_RECYCLING_INSTANCE,
            new NamedWriteableRegistry(Collections.emptyList()),
            new NoneCircuitBreakerService()) {

            @Override
            public BoundTransportAddress boundAddress() {
                return new BoundTransportAddress(
                    new TransportAddress[]{new TransportAddress(InetAddress.getLoopbackAddress(), 9500)},
                    new TransportAddress(InetAddress.getLoopbackAddress(), 9500)
                );
            }

            @Override
            public TransportAddress[] addressesFromString(String address) throws UnknownHostException {
                if ("hostname1".equals(address)) {
                    return new TransportAddress[]{new TransportAddress(TransportAddress.META_ADDRESS, 9300)};
                } else if ("hostname2".equals(address)) {
                    try {
                        conditionLatch.countDown();
                        latch.await();
                        throw new AssertionError("should never be called");
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    throw new UnknownHostException(address);
                }
            }

        };
        closeables.push(transport);

        final TransportService transportService =
            new TransportService(Settings.EMPTY, transport, threadPool, TransportService.NOOP_TRANSPORT_INTERCEPTOR, x -> null, null,
                Collections.emptySet());
        closeables.push(transportService);
        recreateSeedHostsResolver(transportService,
            Settings.builder().put(SeedHostsResolver.DISCOVERY_SEED_RESOLVER_TIMEOUT_SETTING.getKey(), "10m").build());

        final PlainActionFuture<List<TransportAddress>> fut = new PlainActionFuture<>();
        threadPool.generic().execute((() -> fut.onResponse(seedHostsResolver.resolveHosts(Arrays.asList("hostname1", "hostname2")))));

        conditionLatch.await();
        seedHostsResolver.stop();
        assertThat(FutureUtils.get(fut, 10, TimeUnit.SECONDS), hasSize(0));
    }

    public void testInvalidHosts() throws IllegalAccessException {
        final Logger logger = mock(Logger.class);
        final Transport transport = new MockNioTransport(
            Settings.EMPTY,
            Version.CURRENT,
            threadPool,
            new NetworkService(Collections.emptyList()),
            PageCacheRecycler.NON_RECYCLING_INSTANCE,
            new NamedWriteableRegistry(Collections.emptyList()),
            new NoneCircuitBreakerService()) {
            @Override
            public BoundTransportAddress boundAddress() {
                return new BoundTransportAddress(
                    new TransportAddress[]{new TransportAddress(InetAddress.getLoopbackAddress(), 9300)},
                    new TransportAddress(InetAddress.getLoopbackAddress(), 9300)
                );
            }
        };
        closeables.push(transport);

        final TransportService transportService =
            new TransportService(Settings.EMPTY, transport, threadPool, TransportService.NOOP_TRANSPORT_INTERCEPTOR, x -> null, null,
                Collections.emptySet());
        closeables.push(transportService);
        final List<TransportAddress> transportAddresses = SeedHostsResolver.resolveHostsLists(
            new CancellableThreads(),
            executorService,
            logger,
            Arrays.asList("127.0.0.1:9300:9300", "127.0.0.1:9301"),
            transportService,
            TimeValue.timeValueSeconds(30));
        assertThat(transportAddresses, hasSize(1)); // only one of the two is valid and will be used
        assertThat(transportAddresses.get(0).getAddress(), equalTo("127.0.0.1"));
        assertThat(transportAddresses.get(0).getPort(), equalTo(9301));
        verify(logger).warn(eq("failed to resolve host [127.0.0.1:9300:9300]"), Matchers.any(ExecutionException.class));
    }
}
