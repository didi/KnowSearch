/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.ccr.action;

import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.test.AbstractWireSerializingTestCase;
import org.elasticsearch.xpack.core.ccr.AutoFollowMetadata.AutoFollowPattern;
import org.elasticsearch.xpack.core.ccr.action.GetAutoFollowPatternAction;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GetAutoFollowPatternResponseTests extends AbstractWireSerializingTestCase<GetAutoFollowPatternAction.Response> {

    @Override
    protected Writeable.Reader<GetAutoFollowPatternAction.Response> instanceReader() {
        return GetAutoFollowPatternAction.Response::new;
    }

    @Override
    protected GetAutoFollowPatternAction.Response createTestInstance() {
        int numPatterns = randomIntBetween(1, 8);
        Map<String, AutoFollowPattern> patterns = new HashMap<>(numPatterns);
        for (int i = 0; i < numPatterns; i++) {
            AutoFollowPattern autoFollowPattern = new AutoFollowPattern(
                "remote",
                Collections.singletonList(randomAlphaOfLength(4)),
                randomAlphaOfLength(4),
                true, randomIntBetween(0, Integer.MAX_VALUE),
                randomIntBetween(0, Integer.MAX_VALUE),
                randomIntBetween(0, Integer.MAX_VALUE),
                randomIntBetween(0, Integer.MAX_VALUE),
                new ByteSizeValue(randomNonNegativeLong(), ByteSizeUnit.BYTES),
                new ByteSizeValue(randomNonNegativeLong(), ByteSizeUnit.BYTES),
                randomIntBetween(0, Integer.MAX_VALUE),
                new ByteSizeValue(randomNonNegativeLong()),
                TimeValue.timeValueMillis(500),
                TimeValue.timeValueMillis(500));
            patterns.put(randomAlphaOfLength(4), autoFollowPattern);
        }
        return new GetAutoFollowPatternAction.Response(patterns);
    }
}
