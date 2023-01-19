/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.core.rollup.job;

import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.test.AbstractSerializingTestCase;

import java.io.IOException;

import static org.elasticsearch.xpack.core.rollup.ConfigTestHelpers.randomGroupConfig;

public class GroupConfigSerializingTests extends AbstractSerializingTestCase<GroupConfig> {

    @Override
    protected GroupConfig doParseInstance(final XContentParser parser) throws IOException {
        return GroupConfig.fromXContent(parser);
    }

    @Override
    protected Writeable.Reader<GroupConfig> instanceReader() {
        return GroupConfig::new;
    }

    @Override
    protected GroupConfig createTestInstance() {
        return randomGroupConfig(random());
    }
}
