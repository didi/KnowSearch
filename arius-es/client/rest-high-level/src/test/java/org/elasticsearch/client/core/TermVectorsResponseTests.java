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

package org.elasticsearch.client.core;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.test.ESTestCase;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;

import static org.elasticsearch.test.AbstractXContentTestCase.xContentTester;

public class TermVectorsResponseTests extends ESTestCase {

    public void testFromXContent() throws IOException {
        xContentTester(
            this::createParser,
            TermVectorsResponseTests::createTestInstance,
            TermVectorsResponseTests::toXContent,
            TermVectorsResponse::fromXContent)
            .supportsUnknownFields(true)
            .randomFieldsExcludeFilter(field ->
                field.endsWith("term_vectors") || field.endsWith("terms") || field.endsWith("tokens"))
            .test();
    }

    static void toXContent(TermVectorsResponse response, XContentBuilder builder) throws IOException {
        builder.startObject();
        builder.field("_index", response.getIndex());
        builder.field("_type", response.getType());
        if (response.getId() != null) {
            builder.field("_id", response.getId());
        }
        builder.field("_version", response.getDocVersion());
        builder.field("found", response.getFound());
        builder.field("took", response.getTookInMillis());
        List<TermVectorsResponse.TermVector> termVectorList = response.getTermVectorsList();
        if (termVectorList != null) {
            Collections.sort(termVectorList, Comparator.comparing(TermVectorsResponse.TermVector::getFieldName));
            builder.startObject("term_vectors");
            for (TermVectorsResponse.TermVector tv : termVectorList) {
                toXContent(tv, builder);
            }
            builder.endObject();
        }
        builder.endObject();
    }

    private static void toXContent(TermVectorsResponse.TermVector tv, XContentBuilder builder) throws IOException {
        builder.startObject(tv.getFieldName());
        // build fields_statistics
        if (tv.getFieldStatistics() != null) {
            builder.startObject("field_statistics");
            builder.field("sum_doc_freq", tv.getFieldStatistics().getSumDocFreq());
            builder.field("doc_count", tv.getFieldStatistics().getDocCount());
            builder.field("sum_ttf", tv.getFieldStatistics().getSumTotalTermFreq());
            builder.endObject();
        }
        // build terms
        List<TermVectorsResponse.TermVector.Term> terms = tv.getTerms();
        if (terms != null) {
            Collections.sort(terms, Comparator.comparing(TermVectorsResponse.TermVector.Term::getTerm));
            builder.startObject("terms");
            for (TermVectorsResponse.TermVector.Term term : terms) {
                builder.startObject(term.getTerm());
                // build term_statistics
                if (term.getDocFreq() != null) builder.field("doc_freq", term.getDocFreq());
                if (term.getTotalTermFreq() != null) builder.field("ttf", term.getTotalTermFreq());
                builder.field("term_freq", term.getTermFreq());

                // build tokens
                List<TermVectorsResponse.TermVector.Token> tokens = term.getTokens();
                if (tokens != null) {
                    Collections.sort(
                        tokens,
                        Comparator.comparing(TermVectorsResponse.TermVector.Token::getPosition, Comparator.nullsFirst(Integer::compareTo))
                            .thenComparing(TermVectorsResponse.TermVector.Token::getStartOffset, Comparator.nullsFirst(Integer::compareTo))
                            .thenComparing(TermVectorsResponse.TermVector.Token::getEndOffset, Comparator.nullsFirst(Integer::compareTo))
                    );
                    builder.startArray("tokens");
                    for (TermVectorsResponse.TermVector.Token token : tokens) {
                        builder.startObject();
                        if (token.getPosition() != null) builder.field("position", token.getPosition());
                        if (token.getStartOffset()!= null) builder.field("start_offset", token.getStartOffset());
                        if (token.getEndOffset() != null) builder.field("end_offset", token.getEndOffset());
                        if (token.getPayload() != null) builder.field("payload", token.getPayload());
                        builder.endObject();
                    }
                    builder.endArray();
                }
                if (term.getScore() != null) builder.field("score", term.getScore());
                builder.endObject();
            }
            builder.endObject();
        }
        builder.endObject();
    }


