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
import org.apache.lucene.document.BinaryDocValuesField;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
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
import org.elasticsearch.index.mapper.KeywordFieldMapper;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.index.mapper.NumberFieldMapper;
import org.elasticsearch.index.mapper.NumberFieldMapper.NumberFieldType;
import org.elasticsearch.index.mapper.NumberFieldMapper.NumberType;
import org.elasticsearch.index.mapper.RangeFieldMapper;
import org.elasticsearch.index.mapper.RangeType;
import org.elasticsearch.index.mapper.TextFieldMapper.TextFieldType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationExecutionException;
import org.elasticsearch.search.aggregations.AggregatorTestCase;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTermsAggregatorFactory.ExecutionMode;
import org.elasticsearch.search.aggregations.bucket.terms.IncludeExclude;
import org.junit.Before;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.elasticsearch.search.aggregations.AggregationBuilders.significantTerms;

public class SignificantTermsAggregatorTests extends AggregatorTestCase {

    private MappedFieldType fieldType;

    @Before
    public void setUpTest() throws Exception {
        super.setUp();
        fieldType = new KeywordFieldMapper.KeywordFieldType();
        fieldType.setHasDocValues(true);
        fieldType.setIndexOptions(IndexOptions.DOCS);
        fieldType.setName("field");
    }

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
     * Uses the significant terms aggregation to find the keywords in text fields
     */
    public void testSignificance() throws IOException {
        TextFieldType textFieldType = new TextFieldType();
        textFieldType.setName("text");
        textFieldType.setFielddata(true);
        textFieldType.setIndexAnalyzer(new NamedAnalyzer("my_analyzer", AnalyzerScope.GLOBAL, new StandardAnalyzer()));

        IndexWriterConfig indexWriterConfig = newIndexWriterConfig();
        indexWriterConfig.setMaxBufferedDocs(100);
        indexWriterConfig.setRAMBufferSizeMB(100); // flush on open to have a single segment

        try (Directory dir = newDirectory(); IndexWriter w = new IndexWriter(dir, indexWriterConfig)) {
            addMixedTextDocs(textFieldType, w);

            SignificantTermsAggregationBuilder sigAgg = new SignificantTermsAggregationBuilder("sig_text", null).field("text");
            sigAgg.executionHint(randomExecutionHint());
            if (randomBoolean()) {
                // Use a background filter which just happens to be same scope as whole-index.
                sigAgg.backgroundFilter(QueryBuilders.termsQuery("text",  "common"));
            }

            SignificantTermsAggregationBuilder sigNumAgg = new SignificantTermsAggregationBuilder("sig_number", null).field("long_field");
            sigNumAgg.executionHint(randomExecutionHint());

            try (IndexReader reader = DirectoryReader.open(w)) {
                assertEquals("test expects a single segment", 1, reader.leaves().size());
                IndexSearcher searcher = new IndexSearcher(reader);

                // Search "odd"
                SignificantTerms terms = searchAndReduce(searcher, new TermQuery(new Term("text", "odd")), sigAgg, textFieldType);

                assertEquals(1, terms.getBuckets().size());
                assertNull(terms.getBucketByKey("even"));
                assertNull(terms.getBucketByKey("common"));
                assertNotNull(terms.getBucketByKey("odd"));

                // Search even
                terms = searchAndReduce(searcher, new TermQuery(new Term("text", "even")), sigAgg, textFieldType);

                assertEquals(1, terms.getBuckets().size());
                assertNull(terms.getBucketByKey("odd"));
                assertNull(terms.getBucketByKey("common"));
                assertNotNull(terms.getBucketByKey("even"));

                // Search odd with regex includeexcludes
                sigAgg.includeExclude(new IncludeExclude("o.d", null));
                terms = searchAndReduce(searcher, new TermQuery(new Term("text", "odd")), sigAgg, textFieldType);
                assertEquals(1, terms.getBuckets().size());
                assertNotNull(terms.getBucketByKey("odd"));
                assertNull(terms.getBucketByKey("common"));
                assertNull(terms.getBucketByKey("even"));

                // Search with string-based includeexcludes
                String oddStrings[] = new String[] {"odd", "weird"};
                String evenStrings[] = new String[] {"even", "regular"};

                sigAgg.includeExclude(new IncludeExclude(oddStrings, evenStrings));
                sigAgg.significanceHeuristic(SignificanceHeuristicTests.getRandomSignificanceheuristic());
                terms = searchAndReduce(searcher, new TermQuery(new Term("text", "odd")), sigAgg, textFieldType);
                assertEquals(1, terms.getBuckets().size());
                assertNotNull(terms.getBucketByKey("odd"));
                assertNull(terms.getBucketByKey("weird"));
                assertNull(terms.getBucketByKey("common"));
                assertNull(terms.getBucketByKey("even"));
                assertNull(terms.getBucketByKey("regular"));

                sigAgg.includeExclude(new IncludeExclude(evenStrings, oddStrings));
                terms = searchAndReduce(searcher, new TermQuery(new Term("text", "odd")), sigAgg, textFieldType);
                assertEquals(0, terms.getBuckets().size());
                assertNull(terms.getBucketByKey("odd"));
                assertNull(terms.getBucketByKey("weird"));
                assertNull(terms.getBucketByKey("common"));
                assertNull(terms.getBucketByKey("even"));
                assertNull(terms.getBucketByKey("regular"));

            }
        }
    }

