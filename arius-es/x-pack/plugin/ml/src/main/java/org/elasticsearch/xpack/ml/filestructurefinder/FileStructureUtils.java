/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.ml.filestructurefinder;

import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.grok.Grok;
import org.elasticsearch.ingest.Pipeline;
import org.elasticsearch.xpack.core.ml.filestructurefinder.FieldStats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class FileStructureUtils {

    public static final String DEFAULT_TIMESTAMP_FIELD = "@timestamp";
    public static final String MAPPING_TYPE_SETTING = "type";
    public static final String MAPPING_FORMAT_SETTING = "format";
    public static final String MAPPING_PROPERTIES_SETTING = "properties";
    public static final Map<String, String> DATE_MAPPING_WITHOUT_FORMAT =
        Collections.singletonMap(MAPPING_TYPE_SETTING, "date");

    private static final int NUM_TOP_HITS = 10;
    // NUMBER Grok pattern doesn't support scientific notation, so we extend it
    private static final Grok NUMBER_GROK = new Grok(Grok.getBuiltinPatterns(), "^%{NUMBER}(?:[eE][+-]?[0-3]?[0-9]{1,2})?$",
        TimeoutChecker.watchdog);
    private static final Grok IP_GROK = new Grok(Grok.getBuiltinPatterns(), "^%{IP}$", TimeoutChecker.watchdog);
    private static final int KEYWORD_MAX_LEN = 256;
    private static final int KEYWORD_MAX_SPACES = 5;

    private static final String BEAT_TIMEZONE_FIELD = "beat.timezone";

    private FileStructureUtils() {
    }

    /**
     * Given one or more sample records, find a timestamp field that is consistently present in them all.
     * To be returned the timestamp field:
     * - Must exist in every record
     * - Must have the same timestamp format in every record
     * If multiple fields meet these criteria then the one that occurred first in the first sample record
     * is chosen.
     * @param explanation List of reasons for choosing the overall file structure.  This list
     *                    may be non-empty when the method is called, and this method may
     *                    append to it.
     * @param sampleRecords List of records derived from the provided sample.
     * @param overrides Aspects of the file structure that are known in advance.  These take precedence over
     *                  values determined by structure analysis.  An exception will be thrown if the file structure
     *                  is incompatible with an overridden value.
     * @param timeoutChecker Will abort the operation if its timeout is exceeded.
     * @return A tuple of (field name, timestamp format finder) if one can be found, or <code>null</code> if
     *         there is no consistent timestamp.
     */
    static Tuple<String, TimestampFormatFinder> guessTimestampField(List<String> explanation, List<Map<String, ?>> sampleRecords,
                                                                    FileStructureOverrides overrides, TimeoutChecker timeoutChecker) {
        if (sampleRecords.isEmpty()) {
            return null;
        }

        StringBuilder exceptionMsg = null;

        // Accept the first match from the first sample that is compatible with all the other samples
        for (Tuple<String, TimestampFormatFinder> candidate : findCandidates(explanation, sampleRecords, overrides, timeoutChecker)) {

            String fieldName = candidate.v1();
            TimestampFormatFinder timestampFormatFinder = candidate.v2();

            boolean allGood = true;
            for (Map<String, ?> sampleRecord : sampleRecords.subList(1, sampleRecords.size())) {
                Object fieldValue = sampleRecord.get(fieldName);
                if (fieldValue == null) {
                    if (overrides.getTimestampField() != null) {
                        throw new IllegalArgumentException("Specified timestamp field [" + overrides.getTimestampField() +
                            "] is not present in record [" + sampleRecord + "]");
                    }
                    explanation.add("First sample match [" + fieldName + "] ruled out because record [" + sampleRecord +
                        "] doesn't have field");
                    allGood = false;
                    break;
                }

                timeoutChecker.check("timestamp field determination");

                try {
                    timestampFormatFinder.addSample(fieldValue.toString());
                } catch (IllegalArgumentException e) {
                    if (overrides.getTimestampFormat() != null) {
                        if (exceptionMsg == null) {
                            exceptionMsg = new StringBuilder("Specified timestamp format [" + overrides.getTimestampFormat() +
                                "] does not match");
                        } else {
                            exceptionMsg.append(", nor");
                        }
                        exceptionMsg.append(" for record [").append(sampleRecord).append("] in field [").append(fieldName).append("]");
                    }
                    explanation.add("First sample match " + timestampFormatFinder.getRawJavaTimestampFormats()
                        + " ruled out because record [" + sampleRecord + "] does not match");
                    allGood = false;
                    break;
                }
            }

            if (allGood) {
                explanation.add(((overrides.getTimestampField() == null) ? "Guessing timestamp" : "Timestamp") +
                    " field is [" + fieldName + "] with format " + timestampFormatFinder.getJavaTimestampFormats());
                return candidate;
            }
        }

        if (exceptionMsg != null) {
            throw new IllegalArgumentException(exceptionMsg.toString());
        }

        return null;
    }

    private static List<Tuple<String, TimestampFormatFinder>> findCandidates(List<String> explanation, List<Map<String, ?>> sampleRecords,
                                                                             FileStructureOverrides overrides,
                                                                             TimeoutChecker timeoutChecker) {

        assert sampleRecords.isEmpty() == false;
        Map<String, ?> firstRecord = sampleRecords.get(0);

        String onlyConsiderField = overrides.getTimestampField();
        if (onlyConsiderField != null && firstRecord.get(onlyConsiderField) == null) {
            throw new IllegalArgumentException("Specified timestamp field [" + overrides.getTimestampField() +
                "] is not present in record [" + firstRecord + "]");
        }

        List<Tuple<String, TimestampFormatFinder>> candidates = new ArrayList<>();

        // Get candidate timestamps from the possible field(s) of the first sample record
        for (Map.Entry<String, ?> field : firstRecord.entrySet()) {
            String fieldName = field.getKey();
            if (onlyConsiderField == null || onlyConsiderField.equals(fieldName)) {
                Object value = field.getValue();
                if (value != null) {
                    // Construct the TimestampFormatFinder outside the no-op catch because an exception
                    // from the constructor indicates a problem with the overridden format
                    TimestampFormatFinder timestampFormatFinder =
                        new TimestampFormatFinder(explanation, overrides.getTimestampFormat(), true, true, true, timeoutChecker);
                    try {
                        timestampFormatFinder.addSample(value.toString());
                        candidates.add(new Tuple<>(fieldName, timestampFormatFinder));
                        explanation.add("First sample timestamp match " + timestampFormatFinder.getRawJavaTimestampFormats()
                            + " for field [" + fieldName + "]");
                    } catch (IllegalArgumentException e) {
                        // No possible timestamp format found in this particular field - not a problem
                    }
                }
            }
        }

        if (candidates.isEmpty() && overrides.getTimestampFormat() != null) {
            throw new IllegalArgumentException("Specified timestamp format [" + overrides.getTimestampFormat() +
                "] does not match for record [" + firstRecord + "]");
        }

        return candidates;
    }

    /**
     * Given the sampled records, guess appropriate Elasticsearch mappings.
     * @param explanation List of reasons for making decisions.  May contain items when passed and new reasons
     *                    can be appended by this method.
     * @param sampleRecords The sampled records.
     * @param timeoutChecker Will abort the operation if its timeout is exceeded.
     * @return A map of field name to mapping settings.
     */
    static Tuple<SortedMap<String, Object>, SortedMap<String, FieldStats>> guessMappingsAndCalculateFieldStats(
        List<String> explanation, List<Map<String, ?>> sampleRecords, TimeoutChecker timeoutChecker) {

        SortedMap<String, Object> mappings = new TreeMap<>();
        SortedMap<String, FieldStats> fieldStats = new TreeMap<>();

        Set<String> uniqueFieldNames = sampleRecords.stream().flatMap(record -> record.keySet().stream()).collect(Collectors.toSet());

        for (String fieldName : uniqueFieldNames) {

            List<Object> fieldValues = sampleRecords.stream().map(record -> record.get(fieldName)).filter(fieldValue -> fieldValue != null)
                .collect(Collectors.toList());

            Tuple<Map<String, String>, FieldStats> mappingAndFieldStats =
                guessMappingAndCalculateFieldStats(explanation, fieldName, fieldValues, timeoutChecker);
            if (mappingAndFieldStats != null) {
                if (mappingAndFieldStats.v1() != null) {
                    mappings.put(fieldName, mappingAndFieldStats.v1());
                }
                if (mappingAndFieldStats.v2() != null) {
                    fieldStats.put(fieldName, mappingAndFieldStats.v2());
                }
            }
        }

        return new Tuple<>(mappings, fieldStats);
    }

    static Tuple<Map<String, String>, FieldStats> guessMappingAndCalculateFieldStats(List<String> explanation,
                                                                                     String fieldName, List<Object> fieldValues,
                                                                                     TimeoutChecker timeoutChecker) {
        if (fieldValues == null || fieldValues.isEmpty()) {
            // We can get here if all the records that contained a given field had a null value for it.
            // In this case it's best not to make any statement about what the mapping type should be.
            return null;
        }

        if (fieldValues.stream().anyMatch(value -> value instanceof Map)) {
            if (fieldValues.stream().allMatch(value -> value instanceof Map)) {
                return new Tuple<>(Collections.singletonMap(MAPPING_TYPE_SETTING, "object"), null);
            }
            throw new IllegalArgumentException("Field [" + fieldName +
                "] has both object and non-object values - this is not supported by Elasticsearch");
        }

        if (fieldValues.stream().anyMatch(value -> value instanceof List || value instanceof Object[])) {
            // Elasticsearch fields can be either arrays or single values, but array values must all have the same type
            return guessMappingAndCalculateFieldStats(explanation, fieldName,
                fieldValues.stream().flatMap(FileStructureUtils::flatten).collect(Collectors.toList()), timeoutChecker);
        }

        Collection<String> fieldValuesAsStrings = fieldValues.stream().map(Object::toString).collect(Collectors.toList());
        Map<String, String> mapping = guessScalarMapping(explanation, fieldName, fieldValuesAsStrings, timeoutChecker);
        timeoutChecker.check("mapping determination");
        return new Tuple<>(mapping, calculateFieldStats(mapping, fieldValuesAsStrings, timeoutChecker));
    }

    private static Stream<Object> flatten(Object value) {
        if (value instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> objectList = (List<Object>) value;
            return objectList.stream();
        } else if (value instanceof Object[]) {
            return Arrays.stream((Object[]) value);
        } else {
            return Stream.of(value);
        }
    }

    /**
     * Finds the appropriate date mapping for a collection of field values.  Throws
     * {@link IllegalArgumentException} if no consistent date mapping can be found.
     * @param explanation List of reasons for choosing the overall file structure.  This list
     *                    may be non-empty when the method is called, and this method may
     *                    append to it.
     * @param fieldValues Values of the field for which mappings are to be guessed.  The guessed
     *                    mapping will be compatible with all the provided values.  Must not be
     *                    empty.
     * @param timeoutChecker Will abort the operation if its timeout is exceeded.
     * @return The sub-section of the index mappings most appropriate for the field.
     */
    static Map<String, String> findTimestampMapping(List<String> explanation, Collection<String> fieldValues,
                                                    TimeoutChecker timeoutChecker) {
        assert fieldValues.isEmpty() == false;

        TimestampFormatFinder timestampFormatFinder = new TimestampFormatFinder(explanation, true, true, true, timeoutChecker);
        fieldValues.forEach(timestampFormatFinder::addSample);
        return timestampFormatFinder.getEsDateMappingTypeWithFormat();
    }

    /**
     * Given some sample values for a field, guess the most appropriate index mapping for the
     * field.
     * @param explanation List of reasons for choosing the overall file structure.  This list
     *                    may be non-empty when the method is called, and this method may
     *                    append to it.
     * @param fieldName Name of the field for which mappings are to be guessed.
     * @param fieldValues Values of the field for which mappings are to be guessed.  The guessed
     *                    mapping will be compatible with all the provided values.  Must not be
     *                    empty.
     * @param timeoutChecker Will abort the operation if its timeout is exceeded.
     * @return The sub-section of the index mappings most appropriate for the field,
     *         for example <code>{ "type" : "keyword" }</code>.
     */
    static Map<String, String> guessScalarMapping(List<String> explanation, String fieldName, Collection<String> fieldValues,
                                                  TimeoutChecker timeoutChecker) {
        assert fieldValues.isEmpty() == false;

        if (fieldValues.stream().allMatch(value -> "true".equals(value) || "false".equals(value))) {
            return Collections.singletonMap(MAPPING_TYPE_SETTING, "boolean");
        }

        try {
            return findTimestampMapping(explanation, fieldValues, timeoutChecker);
        } catch (IllegalArgumentException e) {
            // To be mapped as type "date" all the values must match the same timestamp format - if
            // they don't we'll end up here, and move on to try other possible mappings
        }

        if (fieldValues.stream().allMatch(NUMBER_GROK::match)) {
            try {
                fieldValues.forEach(Long::parseLong);
                return Collections.singletonMap(MAPPING_TYPE_SETTING, "long");
            } catch (NumberFormatException e) {
                explanation.add("Rejecting type 'long' for field [" + fieldName + "] due to parse failure: [" + e.getMessage() + "]");
            }
            try {
                fieldValues.forEach(Double::parseDouble);
                return Collections.singletonMap(MAPPING_TYPE_SETTING, "double");
            } catch (NumberFormatException e) {
                explanation.add("Rejecting type 'double' for field [" + fieldName + "] due to parse failure: [" + e.getMessage() + "]");
            }
        }
        else if (fieldValues.stream().allMatch(IP_GROK::match)) {
            return Collections.singletonMap(MAPPING_TYPE_SETTING, "ip");
        }

        if (fieldValues.stream().anyMatch(FileStructureUtils::isMoreLikelyTextThanKeyword)) {
            return Collections.singletonMap(MAPPING_TYPE_SETTING, "text");
        }

        return Collections.singletonMap(MAPPING_TYPE_SETTING, "keyword");
    }

    /**
     * Calculate stats for a set of field values.
     * @param mapping The  mapping for the field.
     * @param fieldValues Values of the field for which field stats are to be calculated.
     * @param timeoutChecker Will abort the operation if its timeout is exceeded.
     * @return The stats calculated from the field values.
     */
    static FieldStats calculateFieldStats(Map<String, String> mapping, Collection<String> fieldValues, TimeoutChecker timeoutChecker) {

        FieldStatsCalculator calculator = new FieldStatsCalculator(mapping);
        calculator.accept(fieldValues);
        timeoutChecker.check("field stats calculation");
        return calculator.calculate(NUM_TOP_HITS);
    }

    /**
     * The thinking is that the longer the field value and the more spaces it contains,
     * the more likely it is that it should be indexed as text rather than keyword.
     */
    static boolean isMoreLikelyTextThanKeyword(String str) {
        int length = str.length();
        return length > KEYWORD_MAX_LEN || length - str.replaceAll("\\s", "").length() > KEYWORD_MAX_SPACES;
    }

    /**
     * Create an ingest pipeline definition appropriate for the file structure.
     * @param grokPattern The Grok pattern used for parsing semi-structured text formats.  <code>null</code> for
     *                    fully structured formats.
     * @param customGrokPatternDefinitions The definitions for any custom patterns that {@code grokPattern} uses.
     * @param timestampField The input field containing the timestamp to be parsed into <code>@timestamp</code>.
     *                       <code>null</code> if there is no timestamp.
     * @param timestampFormats Timestamp formats to be used for parsing {@code timestampField}.
     *                         May be <code>null</code> if {@code timestampField} is also <code>null</code>.
     * @param needClientTimezone Is the timezone of the client supplying data to ingest required to uniquely parse the timestamp?
     * @return The ingest pipeline definition, or <code>null</code> if none is required.
     */
    public static Map<String, Object> makeIngestPipelineDefinition(String grokPattern, Map<String, String> customGrokPatternDefinitions,
                                                                   String timestampField, List<String> timestampFormats,
                                                                   boolean needClientTimezone) {

        if (grokPattern == null && timestampField == null) {
            return null;
        }

        Map<String, Object> pipeline = new LinkedHashMap<>();
        pipeline.put(Pipeline.DESCRIPTION_KEY, "Ingest pipeline created by file structure finder");

        List<Map<String, Object>> processors = new ArrayList<>();

        if (grokPattern != null) {
            Map<String, Object> grokProcessorSettings = new LinkedHashMap<>();
            grokProcessorSettings.put("field", "message");
            grokProcessorSettings.put("patterns", Collections.singletonList(grokPattern));
            if (customGrokPatternDefinitions.isEmpty() == false) {
                grokProcessorSettings.put("pattern_definitions", customGrokPatternDefinitions);
            }
            processors.add(Collections.singletonMap("grok", grokProcessorSettings));
        } else {
            assert customGrokPatternDefinitions.isEmpty();
        }

        if (timestampField != null) {
            Map<String, Object> dateProcessorSettings = new LinkedHashMap<>();
            dateProcessorSettings.put("field", timestampField);
            if (needClientTimezone) {
                dateProcessorSettings.put("timezone", "{{ " + BEAT_TIMEZONE_FIELD + " }}");
            }
            dateProcessorSettings.put("formats", timestampFormats);
            processors.add(Collections.singletonMap("date", dateProcessorSettings));
        }

        // This removes the interim timestamp field used for semi-structured text formats
        if (grokPattern != null && timestampField != null) {
            processors.add(Collections.singletonMap("remove", Collections.singletonMap("field", timestampField)));
        }

        pipeline.put(Pipeline.PROCESSORS_KEY, processors);
        return pipeline;
    }
}
