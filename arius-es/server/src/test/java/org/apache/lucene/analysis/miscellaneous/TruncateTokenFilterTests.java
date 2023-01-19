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

package org.apache.lucene.analysis.miscellaneous;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.MockTokenizer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.elasticsearch.test.ESTestCase;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;

public class TruncateTokenFilterTests extends ESTestCase {
    public void testSimple() throws IOException {
        Analyzer analyzer = new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String fieldName) {
                Tokenizer t = new MockTokenizer(MockTokenizer.WHITESPACE, false);
                return new TokenStreamComponents(t, new TruncateTokenFilter(t, 3));
            }
        };

        TokenStream test = analyzer.tokenStream("test", "a bb ccc dddd eeeee");
        test.reset();
        CharTermAttribute termAttribute = test.addAttribute(CharTermAttribute.class);
        assertThat(test.incrementToken(), equalTo(true));
        assertThat(termAttribute.toString(), equalTo("a"));

        assertThat(test.incrementToken(), equalTo(true));
        assertThat(termAttribute.toString(), equalTo("bb"));

        assertThat(test.incrementToken(), equalTo(true));
        assertThat(termAttribute.toString(), equalTo("ccc"));

        assertThat(test.incrementToken(), equalTo(true));
        assertThat(termAttribute.toString(), equalTo("ddd"));

        assertThat(test.incrementToken(), equalTo(true));
        assertThat(termAttribute.toString(), equalTo("eee"));

        assertThat(test.incrementToken(), equalTo(false));
    }
}
