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

package org.elasticsearch.ingest.common;

import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.IngestDocument.MetaData;
import org.elasticsearch.ingest.Processor;
import org.elasticsearch.ingest.RandomDocumentPicks;
import org.elasticsearch.ingest.TestTemplateService;
import org.elasticsearch.ingest.ValueSource;
import org.elasticsearch.test.ESTestCase;
import org.hamcrest.Matchers;

import java.util.HashMap;

import static org.hamcrest.Matchers.equalTo;

public class SetProcessorTests extends ESTestCase {

    public void testSetExistingFields() throws Exception {
        IngestDocument ingestDocument = RandomDocumentPicks.randomIngestDocument(random());
        String fieldName = RandomDocumentPicks.randomExistingFieldName(random(), ingestDocument);
        Object fieldValue = RandomDocumentPicks.randomFieldValue(random());
        Processor processor = createSetProcessor(fieldName, fieldValue, true);
        processor.execute(ingestDocument);
        assertThat(ingestDocument.hasField(fieldName), equalTo(true));
        assertThat(ingestDocument.getFieldValue(fieldName, Object.class), equalTo(fieldValue));
    }

    public void testSetNewFields() throws Exception {
        IngestDocument ingestDocument = RandomDocumentPicks.randomIngestDocument(random(), new HashMap<>());
        //used to verify that there are no conflicts between subsequent fields going to be added
        IngestDocument testIngestDocument = RandomDocumentPicks.randomIngestDocument(random(), new HashMap<>());
        Object fieldValue = RandomDocumentPicks.randomFieldValue(random());
        String fieldName = RandomDocumentPicks.addRandomField(random(), testIngestDocument, fieldValue);
        Processor processor = createSetProcessor(fieldName, fieldValue, true);
        processor.execute(ingestDocument);
        assertThat(ingestDocument.hasField(fieldName), equalTo(true));
        assertThat(ingestDocument.getFieldValue(fieldName, Object.class), equalTo(fieldValue));
    }

    public void testSetFieldsTypeMismatch() throws Exception {
        IngestDocument ingestDocument = RandomDocumentPicks.randomIngestDocument(random(), new HashMap<>());
        ingestDocument.setFieldValue("field", "value");
        Processor processor = createSetProcessor("field.inner", "value", true);
        try {
            processor.execute(ingestDocument);
            fail("processor execute should have failed");
        } catch(IllegalArgumentException e) {
            assertThat(e.getMessage(), equalTo("cannot set [inner] with parent object of type [java.lang.String] as " +
                    "part of path [field.inner]"));
        }
    }

    public void testSetNewFieldWithOverrideDisabled() throws Exception {
        IngestDocument ingestDocument = new IngestDocument(new HashMap<>(), new HashMap<>());
        String fieldName = RandomDocumentPicks.randomFieldName(random());
        Object fieldValue = RandomDocumentPicks.randomFieldValue(random());
        Processor processor = createSetProcessor(fieldName, fieldValue, false);
        processor.execute(ingestDocument);
        assertThat(ingestDocument.hasField(fieldName), equalTo(true));
        assertThat(ingestDocument.getFieldValue(fieldName, Object.class), equalTo(fieldValue));
    }

    public void testSetExistingFieldWithOverrideDisabled() throws Exception {
        IngestDocument ingestDocument = new IngestDocument(new HashMap<>(), new HashMap<>());
        Object fieldValue = "foo";
        String fieldName = RandomDocumentPicks.addRandomField(random(), ingestDocument, fieldValue);
        Processor processor = createSetProcessor(fieldName, "bar", false);
        processor.execute(ingestDocument);
        assertThat(ingestDocument.hasField(fieldName), equalTo(true));
        assertThat(ingestDocument.getFieldValue(fieldName, Object.class), equalTo(fieldValue));
    }

    public void testSetExistingNullFieldWithOverrideDisabled() throws Exception {
        IngestDocument ingestDocument = new IngestDocument(new HashMap<>(), new HashMap<>());
        Object fieldValue = null;
        Object newValue = "bar";
        String fieldName = RandomDocumentPicks.addRandomField(random(), ingestDocument, fieldValue);
        Processor processor = createSetProcessor(fieldName, newValue, false);
        processor.execute(ingestDocument);
        assertThat(ingestDocument.hasField(fieldName), equalTo(true));
        assertThat(ingestDocument.getFieldValue(fieldName, Object.class), equalTo(newValue));
    }

    public void testSetMetadataExceptVersion() throws Exception {
        MetaData randomMetaData = randomFrom(MetaData.INDEX, MetaData.TYPE, MetaData.ID, MetaData.ROUTING);
        Processor processor = createSetProcessor(randomMetaData.getFieldName(), "_value", true);
        IngestDocument ingestDocument = RandomDocumentPicks.randomIngestDocument(random());
        processor.execute(ingestDocument);
        assertThat(ingestDocument.getFieldValue(randomMetaData.getFieldName(), String.class), Matchers.equalTo("_value"));
    }

    public void testSetMetadataVersion() throws Exception {
        long version = randomNonNegativeLong();
        Processor processor = createSetProcessor(MetaData.VERSION.getFieldName(), version, true);
        IngestDocument ingestDocument = RandomDocumentPicks.randomIngestDocument(random());
        processor.execute(ingestDocument);
        assertThat(ingestDocument.getFieldValue(MetaData.VERSION.getFieldName(), Long.class), Matchers.equalTo(version));
    }

    public void testSetMetadataVersionType() throws Exception {
        String versionType = randomFrom("internal", "external", "external_gte");
        Processor processor = createSetProcessor(MetaData.VERSION_TYPE.getFieldName(), versionType, true);
        IngestDocument ingestDocument = RandomDocumentPicks.randomIngestDocument(random());
        processor.execute(ingestDocument);
        assertThat(ingestDocument.getFieldValue(MetaData.VERSION_TYPE.getFieldName(), String.class), Matchers.equalTo(versionType));
    }

    private static Processor createSetProcessor(String fieldName, Object fieldValue, boolean overrideEnabled) {
        return new SetProcessor(randomAlphaOfLength(10), new TestTemplateService.MockTemplateScript.Factory(fieldName),
                ValueSource.wrap(fieldValue, TestTemplateService.instance()), overrideEnabled);
    }
}
