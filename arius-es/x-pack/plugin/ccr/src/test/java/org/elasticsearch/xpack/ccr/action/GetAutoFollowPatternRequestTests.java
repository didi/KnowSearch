/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.ccr.action;

import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.test.AbstractWireSerializingTestCase;
import org.elasticsearch.xpack.core.ccr.action.GetAutoFollowPatternAction;

public class GetAutoFollowPatternRequestTests extends AbstractWireSerializingTestCase<GetAutoFollowPatternAction.Request> {

    @Override
    protected Writeable.Reader<GetAutoFollowPatternAction.Request> instanceReader() {
        return GetAutoFollowPatternAction.Request::new;
    }

    @Override
    protected GetAutoFollowPatternAction.Request createTestInstance() {
        GetAutoFollowPatternAction.Request request = new GetAutoFollowPatternAction.Request();
        if (randomBoolean()) {
            request.setName(randomAlphaOfLength(4));
        }
        return request;
    }
}
