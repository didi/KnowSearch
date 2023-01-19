/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */

package org.elasticsearch.xpack.core.ilm;

import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.test.AbstractXContentTestCase;
import org.elasticsearch.test.EqualsHashCodeTestUtils;
import org.elasticsearch.xpack.core.ilm.ShrunkenIndexCheckStep.Info;

import java.io.IOException;

public class ShrunkenIndexCheckStepInfoTests extends AbstractXContentTestCase<ShrunkenIndexCheckStep.Info> {

    @Override
    protected Info createTestInstance() {
        return new Info(randomAlphaOfLengthBetween(10, 20));
    }

    @Override
    protected Info doParseInstance(XContentParser parser) throws IOException {
        return Info.PARSER.apply(parser, null);
    }

    @Override
    protected boolean supportsUnknownFields() {
        return false;
    }

    public final void testEqualsAndHashcode() {
        for (int runs = 0; runs < NUMBER_OF_TEST_RUNS; runs++) {
            EqualsHashCodeTestUtils.checkEqualsAndHashCode(createTestInstance(), this::copyInstance, this::mutateInstance);
        }
    }

    protected final Info copyInstance(Info instance) throws IOException {
        return new Info(instance.getOriginalIndexName());
    }

    protected Info mutateInstance(Info instance) throws IOException {
        return new Info(randomValueOtherThan(instance.getOriginalIndexName(), () -> randomAlphaOfLengthBetween(10, 20)));
    }

}
