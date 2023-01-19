/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.core.ilm;


import org.apache.lucene.util.SetOnce;
import org.elasticsearch.Version;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.xpack.core.ilm.Step.StepKey;
import org.junit.Before;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.hamcrest.Matchers.equalTo;

public class DeleteStepTests extends AbstractStepTestCase<DeleteStep> {

    private Client client;

    @Before
    public void setup() {
        client = Mockito.mock(Client.class);
    }

    @Override
    public DeleteStep createRandomInstance() {
        StepKey stepKey = randomStepKey();
        StepKey nextStepKey = randomStepKey();

        return new DeleteStep(stepKey, nextStepKey, client);
    }

    @Override
    public DeleteStep mutateInstance(DeleteStep instance) {
        StepKey key = instance.getKey();
        StepKey nextKey = instance.getNextStepKey();

        switch (between(0, 1)) {
        case 0:
            key = new StepKey(key.getPhase(), key.getAction(), key.getName() + randomAlphaOfLength(5));
            break;
        case 1:
            nextKey = new StepKey(key.getPhase(), key.getAction(), key.getName() + randomAlphaOfLength(5));
            break;
        default:
            throw new AssertionError("Illegal randomisation branch");
        }

        return new DeleteStep(key, nextKey, instance.getClient());
    }

    @Override
    public DeleteStep copyInstance(DeleteStep instance) {
        return new DeleteStep(instance.getKey(), instance.getNextStepKey(), instance.getClient());
    }

    public void testIndexSurvives() {
        assertFalse(createRandomInstance().indexSurvives());
    }

    public void testDeleted() {
        IndexMetaData indexMetaData = IndexMetaData.builder(randomAlphaOfLength(10)).settings(settings(Version.CURRENT))
            .numberOfShards(randomIntBetween(1, 5)).numberOfReplicas(randomIntBetween(0, 5)).build();

        AdminClient adminClient = Mockito.mock(AdminClient.class);
        IndicesAdminClient indicesClient = Mockito.mock(IndicesAdminClient.class);

        Mockito.when(client.admin()).thenReturn(adminClient);
        Mockito.when(adminClient.indices()).thenReturn(indicesClient);
        Mockito.doAnswer(invocation -> {
                DeleteIndexRequest request = (DeleteIndexRequest) invocation.getArguments()[0];
                @SuppressWarnings("unchecked")
                ActionListener<AcknowledgedResponse> listener = (ActionListener<AcknowledgedResponse>) invocation.getArguments()[1];
                assertNotNull(request);
                assertEquals(1, request.indices().length);
                assertEquals(indexMetaData.getIndex().getName(), request.indices()[0]);
                listener.onResponse(null);
                return null;
        }).when(indicesClient).delete(Mockito.any(), Mockito.any());

        SetOnce<Boolean> actionCompleted = new SetOnce<>();

        DeleteStep step = createRandomInstance();
        step.performAction(indexMetaData, null, null, new AsyncActionStep.Listener() {
            @Override
            public void onResponse(boolean complete) {
                actionCompleted.set(complete);
            }

            @Override
            public void onFailure(Exception e) {
                throw new AssertionError(e);
            }
        });

        assertThat(actionCompleted.get(), equalTo(true));

        Mockito.verify(client, Mockito.only()).admin();
        Mockito.verify(adminClient, Mockito.only()).indices();
        Mockito.verify(indicesClient, Mockito.only()).delete(Mockito.any(), Mockito.any());
    }

    public void testExceptionThrown() {
        IndexMetaData indexMetaData = IndexMetaData.builder(randomAlphaOfLength(10)).settings(settings(Version.CURRENT))
            .numberOfShards(randomIntBetween(1, 5)).numberOfReplicas(randomIntBetween(0, 5)).build();
        Exception exception = new RuntimeException();

        AdminClient adminClient = Mockito.mock(AdminClient.class);
        IndicesAdminClient indicesClient = Mockito.mock(IndicesAdminClient.class);

        Mockito.when(client.admin()).thenReturn(adminClient);
        Mockito.when(adminClient.indices()).thenReturn(indicesClient);
        Mockito.doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                DeleteIndexRequest request = (DeleteIndexRequest) invocation.getArguments()[0];
                @SuppressWarnings("unchecked")
                ActionListener<AcknowledgedResponse> listener = (ActionListener<AcknowledgedResponse>) invocation.getArguments()[1];
                assertNotNull(request);
                assertEquals(1, request.indices().length);
                assertEquals(indexMetaData.getIndex().getName(), request.indices()[0]);
                listener.onFailure(exception);
                return null;
            }

        }).when(indicesClient).delete(Mockito.any(), Mockito.any());

        SetOnce<Boolean> exceptionThrown = new SetOnce<>();
        DeleteStep step = createRandomInstance();
        step.performAction(indexMetaData, null, null, new AsyncActionStep.Listener() {
            @Override
            public void onResponse(boolean complete) {
                throw new AssertionError("Unexpected method call");
            }

            @Override
            public void onFailure(Exception e) {
                assertEquals(exception, e);
                exceptionThrown.set(true);
            }
        });

        assertThat(exceptionThrown.get(), equalTo(true));
    }
}
