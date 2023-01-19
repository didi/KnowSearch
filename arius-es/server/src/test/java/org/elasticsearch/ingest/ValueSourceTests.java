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

package org.elasticsearch.ingest;

import org.elasticsearch.script.ScriptService;
import org.elasticsearch.test.ESTestCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ValueSourceTests extends ESTestCase {

    public void testDeepCopy() {
        int iterations = scaledRandomIntBetween(8, 64);
        for (int i = 0; i < iterations; i++) {
            Map<String, Object> map = RandomDocumentPicks.randomSource(random());
            ValueSource valueSource = ValueSource.wrap(map, TestTemplateService.instance());
            Object copy = valueSource.copyAndResolve(Collections.emptyMap());
            assertThat("iteration: " + i, copy, equalTo(map));
            assertThat("iteration: " + i, copy, not(sameInstance(map)));
        }
    }

    public void testCopyDoesNotChangeProvidedMap() {
        Map<String, Object> myPreciousMap = new HashMap<>();
        myPreciousMap.put("field2", "value2");

        IngestDocument ingestDocument = new IngestDocument(new HashMap<>(), new HashMap<>());
        ingestDocument.setFieldValue(new TestTemplateService.MockTemplateScript.Factory("field1"),
                ValueSource.wrap(myPreciousMap, TestTemplateService.instance()));
        ingestDocument.removeField("field1.field2");

        assertThat(myPreciousMap.size(), equalTo(1));
        assertThat(myPreciousMap.get("field2"), equalTo("value2"));
    }

    public void testCopyDoesNotChangeProvidedList() {
        List<String> myPreciousList = new ArrayList<>();
        myPreciousList.add("value");

        IngestDocument ingestDocument = new IngestDocument(new HashMap<>(), new HashMap<>());
        ingestDocument.setFieldValue(new TestTemplateService.MockTemplateScript.Factory("field1"),
                ValueSource.wrap(myPreciousList, TestTemplateService.instance()));
        ingestDocument.removeField("field1.0");

        assertThat(myPreciousList.size(), equalTo(1));
        assertThat(myPreciousList.get(0), equalTo("value"));
    }

    public void testNoScriptCompilation() {
        ScriptService scriptService = mock(ScriptService.class);
        when(scriptService.isLangSupported(anyString())).thenReturn(true);
        String propertyValue = randomAlphaOfLength(10);
        ValueSource result = ValueSource.wrap(propertyValue, scriptService);
        assertThat(result.copyAndResolve(null), equalTo(propertyValue));
        verify(scriptService, times(0)).compile(any(), any());
    }

    public void testScriptShouldCompile() {
        ScriptService scriptService = mock(ScriptService.class);
        when(scriptService.isLangSupported(anyString())).thenReturn(true);
        String propertyValue = "{{" + randomAlphaOfLength(10) + "}}";
        String compiledValue = randomAlphaOfLength(10);
        when(scriptService.compile(any(), any())).thenReturn(new TestTemplateService.MockTemplateScript.Factory(compiledValue));
        ValueSource result = ValueSource.wrap(propertyValue, scriptService);
        assertThat(result.copyAndResolve(Collections.emptyMap()), equalTo(compiledValue));
        verify(scriptService, times(1)).compile(any(), any());
    }
}
