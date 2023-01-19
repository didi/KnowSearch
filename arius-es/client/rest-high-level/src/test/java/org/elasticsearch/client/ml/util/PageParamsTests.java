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
package org.elasticsearch.client.ml.util;

import org.elasticsearch.client.core.PageParams;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.test.AbstractXContentTestCase;

public class PageParamsTests extends AbstractXContentTestCase<PageParams> {

    @Override
    protected PageParams doParseInstance(XContentParser parser) {
        return PageParams.PARSER.apply(parser, null);
    }

    @Override
    protected boolean supportsUnknownFields() {
        return false;
    }

    @Override
    protected PageParams createTestInstance() {
        Integer from = randomBoolean() ? randomInt() : null;
        Integer size = randomBoolean() ? randomInt() : null;
        return new PageParams(from, size);
    }
}
