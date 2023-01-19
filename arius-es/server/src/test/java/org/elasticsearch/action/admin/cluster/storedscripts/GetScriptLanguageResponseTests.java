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

package org.elasticsearch.action.admin.cluster.storedscripts;

import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.script.ScriptLanguagesInfo;
import org.elasticsearch.test.AbstractSerializingTestCase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GetScriptLanguageResponseTests extends AbstractSerializingTestCase<GetScriptLanguageResponse> {
    private static int MAX_VALUES = 4;
    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 16;

    @Override
    protected GetScriptLanguageResponse createTestInstance() {
        if (randomBoolean()) {
            return new GetScriptLanguageResponse(
                new ScriptLanguagesInfo(Collections.emptySet(), Collections.emptyMap())
            );
        }
        return new GetScriptLanguageResponse(randomInstance());
    }

    @Override
    protected GetScriptLanguageResponse doParseInstance(XContentParser parser) throws IOException {
        return GetScriptLanguageResponse.fromXContent(parser);
    }

    @Override
    protected Writeable.Reader<GetScriptLanguageResponse> instanceReader() {  return GetScriptLanguageResponse::new; }

    @Override
    protected GetScriptLanguageResponse mutateInstance(GetScriptLanguageResponse instance) throws IOException {
        switch (randomInt(2)) {
            case 0:
                // mutate typesAllowed
                return new GetScriptLanguageResponse(
                    new ScriptLanguagesInfo(mutateStringSet(instance.info.typesAllowed), instance.info.languageContexts)
                );
            case 1:
                // Add language
                String language = randomValueOtherThanMany(
                    instance.info.languageContexts::containsKey,
                    () -> randomAlphaOfLengthBetween(MIN_LENGTH, MAX_LENGTH)
                );
                Map<String,Set<String>> languageContexts = new HashMap<>();
                instance.info.languageContexts.forEach(languageContexts::put);
                languageContexts.put(language, randomStringSet(randomIntBetween(1, MAX_VALUES)));
                return new GetScriptLanguageResponse(new ScriptLanguagesInfo(instance.info.typesAllowed, languageContexts));
            default:
                // Mutate languageContexts
                Map<String,Set<String>> lc = new HashMap<>();
                if (instance.info.languageContexts.size() == 0) {
                    lc.put(randomAlphaOfLengthBetween(MIN_LENGTH, MAX_LENGTH), randomStringSet(randomIntBetween(1, MAX_VALUES)));
                } else {
                    int toModify = randomInt(instance.info.languageContexts.size()-1);
                    List<String> keys = new ArrayList<>(instance.info.languageContexts.keySet());
                    for (int i=0; i<keys.size(); i++) {
                        String key = keys.get(i);
                        Set<String> value = instance.info.languageContexts.get(keys.get(i));
                        if (i == toModify) {
                            value = mutateStringSet(instance.info.languageContexts.get(keys.get(i)));
                        }
                        lc.put(key, value);
                    }
                }
                return new GetScriptLanguageResponse(new ScriptLanguagesInfo(instance.info.typesAllowed, lc));
        }
    }

    private static ScriptLanguagesInfo randomInstance() {
        Map<String,Set<String>> contexts = new HashMap<>();
        for (String context: randomStringSet(randomIntBetween(1, MAX_VALUES))) {
            contexts.put(context, randomStringSet(randomIntBetween(1, MAX_VALUES)));
        }
        return new ScriptLanguagesInfo(randomStringSet(randomInt(MAX_VALUES)), contexts);
    }

    private static Set<String> randomStringSet(int numInstances) {
        Set<String> rand = new HashSet<>(numInstances);
        for (int i = 0; i < numInstances; i++) {
            rand.add(randomValueOtherThanMany(rand::contains, () -> randomAlphaOfLengthBetween(MIN_LENGTH, MAX_LENGTH)));
        }
        return rand;
    }

    private static Set<String> mutateStringSet(Set<String> strings) {
        if (strings.isEmpty()) {
            return Collections.singleton(randomAlphaOfLengthBetween(MIN_LENGTH, MAX_LENGTH));
        }

        if (randomBoolean()) {
            Set<String> updated = new HashSet<>(strings);
            updated.add(randomValueOtherThanMany(updated::contains, () -> randomAlphaOfLengthBetween(MIN_LENGTH, MAX_LENGTH)));
            return updated;
        } else {
            List<String> sorted = strings.stream().sorted().collect(Collectors.toList());
            int toRemove = randomInt(sorted.size() - 1);
            Set<String> updated = new HashSet<>();
            for (int i = 0; i < sorted.size(); i++) {
                if (i != toRemove) {
                    updated.add(sorted.get(i));
                }
            }
            return updated;
        }
    }
}