    static TermVectorsResponse createTestInstance() {
        String index = randomAlphaOfLength(5);
        String type = randomAlphaOfLength(5);
        String id = String.valueOf(randomIntBetween(1,100));
        long version = randomNonNegativeLong();
        long tookInMillis = randomNonNegativeLong();
        boolean found = randomBoolean();
        List<TermVectorsResponse.TermVector> tvList = null;
        if (found == true){
            boolean hasFieldStatistics = randomBoolean();
            boolean hasTermStatistics = randomBoolean();
            boolean hasScores = randomBoolean();
            boolean hasOffsets = randomBoolean();
            boolean hasPositions = randomBoolean();
            boolean hasPayloads = randomBoolean();
            int fieldsCount = randomIntBetween(1, 3);
            tvList = new ArrayList<>(fieldsCount);
            List<String> usedFieldNames = new ArrayList<>(fieldsCount);
            for (int i = 0; i < fieldsCount; i++) {
                String fieldName = randomValueOtherThanMany(usedFieldNames::contains, () -> randomAlphaOfLength(7));
                usedFieldNames.add(fieldName);
                tvList.add(randomTermVector(
                    fieldName, hasFieldStatistics, hasTermStatistics, hasScores, hasOffsets, hasPositions, hasPayloads));
            }
        }
        TermVectorsResponse tvresponse = new TermVectorsResponse(index, type, id, version, found, tookInMillis, tvList);
        return tvresponse;
    }



    private static TermVectorsResponse.TermVector randomTermVector(String fieldName, boolean hasFieldStatistics, boolean hasTermStatistics,
            boolean hasScores, boolean hasOffsets, boolean hasPositions, boolean hasPayloads) {
        TermVectorsResponse.TermVector.FieldStatistics fs = null;
        if (hasFieldStatistics) {
            long sumDocFreq = randomNonNegativeLong();
            int docCount = randomInt(1000);
            long sumTotalTermFreq = randomNonNegativeLong();
            fs = new TermVectorsResponse.TermVector.FieldStatistics(sumDocFreq, docCount, sumTotalTermFreq);
        }

        int termsCount = randomIntBetween(1, 5);
        List<TermVectorsResponse.TermVector.Term> terms = new ArrayList<>(termsCount);
        List<String> usedTerms = new ArrayList<>(termsCount);
        for (int i = 0; i < termsCount; i++) {
            String termTxt = randomValueOtherThanMany(usedTerms::contains, () -> randomAlphaOfLength(7));
            usedTerms.add(termTxt);
            terms.add(randomTerm(termTxt, hasTermStatistics, hasScores, hasOffsets, hasPositions, hasPayloads));
        }

        TermVectorsResponse.TermVector tv = new TermVectorsResponse.TermVector(fieldName, fs, terms);
        return tv;
    }

    private static TermVectorsResponse.TermVector.Term randomTerm(String termTxt, boolean hasTermStatistics, boolean hasScores,
            boolean hasOffsets, boolean hasPositions, boolean hasPayloads) {

        int termFreq =  randomInt(10000);
        Integer docFreq = null;
        Long totalTermFreq = null;
        Float score = null;
        List<TermVectorsResponse.TermVector.Token> tokens = null;
        if (hasTermStatistics) {
            docFreq = randomInt(1000);
            totalTermFreq = randomNonNegativeLong();
        }
        if (hasScores) score = randomFloat();
        if (hasOffsets || hasPositions || hasPayloads ){
            int tokensCount = randomIntBetween(1, 5);
            tokens = new ArrayList<>(tokensCount);
            for (int i = 0; i < tokensCount; i++) {
                Integer startOffset = null;
                Integer endOffset = null;
                Integer position = null;
                String payload = null;
                if (hasOffsets) {
                    startOffset = randomInt(1000);
                    endOffset = randomInt(2000);
                }
                if (hasPositions) position = randomInt(100);
                if (hasPayloads) payload = "payload" + randomAlphaOfLength(2);
                TermVectorsResponse.TermVector.Token token =
                    new TermVectorsResponse.TermVector.Token(startOffset, endOffset, position, payload);
                tokens.add(token);
            }
        }
        TermVectorsResponse.TermVector.Term term =
            new TermVectorsResponse.TermVector.Term(termTxt, termFreq, docFreq, totalTermFreq, score, tokens);
        return term;
    }
}
