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

package org.elasticsearch.search.aggregations.bucket.significant;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BytesRef;
import org.elasticsearch.index.analysis.AnalyzerScope;
import org.elasticsearch.index.analysis.NamedAnalyzer;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.index.mapper.TextFieldMapper.TextFieldType;
import org.elasticsearch.search.aggregations.AggregatorTestCase;
import org.elasticsearch.search.aggregations.bucket.sampler.InternalSampler;
import org.elasticsearch.search.aggregations.bucket.sampler.SamplerAggregationBuilder;
import org.elasticsearch.search.aggregations.support.AggregationInspectionHelper;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.elasticsearch.search.aggregations.AggregationBuilders.sampler;
import static org.elasticsearch.search.aggregations.AggregationBuilders.significantText;

public class SignificantTextAggregatorTests extends AggregatorTestCase {

    /**
     * For each provided field type, we also register an alias with name {@code <field>-alias}.
     */
    @Override
    protected Map<String, MappedFieldType> getFieldAliases(MappedFieldType... fieldTypes) {
        return Arrays.stream(fieldTypes).collect(Collectors.toMap(
            ft -> ft.name() + "-alias",
            Function.identity()));
    }

    /**
     * Uses the significant text aggregation to find the keywords in text fields
     */
    public void testSignificance() throws IOException {
        TextFieldType textFieldType = new TextFieldType();
        textFieldType.setName("text");
        textFieldType.setIndexAnalyzer(new NamedAnalyzer("my_analyzer", AnalyzerScope.GLOBAL, new StandardAnalyzer()));

        IndexWriterConfig indexWriterConfig = newIndexWriterConfig();
        indexWriterConfig.setMaxBufferedDocs(100);
        indexWriterConfig.setRAMBufferSizeMB(100); // flush on open to have a single segment
        try (Directory dir = newDirectory(); IndexWriter w = new IndexWriter(dir, indexWriterConfig)) {
            indexDocuments(w, textFieldType);

            SignificantTextAggregationBuilder sigAgg = new SignificantTextAggregationBuilder("sig_text", "text").filterDuplicateText(true);
            if(randomBoolean()){
                sigAgg.sourceFieldNames(Arrays.asList(new String [] {"json_only_field"}));
            }
            SamplerAggregationBuilder aggBuilder = new SamplerAggregationBuilder("sampler")
                    .subAggregation(sigAgg);

            try (IndexReader reader = DirectoryReader.open(w)) {
                assertEquals("test expects a single segment", 1, reader.leaves().size());
                IndexSearcher searcher = new IndexSearcher(reader);

                // Search "odd" which should have no duplication
                InternalSampler sampler = searchAndReduce(searcher, new TermQuery(new Term("text", "odd")), aggBuilder, textFieldType);
                SignificantTerms terms = sampler.getAggregations().get("sig_text");

                assertNull(terms.getBucketByKey("even"));
                assertNull(terms.getBucketByKey("duplicate"));
                assertNull(terms.getBucketByKey("common"));
                assertNotNull(terms.getBucketByKey("odd"));

                // Search "even" which will have duplication
                sampler = searchAndReduce(searcher, new TermQuery(new Term("text", "even")), aggBuilder, textFieldType);
                terms = sampler.getAggregations().get("sig_text");

                assertNull(terms.getBucketByKey("odd"));
                assertNull(terms.getBucketByKey("duplicate"));
                assertNull(terms.getBucketByKey("common"));
                assertNull(terms.getBucketByKey("separator2"));
                assertNull(terms.getBucketByKey("separator4"));
                assertNull(terms.getBucketByKey("separator6"));

                assertNotNull(terms.getBucketByKey("even"));

                assertTrue(AggregationInspectionHelper.hasValue(sampler));
            }
        }
    }

