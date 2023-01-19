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

package org.elasticsearch.check.mapping;

import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.cluster.routing.UnassignedInfo;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.test.ESSingleNodeTestCase;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;

public class CheckMappingIT extends ESSingleNodeTestCase {
    @Override
    protected Collection<Class<? extends Plugin>> getPlugins() {
        return Arrays.asList( CheckMapping.class);
    }

    public void testNoIndexCheckMappingSucc() throws ExecutionException, InterruptedException {
        CheckMappingAction.Request request = new CheckMappingAction.Request();
        request.setSource(
            "{\"type\":{\"numeric_detection\":true,\"properties\":{\"exception\":{\"type\":\"keyword\"},\"logType\":{\"ignore_above\":512,\"type\":\"keyword\"},\"realIP\":{\"type\":\"keyword\"},\"queryKey\":{\"type\":\"keyword\"},\" errorCode \":{\"type\":\"keyword\"},\"queryId\":{\"type\":\"keyword\"},\"duration\":{\"type\":\"long\"},\" page_url LIKE '%UserGroups%')), granularity\":{\"ignore_above\":512,\"type\":\"keyword\"},\"queryStatus\":{\"type\":\"keyword\"},\"sinkTime\":{\"format\":\"yyyy-MM-dd HH:mm:ss Z||yyyy-MM-dd HH:mm:ss||yyyy-MM-dd HH:mm:ss.SSS Z||yyyy-MM-dd HH:mm:ss.SSS||yyyy-MM-dd HH:mm:ss,SSS||yyyy/MM/dd HH:mm:ss||yyyy-MM-dd HH:mm:ss,SSS Z||yyyy/MM/dd HH:mm:ss,SSS Z||epoch_millis\",\"type\":\"date\"},\"brokerHost\":{\"type\":\"keyword\"},\"queryHost\":{\"type\":\"keyword\"},\"remoteAddr\":{\"type\":\"keyword\"},\"timestamp\":{\"format\":\"yyyy-MM-dd HH:mm:ss.SSS Z||yyyy-MM-dd HH:mm:ss.SSS||yyyy-MM-dd HH:mm:ss,SSS||yyyy/MM/dd HH:mm:ss||yyyy-MM-dd HH:mm:ss,SSS Z||yyyy/MM/dd HH:mm:ss,SSS Z||strict_date_optional_time||epoch_millis\",\"type\":\"date\"},\"queryBytes\":{\"type\":\"long\"},\"query\":{\"type\":\"text\"},\"message\":{\"index\":false,\"type\":\"keyword\",\"doc_values\":false},\"queryType\":{\"type\":\"keyword\"}}}}"
        );
        AcknowledgedResponse response = client().execute(CheckMappingAction.INSTANCE, request).get();

        assertTrue(response.isAcknowledged());
    }

    public void testHasIndexCheckMappingSucc() throws ExecutionException, InterruptedException, IOException {

        createIndex("check_mapping_test", 1);

        CheckMappingAction.Request request = new CheckMappingAction.Request();
        request.setIndex("check_mapping_test");
        request.setType("type");
        request.setSource(
            "{\"type\":{\"numeric_detection\":true,\"properties\":{\"exception\":{\"type\":\"keyword\"},\"logType\":{\"ignore_above\":512,\"type\":\"keyword\"},\"realIP\":{\"type\":\"keyword\"},\"queryKey\":{\"type\":\"keyword\"},\" errorCode \":{\"type\":\"keyword\"},\"queryId\":{\"type\":\"keyword\"},\"duration\":{\"type\":\"long\"},\" page_url LIKE '%UserGroups%')), granularity\":{\"ignore_above\":512,\"type\":\"keyword\"},\"queryStatus\":{\"type\":\"keyword\"},\"sinkTime\":{\"format\":\"yyyy-MM-dd HH:mm:ss Z||yyyy-MM-dd HH:mm:ss||yyyy-MM-dd HH:mm:ss.SSS Z||yyyy-MM-dd HH:mm:ss.SSS||yyyy-MM-dd HH:mm:ss,SSS||yyyy/MM/dd HH:mm:ss||yyyy-MM-dd HH:mm:ss,SSS Z||yyyy/MM/dd HH:mm:ss,SSS Z||epoch_millis\",\"type\":\"date\"},\"brokerHost\":{\"type\":\"keyword\"},\"queryHost\":{\"type\":\"keyword\"},\"remoteAddr\":{\"type\":\"keyword\"},\"timestamp\":{\"format\":\"yyyy-MM-dd HH:mm:ss.SSS Z||yyyy-MM-dd HH:mm:ss.SSS||yyyy-MM-dd HH:mm:ss,SSS||yyyy/MM/dd HH:mm:ss||yyyy-MM-dd HH:mm:ss,SSS Z||yyyy/MM/dd HH:mm:ss,SSS Z||strict_date_optional_time||epoch_millis\",\"type\":\"date\"},\"queryBytes\":{\"type\":\"long\"},\"query\":{\"type\":\"text\"},\"message\":{\"index\":false,\"type\":\"keyword\",\"doc_values\":false},\"queryType\":{\"type\":\"keyword\"}}}}"
        );
        AcknowledgedResponse response = client().execute(CheckMappingAction.INSTANCE, request).get();

        assertTrue(response.isAcknowledged());
    }

    public void testCheckMappingFail() {
        try {
            CheckMappingAction.Request request = new CheckMappingAction.Request();
            request.setSource("{\"properties\":{\"name\":{\"type\":\"keyord\"}}}");
            AcknowledgedResponse response = client().execute(CheckMappingAction.INSTANCE, request).get();
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    private void createIndex(String indexName, int numberOfPrimaryShards) throws IOException {
        final String settings = getIndexSettings(numberOfPrimaryShards, 0, Collections.emptyMap());
        assertAcked(client().admin().indices().prepareCreate(indexName).setSource(settings, XContentType.JSON));
        ensureGreen(indexName);
    }

    private String getIndexSettings(
        final int numberOfShards,
        final int numberOfReplicas,
        final Map<String, String> additionalIndexSettings
    ) throws IOException {
        final String settings;
        try (XContentBuilder builder = jsonBuilder()) {
            builder.startObject();
            {
                builder.startObject("settings");
                {
                    builder.field(UnassignedInfo.INDEX_DELAYED_NODE_LEFT_TIMEOUT_SETTING.getKey(), 0);
                    builder.field("index.number_of_shards", numberOfShards);
                    builder.field("index.number_of_replicas", numberOfReplicas);
                    builder.field("index.refresh_interval", "5s");
                    for (final Map.Entry<String, String> additionalSetting : additionalIndexSettings.entrySet()) {
                        builder.field(additionalSetting.getKey(), additionalSetting.getValue());
                    }
                }
                builder.endObject();
                builder.startObject("mappings");
                {
                    builder.startObject("type");
                    {
                        builder.startObject("properties");
                        {
                            builder.startObject("field");
                            {
                                builder.field("type", "long");
                            }
                            builder.endObject();
                        }
                        builder.endObject();
                    }
                    builder.endObject();
                }
                builder.endObject();
            }
            builder.endObject();
            settings = BytesReference.bytes(builder).utf8ToString();
        }
        return settings;
    }
}