    /**
     * Uses the significant terms aggregation to find the keywords in numeric
     * fields
     */
    public void testNumericSignificance() throws IOException {
        NumberFieldType longFieldType = new NumberFieldMapper.NumberFieldType(NumberFieldMapper.NumberType.LONG);
        longFieldType.setName("long_field");

        TextFieldType textFieldType = new TextFieldType();
        textFieldType.setName("text");
        textFieldType.setIndexAnalyzer(new NamedAnalyzer("my_analyzer", AnalyzerScope.GLOBAL, new StandardAnalyzer()));

        IndexWriterConfig indexWriterConfig = newIndexWriterConfig();
        indexWriterConfig.setMaxBufferedDocs(100);
        indexWriterConfig.setRAMBufferSizeMB(100); // flush on open to have a single segment
        final long ODD_VALUE = 3;
        final long EVEN_VALUE = 6;
        final long COMMON_VALUE = 2;

        try (Directory dir = newDirectory(); IndexWriter w = new IndexWriter(dir, indexWriterConfig)) {

            for (int i = 0; i < 10; i++) {
                Document doc = new Document();
                if (i % 2 == 0) {
                    addFields(doc, NumberType.LONG.createFields("long_field", ODD_VALUE, true, true, false));
                    doc.add(new Field("text", "odd", textFieldType));
                } else {
                    addFields(doc, NumberType.LONG.createFields("long_field", EVEN_VALUE, true, true, false));
                    doc.add(new Field("text", "even", textFieldType));
                }
                addFields(doc, NumberType.LONG.createFields("long_field", COMMON_VALUE, true, true, false));
                w.addDocument(doc);
            }

            SignificantTermsAggregationBuilder sigNumAgg = new SignificantTermsAggregationBuilder("sig_number", null).field("long_field");
            sigNumAgg.executionHint(randomExecutionHint());

            try (IndexReader reader = DirectoryReader.open(w)) {
                assertEquals("test expects a single segment", 1, reader.leaves().size());
                IndexSearcher searcher = new IndexSearcher(reader);

                // Search "odd"
                SignificantLongTerms terms = searchAndReduce(searcher, new TermQuery(new Term("text", "odd")), sigNumAgg, longFieldType);
                assertEquals(1, terms.getBuckets().size());

                assertNull(terms.getBucketByKey(Long.toString(EVEN_VALUE)));
                assertNull(terms.getBucketByKey(Long.toString(COMMON_VALUE)));
                assertNotNull(terms.getBucketByKey(Long.toString(ODD_VALUE)));

                terms = searchAndReduce(searcher, new TermQuery(new Term("text", "even")), sigNumAgg, longFieldType);
                assertEquals(1, terms.getBuckets().size());

                assertNull(terms.getBucketByKey(Long.toString(ODD_VALUE)));
                assertNull(terms.getBucketByKey(Long.toString(COMMON_VALUE)));
                assertNotNull(terms.getBucketByKey(Long.toString(EVEN_VALUE)));

            }
        }
    }

