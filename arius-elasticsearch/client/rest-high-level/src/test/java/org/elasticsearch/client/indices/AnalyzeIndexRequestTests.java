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

package org.elasticsearch.client.indices;

import org.elasticsearch.action.admin.indices.analyze.AnalyzeAction;
import org.elasticsearch.common.xcontent.XContentParser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AnalyzeIndexRequestTests extends AnalyzeRequestTests {

    private static final Map<String, Object> charFilterConfig = new HashMap<>();
    static {
        charFilterConfig.put("type", "html_strip");
    }

    private static final Map<String, Object> tokenFilterConfig = new HashMap<>();
    static {
        tokenFilterConfig.put("type", "synonym");
    }

    @Override
    protected AnalyzeRequest createClientTestInstance() {
        int option = random().nextInt(5);
        switch (option) {
            case 0:
                return AnalyzeRequest.withField("index", "field", "some text", "some more text");
            case 1:
                return AnalyzeRequest.withIndexAnalyzer("index", "my_analyzer", "some text", "some more text");
            case 2:
                return AnalyzeRequest.withNormalizer("index", "my_normalizer", "text", "more text");
            case 3:
                return AnalyzeRequest.buildCustomAnalyzer("index", "my_tokenizer")
                    .addCharFilter("my_char_filter")
                    .addCharFilter(charFilterConfig)
                    .addTokenFilter("my_token_filter")
                    .addTokenFilter(tokenFilterConfig)
                    .build("some text", "some more text");
            case 4:
                return AnalyzeRequest.buildCustomNormalizer("index")
                    .addCharFilter("my_char_filter")
                    .addCharFilter(charFilterConfig)
                    .addTokenFilter("my_token_filter")
                    .addTokenFilter(tokenFilterConfig)
                    .build("some text", "some more text");
        }
        throw new IllegalStateException("nextInt(5) has returned a value greater than 4");
    }

    @Override
    protected AnalyzeAction.Request doParseToServerInstance(XContentParser parser) throws IOException {
        return AnalyzeAction.Request.fromXContent(parser, "index");
    }
}
