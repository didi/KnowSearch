/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.client.transform.transforms;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.MatchNoneQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchModule;
import org.elasticsearch.test.AbstractXContentTestCase;

import java.io.IOException;

import static java.util.Collections.emptyList;

public class QueryConfigTests extends AbstractXContentTestCase<QueryConfig> {

    public static QueryConfig randomQueryConfig() {
        QueryBuilder queryBuilder = randomBoolean() ? new MatchAllQueryBuilder() : new MatchNoneQueryBuilder();
        return new QueryConfig(queryBuilder);
    }

    @Override
    protected QueryConfig createTestInstance() {
        return randomQueryConfig();
    }

    @Override
    protected QueryConfig doParseInstance(XContentParser parser) throws IOException {
        return QueryConfig.fromXContent(parser);
    }

    @Override
    protected boolean supportsUnknownFields() {
        return false;
    }

    @Override
    protected NamedXContentRegistry xContentRegistry() {
        SearchModule searchModule = new SearchModule(Settings.EMPTY, false, emptyList());
        return new NamedXContentRegistry(searchModule.getNamedXContents());
    }
}
