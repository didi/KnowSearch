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

package org.elasticsearch.script.mustache;

import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.common.Strings;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.test.ESIntegTestCase;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertHitCount;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;

public class MultiSearchTemplateIT extends ESIntegTestCase {

    @Override
    protected Collection<Class<? extends Plugin>> nodePlugins() {
        return Collections.singleton(MustachePlugin.class);
    }

    @Override
    protected Collection<Class<? extends Plugin>> transportClientPlugins() {
        return nodePlugins();
    }

    public void testBasic() throws Exception {
        createIndex("msearch");
        final int numDocs = randomIntBetween(10, 100);
        IndexRequestBuilder[] indexRequestBuilders = new IndexRequestBuilder[numDocs];
        for (int i = 0; i < numDocs; i++) {
            indexRequestBuilders[i] = client().prepareIndex("msearch", "test", String.valueOf(i))
                    .setSource("odd", (i % 2 == 0), "group", (i % 3));
        }
        indexRandom(true, indexRequestBuilders);

        final String template = Strings.toString(jsonBuilder().startObject()
                                                .startObject("query")
                                                    .startObject("{{query_type}}")
                                                        .field("{{field_name}}", "{{field_value}}")
                                                    .endObject()
                                                .endObject()
                                            .endObject());

        MultiSearchTemplateRequest multiRequest = new MultiSearchTemplateRequest();

        // Search #1
        SearchTemplateRequest search1 = new SearchTemplateRequest();
        search1.setRequest(new SearchRequest("msearch"));
        search1.setScriptType(ScriptType.INLINE);
        search1.setScript(template);

        Map<String, Object> params1 = new HashMap<>();
        params1.put("query_type", "match");
        params1.put("field_name", "odd");
        params1.put("field_value", true);
        search1.setScriptParams(params1);
        multiRequest.add(search1);

        // Search #2 (Simulate is true)
        SearchTemplateRequest search2 = new SearchTemplateRequest();
        search2.setRequest(new SearchRequest("msearch"));
        search2.setScriptType(ScriptType.INLINE);
        search2.setScript(template);
        search2.setSimulate(true);

        Map<String, Object> params2 = new HashMap<>();
        params2.put("query_type", "match_phrase_prefix");
        params2.put("field_name", "message");
        params2.put("field_value", "quick brown f");
        search2.setScriptParams(params2);
        multiRequest.add(search2);

        // Search #3
        SearchTemplateRequest search3 = new SearchTemplateRequest();
        search3.setRequest(new SearchRequest("msearch"));
        search3.setScriptType(ScriptType.INLINE);
        search3.setScript(template);
        search3.setSimulate(false);

        Map<String, Object> params3 = new HashMap<>();
        params3.put("query_type", "term");
        params3.put("field_name", "odd");
        params3.put("field_value", "false");
        search3.setScriptParams(params3);
        multiRequest.add(search3);

        // Search #4 (Fail because of unknown index)
        SearchTemplateRequest search4 = new SearchTemplateRequest();
        search4.setRequest(new SearchRequest("unknown"));
        search4.setScriptType(ScriptType.INLINE);
        search4.setScript(template);

        Map<String, Object> params4 = new HashMap<>();
        params4.put("query_type", "match");
        params4.put("field_name", "group");
        params4.put("field_value", "test");
        search4.setScriptParams(params4);
        multiRequest.add(search4);

        // Search #5 (Simulate is true)
        SearchTemplateRequest search5 = new SearchTemplateRequest();
        search5.setRequest(new SearchRequest("msearch"));
        search5.setScriptType(ScriptType.INLINE);
        search5.setScript("{{! ignore me }}{\"query\":{\"terms\":{\"group\":[{{#groups}}{{.}},{{/groups}}]}}}");
        search5.setSimulate(true);

        Map<String, Object> params5 = new HashMap<>();
        params5.put("groups", Arrays.asList(1, 2, 3));
        search5.setScriptParams(params5);
        multiRequest.add(search5);

        MultiSearchTemplateResponse response = client().execute(MultiSearchTemplateAction.INSTANCE, multiRequest).get();
        assertThat(response.getResponses(), arrayWithSize(5));
        assertThat(response.getTook().millis(), greaterThan(0L));

        MultiSearchTemplateResponse.Item response1 = response.getResponses()[0];
        assertThat(response1.isFailure(), is(false));
        SearchTemplateResponse searchTemplateResponse1 = response1.getResponse();
        assertThat(searchTemplateResponse1.hasResponse(), is(true));
        assertHitCount(searchTemplateResponse1.getResponse(), (numDocs / 2) + (numDocs % 2));
        assertThat(searchTemplateResponse1.getSource().utf8ToString(),
                equalTo("{\"query\":{\"match\":{\"odd\":\"true\"}}}"));

        MultiSearchTemplateResponse.Item response2 = response.getResponses()[1];
        assertThat(response2.isFailure(), is(false));
        SearchTemplateResponse searchTemplateResponse2 = response2.getResponse();
        assertThat(searchTemplateResponse2.hasResponse(), is(false));
        assertThat(searchTemplateResponse2.getSource().utf8ToString(),
                equalTo("{\"query\":{\"match_phrase_prefix\":{\"message\":\"quick brown f\"}}}"));

        MultiSearchTemplateResponse.Item response3 = response.getResponses()[2];
        assertThat(response3.isFailure(), is(false));
        SearchTemplateResponse searchTemplateResponse3 = response3.getResponse();
        assertThat(searchTemplateResponse3.hasResponse(), is(true));
        assertHitCount(searchTemplateResponse3.getResponse(), (numDocs / 2));
        assertThat(searchTemplateResponse3.getSource().utf8ToString(),
                equalTo("{\"query\":{\"term\":{\"odd\":\"false\"}}}"));

        MultiSearchTemplateResponse.Item response4 = response.getResponses()[3];
        assertThat(response4.isFailure(), is(true));
        assertThat(response4.getFailure(), instanceOf(IndexNotFoundException.class));
        assertThat(response4.getFailure().getMessage(), equalTo("no such index [unknown]"));

        MultiSearchTemplateResponse.Item response5 = response.getResponses()[4];
        assertThat(response5.isFailure(), is(false));
        SearchTemplateResponse searchTemplateResponse5 = response5.getResponse();
        assertThat(searchTemplateResponse5.hasResponse(), is(false));
        assertThat(searchTemplateResponse5.getSource().utf8ToString(),
                equalTo("{\"query\":{\"terms\":{\"group\":[1,2,3,]}}}"));
    }
}
