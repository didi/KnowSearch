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
package org.elasticsearch.analysis.common;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.elasticsearch.Version;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.TestEnvironment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AnalysisTestsHelper;
import org.elasticsearch.index.analysis.IndexAnalyzers;
import org.elasticsearch.index.analysis.NamedAnalyzer;
import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.elasticsearch.indices.analysis.AnalysisModule;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.test.IndexSettingsModule;
import org.elasticsearch.test.VersionUtils;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;

public class WordDelimiterGraphTokenFilterFactoryTests
        extends BaseWordDelimiterTokenFilterFactoryTestCase {
    public WordDelimiterGraphTokenFilterFactoryTests() {
        super("word_delimiter_graph");
    }

    public void testMultiTerms() throws IOException {
        ESTestCase.TestAnalysis analysis = AnalysisTestsHelper.createTestAnalysisFromSettings(
                Settings.builder()
                    .put(Environment.PATH_HOME_SETTING.getKey(), createTempDir().toString())
                    .put("index.analysis.filter.my_word_delimiter.type", type)
                    .put("index.analysis.filter.my_word_delimiter.catenate_all", "true")
                    .put("index.analysis.filter.my_word_delimiter.preserve_original", "true")
                    .build(),
                new CommonAnalysisPlugin());

        TokenFilterFactory tokenFilter = analysis.tokenFilter.get("my_word_delimiter");
        String source = "PowerShot 500-42 wi-fi wi-fi-4000 j2se O'Neil's";
        String[] expected = new String[] { "PowerShot", "PowerShot", "Power", "Shot", "500-42",
                "50042", "500", "42", "wi-fi", "wifi", "wi", "fi", "wi-fi-4000", "wifi4000", "wi",
                "fi", "4000", "j2se", "j2se", "j", "2", "se", "O'Neil's", "ONeil", "O", "Neil" };
        Tokenizer tokenizer = new WhitespaceTokenizer();
        tokenizer.setReader(new StringReader(source));
        int[] expectedIncr = new int[] { 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 1, 0, 0,
                1, 1, 1, 0, 0, 1 };
        int[] expectedPosLen = new int[] { 2, 2, 1, 1, 2, 2, 1, 1, 2, 2, 1, 1, 3, 3, 1, 1, 1, 3, 3,
                1, 1, 1, 2, 2, 1, 1 };
        assertTokenStreamContents(tokenFilter.create(tokenizer), expected, null, null, null,
                expectedIncr, expectedPosLen, null);
    }

    /**
     * Correct offset order when doing both parts and concatenation: PowerShot is a synonym of Power
     */
    public void testPartsAndCatenate() throws IOException {
        ESTestCase.TestAnalysis analysis = AnalysisTestsHelper.createTestAnalysisFromSettings(
                Settings.builder()
                    .put(Environment.PATH_HOME_SETTING.getKey(), createTempDir().toString())
                    .put("index.analysis.filter.my_word_delimiter.type", type)
                    .put("index.analysis.filter.my_word_delimiter.catenate_words", "true")
                    .put("index.analysis.filter.my_word_delimiter.generate_word_parts", "true")
                    .build(),
                new CommonAnalysisPlugin());
        TokenFilterFactory tokenFilter = analysis.tokenFilter.get("my_word_delimiter");
        String source = "PowerShot";
        int[] expectedIncr = new int[]{1, 0, 1};
        int[] expectedPosLen = new int[]{2, 1, 1};
        int[] expectedStartOffsets = new int[]{0, 0, 5};
        int[] expectedEndOffsets = new int[]{9, 5, 9};
        String[] expected = new String[]{"PowerShot", "Power", "Shot" };
        Tokenizer tokenizer = new WhitespaceTokenizer();
        tokenizer.setReader(new StringReader(source));
        assertTokenStreamContents(tokenFilter.create(tokenizer), expected, expectedStartOffsets, expectedEndOffsets, null,
            expectedIncr, expectedPosLen, null);
    }

    public void testAdjustingOffsets() throws IOException {
        ESTestCase.TestAnalysis analysis = AnalysisTestsHelper.createTestAnalysisFromSettings(
            Settings.builder()
                .put(Environment.PATH_HOME_SETTING.getKey(), createTempDir().toString())
                .put("index.analysis.filter.my_word_delimiter.type", type)
                .put("index.analysis.filter.my_word_delimiter.catenate_words", "true")
                .put("index.analysis.filter.my_word_delimiter.generate_word_parts", "true")
                .put("index.analysis.filter.my_word_delimiter.adjust_offsets", "false")
                .build(),
            new CommonAnalysisPlugin());
        TokenFilterFactory tokenFilter = analysis.tokenFilter.get("my_word_delimiter");
        String source = "PowerShot";
        int[] expectedIncr = new int[]{1, 0, 1};
        int[] expectedPosLen = new int[]{2, 1, 1};
        int[] expectedStartOffsets = new int[]{0, 0, 0};
        int[] expectedEndOffsets = new int[]{9, 9, 9};
        String[] expected = new String[]{"PowerShot", "Power", "Shot" };
        Tokenizer tokenizer = new WhitespaceTokenizer();
        tokenizer.setReader(new StringReader(source));
        assertTokenStreamContents(tokenFilter.create(tokenizer), expected, expectedStartOffsets, expectedEndOffsets, null,
            expectedIncr, expectedPosLen, null);
    }

    public void testPreconfiguredFilter() throws IOException {
        // Before 7.3 we don't adjust offsets
        {
            Settings settings = Settings.builder()
                .put(Environment.PATH_HOME_SETTING.getKey(), createTempDir().toString())
                .build();
            Settings indexSettings = Settings.builder()
                .put(IndexMetaData.SETTING_VERSION_CREATED,
                    VersionUtils.randomVersionBetween(random(), Version.V_7_0_0, VersionUtils.getPreviousVersion(Version.V_7_3_0)))
                .put("index.analysis.analyzer.my_analyzer.tokenizer", "standard")
                .putList("index.analysis.analyzer.my_analyzer.filter", "word_delimiter_graph")
                .build();
            IndexSettings idxSettings = IndexSettingsModule.newIndexSettings("index", indexSettings);

            try (IndexAnalyzers indexAnalyzers = new AnalysisModule(TestEnvironment.newEnvironment(settings),
                Collections.singletonList(new CommonAnalysisPlugin())).getAnalysisRegistry().build(idxSettings)) {

                NamedAnalyzer analyzer = indexAnalyzers.get("my_analyzer");
                assertNotNull(analyzer);
                assertAnalyzesTo(analyzer, "h100", new String[]{"h", "100"}, new int[]{ 0, 0 }, new int[]{ 4, 4 });

            }
        }

        // Afger 7.3 we do adjust offsets
        {
            Settings settings = Settings.builder()
                .put(Environment.PATH_HOME_SETTING.getKey(), createTempDir().toString())
                .build();
            Settings indexSettings = Settings.builder()
                .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                .put("index.analysis.analyzer.my_analyzer.tokenizer", "standard")
                .putList("index.analysis.analyzer.my_analyzer.filter", "word_delimiter_graph")
                .build();
            IndexSettings idxSettings = IndexSettingsModule.newIndexSettings("index", indexSettings);

            try (IndexAnalyzers indexAnalyzers = new AnalysisModule(TestEnvironment.newEnvironment(settings),
                Collections.singletonList(new CommonAnalysisPlugin())).getAnalysisRegistry().build(idxSettings)) {

                NamedAnalyzer analyzer = indexAnalyzers.get("my_analyzer");
                assertNotNull(analyzer);
                assertAnalyzesTo(analyzer, "h100", new String[]{"h", "100"}, new int[]{ 0, 1 }, new int[]{ 1, 4 });

            }
        }
    }
}
