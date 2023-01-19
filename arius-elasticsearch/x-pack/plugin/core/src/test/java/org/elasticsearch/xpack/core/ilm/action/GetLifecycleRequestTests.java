/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.core.ilm.action;

import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.test.AbstractWireSerializingTestCase;
import org.elasticsearch.xpack.core.ilm.action.GetLifecycleAction.Request;

import java.util.Arrays;

public class GetLifecycleRequestTests extends AbstractWireSerializingTestCase<Request> {

    @Override
    protected Request createTestInstance() {
        return new Request(randomAlphaOfLengthBetween(1, 20));
    }

    @Override
    protected Writeable.Reader<Request> instanceReader() {
        return Request::new;
    }

    @Override
    protected Request mutateInstance(Request request) {
        String[] originalPolicies = request.getPolicyNames();
        String[] newPolicies = Arrays.copyOf(originalPolicies, originalPolicies.length + 1);
        newPolicies[originalPolicies.length] = randomAlphaOfLength(5);
        return new Request(newPolicies);
    }
}