    /**
     * Uses the significant terms aggregation on an index with unmapped field
     */
    public void testUnmapped() throws IOException {
        TextFieldType textFieldType = new TextFieldType();
        textFieldType.setName("text");
        textFieldType.setFielddata(true);
        textFieldType.setIndexAnalyzer(new NamedAnalyzer("my_analyzer", AnalyzerScope.GLOBAL, new StandardAnalyzer()));

        IndexWriterConfig indexWriterConfig = newIndexWriterConfig();
        indexWriterConfig.setMaxBufferedDocs(100);
        indexWriterConfig.setRAMBufferSizeMB(100); // flush on open to have a single segment
        try (Directory dir = newDirectory(); IndexWriter w = new IndexWriter(dir, indexWriterConfig)) {
            addMixedTextDocs(textFieldType, w);

            // Attempt aggregation on unmapped field
            SignificantTermsAggregationBuilder sigAgg = new SignificantTermsAggregationBuilder("sig_text", null).field("unmapped_field");
            sigAgg.executionHint(randomExecutionHint());

            try (IndexReader reader = DirectoryReader.open(w)) {
                assertEquals("test expects a single segment", 1, reader.leaves().size());
                IndexSearcher searcher = new IndexSearcher(reader);

                // Search "odd"
                SignificantTerms terms = searchAndReduce(searcher, new TermQuery(new Term("text", "odd")), sigAgg, textFieldType);
                assertEquals(0, terms.getBuckets().size());

                assertNull(terms.getBucketByKey("even"));
                assertNull(terms.getBucketByKey("common"));
                assertNull(terms.getBucketByKey("odd"));

            }
        }
    }

    /**
     * Uses the significant terms aggregation on a range field
     */
    public void testRangeField() throws IOException {
        RangeType rangeType = RangeType.DOUBLE;
        final RangeFieldMapper.Range range1 = new RangeFieldMapper.Range(rangeType, 1.0D, 5.0D, true, true);
        final RangeFieldMapper.Range range2 = new RangeFieldMapper.Range(rangeType, 6.0D, 10.0D, true, true);
        final String fieldName = "rangeField";
        MappedFieldType fieldType = new RangeFieldMapper.Builder(fieldName, rangeType).fieldType();
        fieldType.setName(fieldName);

        IndexWriterConfig indexWriterConfig = newIndexWriterConfig();
        indexWriterConfig.setMaxBufferedDocs(100);
        indexWriterConfig.setRAMBufferSizeMB(100); // flush on open to have a single segment
        try (Directory dir = newDirectory(); IndexWriter w = new IndexWriter(dir, indexWriterConfig)) {
            for (RangeFieldMapper.Range range : new RangeFieldMapper.Range[] {
                new RangeFieldMapper.Range(rangeType, 1L, 5L, true, true),
                new RangeFieldMapper.Range(rangeType, -3L, 4L, true, true),
                new RangeFieldMapper.Range(rangeType, 4L, 13L, true, true),
                new RangeFieldMapper.Range(rangeType, 42L, 49L, true, true),
            }) {
                Document doc = new Document();
                BytesRef encodedRange = rangeType.encodeRanges(Collections.singleton(range));
                doc.add(new BinaryDocValuesField("field", encodedRange));
                w.addDocument(doc);
            }

            // Attempt aggregation on range field
            SignificantTermsAggregationBuilder sigAgg = new SignificantTermsAggregationBuilder("sig_text", null).field(fieldName);
            sigAgg.executionHint(randomExecutionHint());

            try (IndexReader reader = DirectoryReader.open(w)) {
                IndexSearcher indexSearcher = newIndexSearcher(reader);
                expectThrows(AggregationExecutionException.class, () -> createAggregator(sigAgg, indexSearcher, fieldType));
            }
        }
    }

