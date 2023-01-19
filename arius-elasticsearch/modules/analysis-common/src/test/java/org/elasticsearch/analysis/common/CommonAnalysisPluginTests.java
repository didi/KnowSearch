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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.Version;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.IndexAnalyzers;
import org.elasticsearch.index.analysis.NamedAnalyzer;
import org.elasticsearch.index.analysis.TokenizerFactory;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.test.IndexSettingsModule;
import org.elasticsearch.test.VersionUtils;

import java.io.IOException;
import java.util.Map;

public class CommonAnalysisPluginTests extends ESTestCase {

    /**
     * Check that the deprecated name "nGram" issues a deprecation warning for indices created since 6.0.0
     */
    public void testNGramDeprecationWarning() throws IOException {
        Settings settings = Settings.builder().put(Environment.PATH_HOME_SETTING.getKey(), createTempDir())
            .put(IndexMetaData.SETTING_VERSION_CREATED,
                        VersionUtils.randomVersionBetween(random(), Version.V_6_0_0, VersionUtils.getPreviousVersion(Version.V_7_0_0)))
            .put("index.analysis.analyzer.custom_analyzer.type", "custom")
            .put("index.analysis.analyzer.custom_analyzer.tokenizer", "standard")
            .putList("index.analysis.analyzer.custom_analyzer.filter", "nGram")
            .build();

        try (CommonAnalysisPlugin commonAnalysisPlugin = new CommonAnalysisPlugin()) {
            createTestAnalysis(IndexSettingsModule.newIndexSettings("index", settings), settings, commonAnalysisPlugin);
        }

        assertWarnings("The [nGram] token filter name is deprecated and will be removed in a future version. "
            + "Please change the filter name to [ngram] instead.");
    }

    /**
     * Check that the deprecated name "nGram" throws an error since 7.0.0
     */
    public void testNGramDeprecationError() throws IOException {
        Settings settings = Settings.builder().put(Environment.PATH_HOME_SETTING.getKey(), createTempDir())
            .put(IndexMetaData.SETTING_VERSION_CREATED, VersionUtils.randomVersionBetween(random(), Version.V_7_0_0, null))
            .put("index.analysis.analyzer.custom_analyzer.type", "custom")
            .put("index.analysis.analyzer.custom_analyzer.tokenizer", "standard")
            .putList("index.analysis.analyzer.custom_analyzer.filter", "nGram")
            .build();

        try (CommonAnalysisPlugin commonAnalysisPlugin = new CommonAnalysisPlugin()) {
            IllegalArgumentException e = expectThrows(IllegalArgumentException.class,
                () -> createTestAnalysis(IndexSettingsModule.newIndexSettings("index", settings), settings, commonAnalysisPlugin));
            assertEquals("The [nGram] token filter name was deprecated in 6.4 and cannot be used in new indices. "
                + "Please change the filter name to [ngram] instead.", e.getMessage());
        }
    }

    /**
     * Check that the deprecated name "edgeNGram" issues a deprecation warning for indices created since 6.0.0
     */
    public void testEdgeNGramDeprecationWarning() throws IOException {
        Settings settings = Settings.builder().put(Environment.PATH_HOME_SETTING.getKey(), createTempDir())
            .put(IndexMetaData.SETTING_VERSION_CREATED,
                VersionUtils.randomVersionBetween(random(), Version.V_6_4_0, VersionUtils.getPreviousVersion(Version.V_7_0_0)))
            .put("index.analysis.analyzer.custom_analyzer.type", "custom")
            .put("index.analysis.analyzer.custom_analyzer.tokenizer", "standard")
            .putList("index.analysis.analyzer.custom_analyzer.filter", "edgeNGram")
            .build();

        try (CommonAnalysisPlugin commonAnalysisPlugin = new CommonAnalysisPlugin()) {
            createTestAnalysis(IndexSettingsModule.newIndexSettings("index", settings), settings, commonAnalysisPlugin);
        }
        assertWarnings("The [edgeNGram] token filter name is deprecated and will be removed in a future version. "
            + "Please change the filter name to [edge_ngram] instead.");
    }

    /**
     * Check that the deprecated name "edgeNGram" throws an error for indices created since 7.0.0
     */
    public void testEdgeNGramDeprecationError() throws IOException {
        Settings settings = Settings.builder().put(Environment.PATH_HOME_SETTING.getKey(), createTempDir())
            .put(IndexMetaData.SETTING_VERSION_CREATED, VersionUtils.randomVersionBetween(random(), Version.V_7_0_0, null))
            .put("index.analysis.analyzer.custom_analyzer.type", "custom")
            .put("index.analysis.analyzer.custom_analyzer.tokenizer", "standard")
            .putList("index.analysis.analyzer.custom_analyzer.filter", "edgeNGram")
            .build();

        try (CommonAnalysisPlugin commonAnalysisPlugin = new CommonAnalysisPlugin()) {
            IllegalArgumentException ex = expectThrows(IllegalArgumentException.class,
                () -> createTestAnalysis(IndexSettingsModule.newIndexSettings("index", settings), settings, commonAnalysisPlugin));
            assertEquals("The [edgeNGram] token filter name was deprecated in 6.4 and cannot be used in new indices. "
                + "Please change the filter name to [edge_ngram] instead.", ex.getMessage());
        }
    }

