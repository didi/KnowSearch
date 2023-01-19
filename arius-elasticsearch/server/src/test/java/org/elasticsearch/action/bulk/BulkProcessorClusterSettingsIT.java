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

package org.elasticsearch.action.bulk;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.test.ESIntegTestCase;
import org.elasticsearch.test.ESIntegTestCase.ClusterScope;
import org.elasticsearch.test.ESIntegTestCase.Scope;

@ClusterScope(scope = Scope.TEST, numDataNodes = 0)
public class BulkProcessorClusterSettingsIT extends ESIntegTestCase {
    public void testBulkProcessorAutoCreateRestrictions() throws Exception {
        // See issue #8125
        Settings settings = Settings.builder().put("action.auto_create_index", false).build();

        internalCluster().startNode(settings);

        createIndex("willwork");
        client().admin().cluster().prepareHealth("willwork").setWaitForGreenStatus().execute().actionGet();

        BulkRequestBuilder bulkRequestBuilder = client().prepareBulk();
        bulkRequestBuilder.add(client().prepareIndex("willwork", "type1", "1").setSource("{\"foo\":1}", XContentType.JSON));
        bulkRequestBuilder.add(client().prepareIndex("wontwork", "type1", "2").setSource("{\"foo\":2}", XContentType.JSON));
        bulkRequestBuilder.add(client().prepareIndex("willwork", "type1", "3").setSource("{\"foo\":3}", XContentType.JSON));
        BulkResponse br = bulkRequestBuilder.get();
        BulkItemResponse[] responses = br.getItems();
        assertEquals(3, responses.length);
        assertFalse("Operation on existing index should succeed", responses[0].isFailed());
        assertTrue("Missing index should have been flagged", responses[1].isFailed());
        assertEquals("[wontwork] IndexNotFoundException[no such index [wontwork]]", responses[1].getFailureMessage());
        assertFalse("Operation on existing index should succeed", responses[2].isFailed());
    }
}