    public void testFieldAlias() throws IOException {
        TextFieldType textFieldType = new TextFieldType();
        textFieldType.setName("text");
        textFieldType.setIndexAnalyzer(new NamedAnalyzer("my_analyzer", AnalyzerScope.GLOBAL, new StandardAnalyzer()));

        IndexWriterConfig indexWriterConfig = newIndexWriterConfig();
        indexWriterConfig.setMaxBufferedDocs(100);
        indexWriterConfig.setRAMBufferSizeMB(100); // flush on open to have a single segment
        try (Directory dir = newDirectory(); IndexWriter w = new IndexWriter(dir, indexWriterConfig)) {
            indexDocuments(w, textFieldType);

            SignificantTextAggregationBuilder agg = significantText("sig_text", "text")
                .filterDuplicateText(true);
            SignificantTextAggregationBuilder aliasAgg = significantText("sig_text", "text-alias")
                .filterDuplicateText(true);

            if (randomBoolean()) {
                List<String> sourceFieldNames = Arrays.asList(new String [] {"json_only_field"});
                agg.sourceFieldNames(sourceFieldNames);
                aliasAgg.sourceFieldNames(sourceFieldNames);
            }

            try (IndexReader reader = DirectoryReader.open(w)) {
                assertEquals("test expects a single segment", 1, reader.leaves().size());
                IndexSearcher searcher = new IndexSearcher(reader);

                SamplerAggregationBuilder samplerAgg = sampler("sampler").subAggregation(agg);
                SamplerAggregationBuilder aliasSamplerAgg = sampler("sampler").subAggregation(aliasAgg);

                InternalSampler sampler = searchAndReduce(searcher, new TermQuery(new Term("text", "odd")), samplerAgg, textFieldType);
                InternalSampler aliasSampler = searchAndReduce(searcher, new TermQuery(new Term("text", "odd")),
                    aliasSamplerAgg, textFieldType);

                SignificantTerms terms = sampler.getAggregations().get("sig_text");
                SignificantTerms aliasTerms = aliasSampler.getAggregations().get("sig_text");
                assertFalse(terms.getBuckets().isEmpty());
                assertEquals(terms, aliasTerms);

                sampler = searchAndReduce(searcher, new TermQuery(new Term("text", "even")), samplerAgg, textFieldType);
                aliasSampler = searchAndReduce(searcher, new TermQuery(new Term("text", "even")), aliasSamplerAgg, textFieldType);

                terms = sampler.getAggregations().get("sig_text");
                aliasTerms = aliasSampler.getAggregations().get("sig_text");
                assertFalse(terms.getBuckets().isEmpty());
                assertEquals(terms, aliasTerms);

                assertTrue(AggregationInspectionHelper.hasValue(sampler));
                assertTrue(AggregationInspectionHelper.hasValue(aliasSampler));
            }
        }
    }

    private void indexDocuments(IndexWriter writer, TextFieldType textFieldType) throws IOException {
        for (int i = 0; i < 10; i++) {
            Document doc = new Document();
            StringBuilder text = new StringBuilder("common ");
            if (i % 2 == 0) {
                text.append("odd ");
            } else {
                text.append("even separator" + i + " duplicate duplicate duplicate duplicate duplicate duplicate ");
            }

            doc.add(new Field("text", text.toString(), textFieldType));
            String json ="{ \"text\" : \"" + text.toString() + "\","+
                " \"json_only_field\" : \"" + text.toString() + "\"" +
                " }";
            doc.add(new StoredField("_source", new BytesRef(json)));
            writer.addDocument(doc);
        }
    }

    /**
     * Test documents with arrays of text
     */
    public void testSignificanceOnTextArrays() throws IOException {
        TextFieldType textFieldType = new TextFieldType();
        textFieldType.setName("text");
        textFieldType.setIndexAnalyzer(new NamedAnalyzer("my_analyzer", AnalyzerScope.GLOBAL, new StandardAnalyzer()));

        IndexWriterConfig indexWriterConfig = newIndexWriterConfig();
        indexWriterConfig.setMaxBufferedDocs(100);
        indexWriterConfig.setRAMBufferSizeMB(100); // flush on open to have a single segment
        try (Directory dir = newDirectory(); IndexWriter w = new IndexWriter(dir, indexWriterConfig)) {
            for (int i = 0; i < 10; i++) {
                Document doc = new Document();
                doc.add(new Field("text", "foo", textFieldType));
                String json ="{ \"text\" : [\"foo\",\"foo\"], \"title\" : [\"foo\", \"foo\"]}";
                doc.add(new StoredField("_source", new BytesRef(json)));
                w.addDocument(doc);
            }

            SignificantTextAggregationBuilder sigAgg = new SignificantTextAggregationBuilder("sig_text", "text");
            sigAgg.sourceFieldNames(Arrays.asList(new String [] {"title", "text"}));
            try (IndexReader reader = DirectoryReader.open(w)) {
                assertEquals("test expects a single segment", 1, reader.leaves().size());
                IndexSearcher searcher = new IndexSearcher(reader);
                searchAndReduce(searcher, new TermQuery(new Term("text", "foo")), sigAgg, textFieldType);
                // No significant results to be found in this test - only checking we don't end up
                // with the internal exception discovered in issue https://github.com/elastic/elasticsearch/issues/25029
            }
        }
    }


}