    /**
     * Check that the deprecated analyzer name "standard_html_strip" throws exception for indices created since 7.0.0
     */
    public void testStandardHtmlStripAnalyzerDeprecationError() throws IOException {
        Settings settings = Settings.builder().put(Environment.PATH_HOME_SETTING.getKey(), createTempDir())
            .put(IndexMetaData.SETTING_VERSION_CREATED,
                VersionUtils.randomVersionBetween(random(), Version.V_7_0_0, Version.CURRENT))
            .put("index.analysis.analyzer.custom_analyzer.type", "standard_html_strip")
            .putList("index.analysis.analyzer.custom_analyzer.stopwords", "a", "b")
            .build();

        IndexSettings idxSettings = IndexSettingsModule.newIndexSettings("index", settings);
        CommonAnalysisPlugin commonAnalysisPlugin = new CommonAnalysisPlugin();
        IllegalArgumentException ex = expectThrows(IllegalArgumentException.class,
            () -> createTestAnalysis(idxSettings, settings, commonAnalysisPlugin));
        assertEquals("[standard_html_strip] analyzer is not supported for new indices, " +
            "use a custom analyzer using [standard] tokenizer and [html_strip] char_filter, plus [lowercase] filter", ex.getMessage());
    }

    /**
     * Check that the deprecated analyzer name "standard_html_strip" issues a deprecation warning for indices created since 6.5.0 until 7
     */
    public void testStandardHtmlStripAnalyzerDeprecationWarning() throws IOException {
        Settings settings = Settings.builder().put(Environment.PATH_HOME_SETTING.getKey(), createTempDir())
            .put(IndexMetaData.SETTING_VERSION_CREATED,
                VersionUtils.randomVersionBetween(random(), Version.V_6_0_0,
                    VersionUtils.getPreviousVersion(Version.V_7_0_0)))
            .put("index.analysis.analyzer.custom_analyzer.type", "standard_html_strip")
            .putList("index.analysis.analyzer.custom_analyzer.stopwords", "a", "b")
            .build();

        IndexSettings idxSettings = IndexSettingsModule.newIndexSettings("index", settings);
        try (CommonAnalysisPlugin commonAnalysisPlugin = new CommonAnalysisPlugin()) {
            IndexAnalyzers analyzers = createTestAnalysis(idxSettings, settings, commonAnalysisPlugin).indexAnalyzers;
            Analyzer analyzer = analyzers.get("custom_analyzer");
            assertNotNull(((NamedAnalyzer) analyzer).analyzer());
            assertWarnings(
                "Deprecated analyzer [standard_html_strip] used, " +
                    "replace it with a custom analyzer using [standard] tokenizer and [html_strip] char_filter, plus [lowercase] filter");
        }
    }

    /**
     * Check that the deprecated "nGram" filter logs a warning when the filter is used as a custom filter
     */
    public void testnGramFilterInCustomAnalyzerDeprecationError() throws IOException {
        final Settings settings = Settings.builder().put(Environment.PATH_HOME_SETTING.getKey(), createTempDir())
            .put(IndexMetaData.SETTING_VERSION_CREATED,
                VersionUtils.randomVersionBetween(random(), Version.V_7_0_0, Version.CURRENT))
            .put("index.analysis.analyzer.custom_analyzer.type", "custom")
            .put("index.analysis.analyzer.custom_analyzer.tokenizer", "standard")
            .putList("index.analysis.analyzer.custom_analyzer.filter", "my_ngram")
            .put("index.analysis.filter.my_ngram.type", "nGram")
            .build();

        final CommonAnalysisPlugin commonAnalysisPlugin = new CommonAnalysisPlugin();

        createTestAnalysis(IndexSettingsModule.newIndexSettings("index", settings), settings, commonAnalysisPlugin);
        assertWarnings("The [nGram] token filter name is deprecated and will be removed in a future version. "
                + "Please change the filter name to [ngram] instead.");
    }

