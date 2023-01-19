/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */

package org.elasticsearch.xpack.core.transform.action;

import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.tasks.Task;
import org.elasticsearch.tasks.TaskId;
import org.elasticsearch.test.AbstractWireSerializingTestCase;
import org.elasticsearch.xpack.core.transform.TransformField;
import org.elasticsearch.xpack.core.transform.action.StopTransformAction.Request;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public class StopTransformActionRequestTests extends AbstractWireSerializingTestCase<Request> {

    @Override
    protected Request createTestInstance() {
        TimeValue timeout = randomBoolean() ? TimeValue.timeValueMinutes(randomIntBetween(1, 10)) : null;
        Request request = new Request(randomAlphaOfLengthBetween(1, 10),
            randomBoolean(),
            randomBoolean(),
            timeout,
            randomBoolean(),
            randomBoolean());
        if (randomBoolean()) {
            request.setExpandedIds(new HashSet<>(Arrays.asList(generateRandomStringArray(5, 6, false))));
        }
        return request;
    }

    @Override
    protected Writeable.Reader<Request> instanceReader() {
        return Request::new;
    }

    public void testSameButDifferentTimeout() {
        String id = randomAlphaOfLengthBetween(1, 10);
        boolean waitForCompletion = randomBoolean();
        boolean force = randomBoolean();
        boolean allowNoMatch = randomBoolean();
        boolean waitForCheckpoint = randomBoolean();

        Request r1 = new Request(id, waitForCompletion, force, TimeValue.timeValueSeconds(10), allowNoMatch, waitForCheckpoint);
        Request r2 = new Request(id, waitForCompletion, force, TimeValue.timeValueSeconds(20), allowNoMatch, waitForCheckpoint);

        assertNotEquals(r1,r2);
        assertNotEquals(r1.hashCode(),r2.hashCode());
    }

    public void testMatch() {
        String dataFrameId = "dataframe-id";

        Task dataFrameTask = new Task(1L, "persistent", "action",
            TransformField.PERSISTENT_TASK_DESCRIPTION_PREFIX + dataFrameId,
            TaskId.EMPTY_TASK_ID, Collections.emptyMap());

        Request request = new Request("unrelated", false, false, null, false, false);
        request.setExpandedIds(new HashSet<>(Arrays.asList("foo", "bar")));
        assertFalse(request.match(dataFrameTask));

        Request matchingRequest = new Request(dataFrameId, false, false, null, false, false);
        matchingRequest.setExpandedIds(Collections.singleton(dataFrameId));
        assertTrue(matchingRequest.match(dataFrameTask));

        Task notADataFrameTask = new Task(1L, "persistent", "action",
            "some other task, say monitoring",
            TaskId.EMPTY_TASK_ID, Collections.emptyMap());
        assertFalse(matchingRequest.match(notADataFrameTask));
    }
}
