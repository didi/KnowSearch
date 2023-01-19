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

package org.elasticsearch.index.query;

import org.apache.lucene.search.Query;
import org.elasticsearch.common.ParsingException;
import org.elasticsearch.script.MockScriptEngine;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.test.AbstractQueryTestCase;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;

public class ScriptQueryBuilderTests extends AbstractQueryTestCase<ScriptQueryBuilder> {
    @Override
    protected ScriptQueryBuilder doCreateTestQueryBuilder() {
        String script = "1";
        Map<String, Object> params = Collections.emptyMap();
        return new ScriptQueryBuilder(new Script(ScriptType.INLINE, MockScriptEngine.NAME, script, params));
    }

    @Override
    protected boolean builderGeneratesCacheableQueries() {
        return false;
    }

    @Override
    protected void doAssertLuceneQuery(ScriptQueryBuilder queryBuilder, Query query, QueryShardContext context) throws IOException {
        assertThat(query, instanceOf(ScriptQueryBuilder.ScriptQuery.class));
    }

    public void testIllegalConstructorArg() {
        expectThrows(IllegalArgumentException.class, () -> new ScriptQueryBuilder((Script) null));
    }

    public void testFromJsonVerbose() throws IOException {
        String json =
            "{\n" +
                "  \"script\" : {\n" +
                "    \"script\" : {\n" +
                "      \"source\" : \"5\",\n" +
                "      \"lang\" : \"mockscript\"\n" +
                "    },\n" +
                "    \"boost\" : 1.0,\n" +
                "    \"_name\" : \"PcKdEyPOmR\"\n" +
                "  }\n" +
                "}";

        ScriptQueryBuilder parsed = (ScriptQueryBuilder) parseQuery(json);
        checkGeneratedJson(json, parsed);

        assertEquals(json, "mockscript", parsed.script().getLang());
    }

    public void testFromJson() throws IOException {
        String json =
            "{\n" +
                "  \"script\" : {\n" +
                "    \"script\" : \"5\"," +
                "    \"boost\" : 1.0,\n" +
                "    \"_name\" : \"PcKdEyPOmR\"\n" +
                "  }\n" +
                "}";

        ScriptQueryBuilder parsed = (ScriptQueryBuilder) parseQuery(json);
        assertEquals(json, "5", parsed.script().getIdOrCode());
    }

    public void testArrayOfScriptsException() {
        String json =
            "{\n" +
                "  \"script\" : {\n" +
                "    \"script\" : [ {\n" +
                "      \"source\" : \"5\",\n" +
                "      \"lang\" : \"mockscript\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"source\" : \"6\",\n" +
                "      \"lang\" : \"mockscript\"\n" +
                "    }\n ]" +
                "  }\n" +
                "}";

        ParsingException e = expectThrows(ParsingException.class, () -> parseQuery(json));
        assertThat(e.getMessage(), containsString("does not support an array of scripts"));
    }

    @Override
    protected Set<String> getObjectsHoldingArbitraryContent() {
        //script_score.script.params can contain arbitrary parameters. no error is expected when
        //adding additional objects within the params object.
        return Collections.singleton(Script.PARAMS_PARSE_FIELD.getPreferredName());
    }

    /**
     * Check that this query is generally not cacheable
     */
    @Override
    public void testCacheability() throws IOException {
        ScriptQueryBuilder queryBuilder = createTestQueryBuilder();
        QueryShardContext context = createShardContext();
        QueryBuilder rewriteQuery = rewriteQuery(queryBuilder, new QueryShardContext(context));
        assertNotNull(rewriteQuery.toQuery(context));
        assertFalse("query should not be cacheable: " + queryBuilder.toString(), context.isCacheable());
    }
}