    /**
     * Check that the deprecated "edgeNGram" filter logs a warning when the filter is used as a custom filter
     */
    public void testEdgeNGramFilterInCustomAnalyzerDeprecationError() throws IOException {
        final Settings settings = Settings.builder().put(Environment.PATH_HOME_SETTING.getKey(), createTempDir())
            .put(IndexMetaData.SETTING_VERSION_CREATED,
                VersionUtils.randomVersionBetween(random(), Version.V_7_0_0, Version.CURRENT))
            .put("index.analysis.analyzer.custom_analyzer.type", "custom")
            .put("index.analysis.analyzer.custom_analyzer.tokenizer", "standard")
            .putList("index.analysis.analyzer.custom_analyzer.filter", "my_ngram")
            .put("index.analysis.filter.my_ngram.type", "edgeNGram")
            .build();
        final CommonAnalysisPlugin commonAnalysisPlugin = new CommonAnalysisPlugin();

        createTestAnalysis(IndexSettingsModule.newIndexSettings("index", settings), settings, commonAnalysisPlugin);
        assertWarnings("The [edgeNGram] token filter name is deprecated and will be removed in a future version. "
                + "Please change the filter name to [edge_ngram] instead.");
    }

    /**
     * Check that we log a deprecation warning for "nGram" and "edgeNGram" tokenizer names with 7.6 and
     * disallow usages for indices created after 8.0
     */
    public void testNGramTokenizerDeprecation() throws IOException {
        // tests for prebuilt tokenizer
        doTestPrebuiltTokenizerDeprecation("nGram", "ngram",
                VersionUtils.randomVersionBetween(random(), Version.V_7_0_0, Version.V_7_5_2), false);
        doTestPrebuiltTokenizerDeprecation("edgeNGram", "edge_ngram",
                VersionUtils.randomVersionBetween(random(), Version.V_7_0_0, Version.V_7_5_2), false);
        doTestPrebuiltTokenizerDeprecation("nGram", "ngram", Version.V_7_6_0, true);
        doTestPrebuiltTokenizerDeprecation("edgeNGram", "edge_ngram", Version.V_7_6_0, true);

        // same batch of tests for custom tokenizer definition in the settings
        doTestCustomTokenizerDeprecation("nGram", "ngram",
                VersionUtils.randomVersionBetween(random(), Version.V_7_0_0, Version.V_7_5_2), false);
        doTestCustomTokenizerDeprecation("edgeNGram", "edge_ngram",
                VersionUtils.randomVersionBetween(random(), Version.V_7_0_0, Version.V_7_5_2), false);
        doTestCustomTokenizerDeprecation("nGram", "ngram", Version.V_7_6_0, true);
        doTestCustomTokenizerDeprecation("edgeNGram", "edge_ngram", Version.V_7_6_0, true);
    }

    public void doTestPrebuiltTokenizerDeprecation(String deprecatedName, String replacement, Version version, boolean expectWarning)
            throws IOException {
        final Settings settings = Settings.builder().put(Environment.PATH_HOME_SETTING.getKey(), createTempDir())
            .put(IndexMetaData.SETTING_VERSION_CREATED, version).build();

        try (CommonAnalysisPlugin commonAnalysisPlugin = new CommonAnalysisPlugin()) {
            Map<String, TokenizerFactory> tokenizers = createTestAnalysis(
                    IndexSettingsModule.newIndexSettings("index", settings), settings, commonAnalysisPlugin).tokenizer;
            TokenizerFactory tokenizerFactory = tokenizers.get(deprecatedName);

            Tokenizer tokenizer = tokenizerFactory.create();
            assertNotNull(tokenizer);
            if (expectWarning) {
                assertWarnings("The [" + deprecatedName + "] tokenizer name is deprecated and will be removed in a future version. "
                        + "Please change the tokenizer name to [" + replacement + "] instead.");
            }
        }
    }

    public void doTestCustomTokenizerDeprecation(String deprecatedName, String replacement, Version version, boolean expectWarning)
            throws IOException {
        final Settings settings = Settings.builder().put(Environment.PATH_HOME_SETTING.getKey(), createTempDir())
            .put(IndexMetaData.SETTING_VERSION_CREATED, version)
            .put("index.analysis.analyzer.custom_analyzer.type", "custom")
            .put("index.analysis.analyzer.custom_analyzer.tokenizer", "my_tokenizer")
            .put("index.analysis.tokenizer.my_tokenizer.type", deprecatedName)
        .build();

        try (CommonAnalysisPlugin commonAnalysisPlugin = new CommonAnalysisPlugin()) {
            createTestAnalysis(IndexSettingsModule.newIndexSettings("index", settings), settings, commonAnalysisPlugin);

            if (expectWarning) {
                assertWarnings("The [" + deprecatedName + "] tokenizer name is deprecated and will be removed in a future version. "
                        + "Please change the tokenizer name to [" + replacement + "] instead.");
            }
        }
    }
}
