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

import org.elasticsearch.common.util.set.Sets;
import org.elasticsearch.ingest.AbstractProcessor;
import org.elasticsearch.ingest.ConfigurationUtils;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.Processor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * The KeyValueProcessor parses and extracts messages of the `key=value` variety into fields with values of the keys.
 */
public final class KeyValueProcessor extends AbstractProcessor {

    public static final String TYPE = "kv";

    private static final Pattern STRIP_BRACKETS = Pattern.compile("(^[\\(\\[<\"'])|([\\]\\)>\"']$)");

    private final String field;
    private final String fieldSplit;
    private final String valueSplit;
    private final Set<String> includeKeys;
    private final Set<String> excludeKeys;
    private final String targetField;
    private final boolean ignoreMissing;
    private final Consumer<IngestDocument> execution;

    KeyValueProcessor(String tag, String field, String fieldSplit, String valueSplit, Set<String> includeKeys,
                      Set<String> excludeKeys, String targetField, boolean ignoreMissing,
                      String trimKey, String trimValue, boolean stripBrackets, String prefix) {
        super(tag);
        this.field = field;
        this.targetField = targetField;
        this.fieldSplit = fieldSplit;
        this.valueSplit = valueSplit;
        this.includeKeys = includeKeys;
        this.excludeKeys = excludeKeys;
        this.ignoreMissing = ignoreMissing;
        this.execution = buildExecution(
            fieldSplit, valueSplit, field, includeKeys, excludeKeys, targetField, ignoreMissing, trimKey, trimValue,
            stripBrackets, prefix
        );
    }

    private static Consumer<IngestDocument> buildExecution(String fieldSplit, String valueSplit, String field,
                                                           Set<String> includeKeys, Set<String> excludeKeys,
                                                           String targetField, boolean ignoreMissing,
                                                           String trimKey, String trimValue, boolean stripBrackets,
                                                           String prefix) {
        final Predicate<String> keyFilter;
        if (includeKeys == null) {
            if (excludeKeys == null) {
                keyFilter = key -> true;
            } else {
                keyFilter = key -> excludeKeys.contains(key) == false;
            }
        } else {
            if (excludeKeys == null) {
                keyFilter = includeKeys::contains;
            } else {
                keyFilter = key -> includeKeys.contains(key) && excludeKeys.contains(key) == false;
            }
        }
        final String fieldPathPrefix;
        String keyPrefix = prefix == null ? "" : prefix;
        if (targetField == null) {
            fieldPathPrefix = keyPrefix;
        } else {
            fieldPathPrefix = targetField + "." + keyPrefix;
        }
        final Function<String, String> keyPrefixer;
        if (fieldPathPrefix.isEmpty()) {
            keyPrefixer = val -> val;
        } else {
            keyPrefixer = val -> fieldPathPrefix + val;
        }
        final Function<String, String[]> fieldSplitter = buildSplitter(fieldSplit, true);
        Function<String, String[]> valueSplitter = buildSplitter(valueSplit, false);
        final Function<String, String> keyTrimmer = buildTrimmer(trimKey);
        final Function<String, String> bracketStrip;
        if (stripBrackets) {
            bracketStrip = val -> STRIP_BRACKETS.matcher(val).replaceAll("");
        } else {
            bracketStrip = val -> val;
        }
        final Function<String, String> valueTrimmer = buildTrimmer(trimValue);
        return document -> {
            String value = document.getFieldValue(field, String.class, ignoreMissing);
            if (value == null) {
                if (ignoreMissing) {
                    return;
                }
                throw new IllegalArgumentException("field [" + field + "] is null, cannot extract key-value pairs.");
            }
            for (String part : fieldSplitter.apply(value)) {
                String[] kv = valueSplitter.apply(part);
                if (kv.length != 2) {
                    throw new IllegalArgumentException("field [" + field + "] does not contain value_split [" + valueSplit + "]");
                }
                String key = keyTrimmer.apply(kv[0]);
                if (keyFilter.test(key)) {
                    append(document, keyPrefixer.apply(key), valueTrimmer.apply(bracketStrip.apply(kv[1])));
                }
            }
        };
    }

    private static Function<String, String> buildTrimmer(String trim) {
        if (trim == null) {
            return val -> val;
        } else {
            Pattern pattern = Pattern.compile("(^([" + trim + "]+))|([" + trim + "]+$)");
            return val -> pattern.matcher(val).replaceAll("");
        }
    }

    private static Function<String, String[]> buildSplitter(String split, boolean fields) {
        int limit = fields ? 0 : 2;
        if (split.length() > 2 || split.length() == 2 && split.charAt(0) != '\\') {
            Pattern splitPattern = Pattern.compile(split);
            return val -> splitPattern.split(val, limit);
        } else {
            return val -> val.split(split, limit);
        }
    }

    String getField() {
        return field;
    }

    String getFieldSplit() {
        return fieldSplit;
    }

    String getValueSplit() {
        return valueSplit;
    }

    Set<String> getIncludeKeys() {
        return includeKeys;
    }

    Set<String> getExcludeKeys() {
        return excludeKeys;
    }

    String getTargetField() {
        return targetField;
    }

    boolean isIgnoreMissing() {
        return ignoreMissing;
    }

    private static void append(IngestDocument document, String targetField, String value) {
        if (document.hasField(targetField)) {
            document.appendFieldValue(targetField, value);
        } else {
            document.setFieldValue(targetField, value);
        }
    }

    @Override
    public IngestDocument execute(IngestDocument document) {
        execution.accept(document);
        return document;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public static class Factory implements Processor.Factory {
        @Override
        public KeyValueProcessor create(Map<String, Processor.Factory> registry, String processorTag,
                                        Map<String, Object> config) throws Exception {
            String field = ConfigurationUtils.readStringProperty(TYPE, processorTag, config, "field");
            String targetField = ConfigurationUtils.readOptionalStringProperty(TYPE, processorTag, config, "target_field");
            String fieldSplit = ConfigurationUtils.readStringProperty(TYPE, processorTag, config, "field_split");
            String valueSplit = ConfigurationUtils.readStringProperty(TYPE, processorTag, config, "value_split");
            String trimKey = ConfigurationUtils.readOptionalStringProperty(TYPE, processorTag, config, "trim_key");
            String trimValue = ConfigurationUtils.readOptionalStringProperty(TYPE, processorTag, config, "trim_value");
            String prefix = ConfigurationUtils.readOptionalStringProperty(TYPE, processorTag, config, "prefix");
            boolean stripBrackets =
                ConfigurationUtils.readBooleanProperty(TYPE, processorTag, config, "strip_brackets", false);
            Set<String> includeKeys = null;
            Set<String> excludeKeys = null;
            List<String> includeKeysList = ConfigurationUtils.readOptionalList(TYPE, processorTag, config, "include_keys");
            if (includeKeysList != null) {
                includeKeys = Collections.unmodifiableSet(Sets.newHashSet(includeKeysList));
            }
            List<String> excludeKeysList = ConfigurationUtils.readOptionalList(TYPE, processorTag, config, "exclude_keys");
            if (excludeKeysList != null) {
                excludeKeys = Collections.unmodifiableSet(Sets.newHashSet(excludeKeysList));
            }
            boolean ignoreMissing = ConfigurationUtils.readBooleanProperty(TYPE, processorTag, config, "ignore_missing", false);
            return new KeyValueProcessor(
                processorTag, field, fieldSplit, valueSplit, includeKeys, excludeKeys, targetField, ignoreMissing,
                trimKey, trimValue, stripBrackets, prefix
            );
        }
    }
}