    public void testFieldAlias() throws IOException {
        TextFieldType textFieldType = new TextFieldType();
        textFieldType.setName("text");
        textFieldType.setFielddata(true);
        textFieldType.setIndexAnalyzer(new NamedAnalyzer("my_analyzer", AnalyzerScope.GLOBAL, new StandardAnalyzer()));

        IndexWriterConfig indexWriterConfig = newIndexWriterConfig();
        indexWriterConfig.setMaxBufferedDocs(100);
        indexWriterConfig.setRAMBufferSizeMB(100); // flush on open to have a single segment

        try (Directory dir = newDirectory(); IndexWriter w = new IndexWriter(dir, indexWriterConfig)) {
            addMixedTextDocs(textFieldType, w);

            SignificantTermsAggregationBuilder agg = significantTerms("sig_text").field("text");
            SignificantTermsAggregationBuilder aliasAgg = significantTerms("sig_text").field("text-alias");

            String executionHint = randomExecutionHint();
            agg.executionHint(executionHint);
            aliasAgg.executionHint(executionHint);

            if (randomBoolean()) {
                // Use a background filter which just happens to be same scope as whole-index.
                QueryBuilder backgroundFilter = QueryBuilders.termsQuery("text", "common");
                agg.backgroundFilter(backgroundFilter);
                aliasAgg.backgroundFilter(backgroundFilter);
            }

            try (IndexReader reader = DirectoryReader.open(w)) {
                assertEquals("test expects a single segment", 1, reader.leaves().size());
                IndexSearcher searcher = new IndexSearcher(reader);

                SignificantTerms evenTerms = searchAndReduce(searcher, new TermQuery(new Term("text", "even")),
                    agg, textFieldType);
                SignificantTerms aliasEvenTerms = searchAndReduce(searcher, new TermQuery(new Term("text", "even")),
                    aliasAgg, textFieldType);

                assertFalse(evenTerms.getBuckets().isEmpty());
                assertEquals(evenTerms, aliasEvenTerms);

                SignificantTerms oddTerms = searchAndReduce(searcher, new TermQuery(new Term("text", "odd")),
                    agg, textFieldType);
                SignificantTerms aliasOddTerms = searchAndReduce(searcher, new TermQuery(new Term("text", "odd")),
                    aliasAgg, textFieldType);

                assertFalse(oddTerms.getBuckets().isEmpty());
                assertEquals(oddTerms, aliasOddTerms);
            }
        }
    }

    private void addMixedTextDocs(TextFieldType textFieldType, IndexWriter w) throws IOException {
        for (int i = 0; i < 10; i++) {
            Document doc = new Document();
            StringBuilder text = new StringBuilder("common ");
            if (i % 2 == 0) {
                text.append("odd ");
            } else {
                text.append("even ");
            }

            doc.add(new Field("text", text.toString(), textFieldType));
            String json = "{ \"text\" : \"" + text.toString() + "\" }";
            doc.add(new StoredField("_source", new BytesRef(json)));

            w.addDocument(doc);
        }
    }

    private void addFields(Document doc, List<Field> createFields) {
        for (Field field : createFields) {
            doc.add(field);
        }
    }

    public String randomExecutionHint() {
        return randomBoolean() ? null : randomFrom(ExecutionMode.values()).toString();
    }

}
