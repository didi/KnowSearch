/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.core.ml.action;

import org.elasticsearch.common.io.stream.NamedWriteableRegistry;
import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.search.SearchModule;
import org.elasticsearch.test.AbstractWireSerializingTestCase;
import org.elasticsearch.xpack.core.ml.action.GetDatafeedsAction.Response;
import org.elasticsearch.xpack.core.action.util.QueryPage;
import org.elasticsearch.xpack.core.ml.datafeed.DatafeedConfig;
import org.elasticsearch.xpack.core.ml.datafeed.DatafeedConfigTests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GetDatafeedsActionResponseTests extends AbstractWireSerializingTestCase<Response> {

    @Override
    protected Response createTestInstance() {
        int listSize = randomInt(10);
        List<DatafeedConfig> datafeedList = new ArrayList<>(listSize);
        for (int j = 0; j < listSize; j++) {
            datafeedList.add(DatafeedConfigTests.createRandomizedDatafeedConfig(randomAlphaOfLength(10)));
        }
        return new Response(new QueryPage<>(datafeedList, datafeedList.size(), DatafeedConfig.RESULTS_FIELD));
    }

    @Override
    protected Writeable.Reader<Response> instanceReader() {
        return Response::new;
    }

    @Override
    protected NamedWriteableRegistry getNamedWriteableRegistry() {
        SearchModule searchModule = new SearchModule(Settings.EMPTY, false, Collections.emptyList());
        return new NamedWriteableRegistry(searchModule.getNamedWriteables());
    }


}
