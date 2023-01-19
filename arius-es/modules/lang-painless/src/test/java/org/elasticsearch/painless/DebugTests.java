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

package org.elasticsearch.painless;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.io.stream.BytesStreamOutput;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.painless.lookup.PainlessLookup;
import org.elasticsearch.painless.lookup.PainlessLookupBuilder;
import org.elasticsearch.painless.spi.Whitelist;
import org.elasticsearch.script.ScriptException;

import java.io.IOException;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;

public class DebugTests extends ScriptTestCase {
    private final PainlessLookup painlessLookup = PainlessLookupBuilder.buildFromWhitelists(Whitelist.BASE_WHITELISTS);

    public void testExplain() {
        // Debug.explain can explain an object
        Object dummy = new Object();
        PainlessExplainError e = expectScriptThrows(PainlessExplainError.class, () -> exec(
                "Debug.explain(params.a)", singletonMap("a", dummy), true));
        assertSame(dummy, e.getObjectToExplain());
        assertThat(e.getHeaders(painlessLookup), hasEntry("es.to_string", singletonList(dummy.toString())));
        assertThat(e.getHeaders(painlessLookup), hasEntry("es.java_class", singletonList("java.lang.Object")));
        assertThat(e.getHeaders(painlessLookup), hasEntry("es.painless_class", singletonList("java.lang.Object")));

        // Null should be ok
        e = expectScriptThrows(PainlessExplainError.class, () -> exec("Debug.explain(null)"));
        assertNull(e.getObjectToExplain());
        assertThat(e.getHeaders(painlessLookup), hasEntry("es.to_string", singletonList("null")));
        assertThat(e.getHeaders(painlessLookup), not(hasKey("es.java_class")));
        assertThat(e.getHeaders(painlessLookup), not(hasKey("es.painless_class")));

        // You can't catch the explain exception
        e = expectScriptThrows(PainlessExplainError.class, () -> exec(
                "try {\n"
              + "  Debug.explain(params.a)\n"
              + "} catch (Exception e) {\n"
              + "  return 1\n"
              + "}", singletonMap("a", dummy), true));
        assertSame(dummy, e.getObjectToExplain());
    }

    /**
     * {@link PainlessExplainError} doesn't serialize but the headers still make it.
     */
    public void testPainlessExplainErrorSerialization() throws IOException {
        Map<String, Object> params = singletonMap("a", "jumped over the moon");
        ScriptException e = expectThrows(ScriptException.class, () -> exec("Debug.explain(params.a)", params, true));
        assertEquals(singletonList("jumped over the moon"), e.getMetadata("es.to_string"));
        assertEquals(singletonList("java.lang.String"), e.getMetadata("es.java_class"));
        assertEquals(singletonList("java.lang.String"), e.getMetadata("es.painless_class"));

        try (BytesStreamOutput out = new BytesStreamOutput()) {
            out.writeException(e);
            try (StreamInput in = out.bytes().streamInput()) {
                ElasticsearchException read = (ScriptException) in.readException();
                assertEquals(singletonList("jumped over the moon"), read.getMetadata("es.to_string"));
                assertEquals(singletonList("java.lang.String"), read.getMetadata("es.java_class"));
                assertEquals(singletonList("java.lang.String"), read.getMetadata("es.painless_class"));
            }
        }
    }
}
