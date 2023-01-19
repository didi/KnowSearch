package org.elasticsearch.monitor.request;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.mockito.Mockito.mock;

import java.util.PriorityQueue;

import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.transport.Transport;
import org.elasticsearch.transport.TransportResponseHandler;

public class RequestTrackerTests extends ESTestCase {
    public void testTransportHandlers() {
        int slowCount = between(20, 30);
        for (int i = 0; i < slowCount; i++) {
            Transport.ResponseContext context = new Transport.ResponseContext(
                mock(TransportResponseHandler.class), mock(Transport.Connection.class), "action", randomLongBetween(100, 200));
            RequestTracker.getInstance().addTransportHandler(i, context);
        }

        int normalCount = between(10, 20);
        long now = System.currentTimeMillis();
        for (int i = 0; i < normalCount; i++) {
            Transport.ResponseContext context = new Transport.ResponseContext(
                mock(TransportResponseHandler.class), mock(Transport.Connection.class), "action", now - randomLongBetween(100, 200));
            RequestTracker.getInstance().addTransportHandler(slowCount + i, context);
        }

        PriorityQueue<Transport.ResponseContext> priorityQueue = RequestTracker.getInstance().getSlowQueue();
        assertEquals(priorityQueue.size(), slowCount);

        Transport.ResponseContext before = priorityQueue.poll();
        Transport.ResponseContext context = priorityQueue.poll();
        while (context != null) {
            assertThat(context.getCreateTime(), greaterThanOrEqualTo(before.getCreateTime()));
            context = priorityQueue.poll();
            before = context;
        }
    }
}
