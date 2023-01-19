/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.core.ml.job.config;

import org.elasticsearch.Version;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.ConstructingObjectParser;
import org.elasticsearch.common.xcontent.ObjectParser;
import org.elasticsearch.common.xcontent.ToXContentObject;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.xpack.core.ml.job.messages.Messages;
import org.elasticsearch.xpack.core.ml.utils.ExceptionsHelper;
import org.elasticsearch.xpack.core.common.time.TimeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

/**
 * Autodetect analysis configuration options describes which fields are
 * analysed and the functions to use.
 * <p>
 * The configuration can contain multiple detectors, a new anomaly detector will
 * be created for each detector configuration. The fields
 * <code>bucketSpan, summaryCountFieldName and categorizationFieldName</code>
 * apply to all detectors.
 * <p>
 * If a value has not been set it will be <code>null</code>
 * Object wrappers are used around integral types &amp; booleans so they can take
 * <code>null</code> values.
 */
public class AnalysisConfig implements ToXContentObject, Writeable {
    /**
     * Serialisation names
     */
    public static final ParseField ANALYSIS_CONFIG = new ParseField("analysis_config");
    public static final ParseField BUCKET_SPAN = new ParseField("bucket_span");
    public static final ParseField CATEGORIZATION_FIELD_NAME = new ParseField("categorization_field_name");
    public static final ParseField CATEGORIZATION_FILTERS = new ParseField("categorization_filters");
    public static final ParseField CATEGORIZATION_ANALYZER = CategorizationAnalyzerConfig.CATEGORIZATION_ANALYZER;
    public static final ParseField LATENCY = new ParseField("latency");
    public static final ParseField SUMMARY_COUNT_FIELD_NAME = new ParseField("summary_count_field_name");
    public static final ParseField DETECTORS = new ParseField("detectors");
    public static final ParseField INFLUENCERS = new ParseField("influencers");
    public static final ParseField MULTIVARIATE_BY_FIELDS = new ParseField("multivariate_by_fields");

    public static final String ML_CATEGORY_FIELD = "mlcategory";
    public static final Set<String> AUTO_CREATED_FIELDS = new HashSet<>(Collections.singletonList(ML_CATEGORY_FIELD));

    // These parsers follow the pattern that metadata is parsed leniently (to allow for enhancements), whilst config is parsed strictly
    public static final ConstructingObjectParser<AnalysisConfig.Builder, Void> LENIENT_PARSER = createParser(true);
    public static final ConstructingObjectParser<AnalysisConfig.Builder, Void> STRICT_PARSER = createParser(false);

    @SuppressWarnings("unchecked")
    private static ConstructingObjectParser<AnalysisConfig.Builder, Void> createParser(boolean ignoreUnknownFields) {
        ConstructingObjectParser<AnalysisConfig.Builder, Void> parser = new ConstructingObjectParser<>(ANALYSIS_CONFIG.getPreferredName(),
            ignoreUnknownFields, a -> new AnalysisConfig.Builder((List<Detector>) a[0]));

        parser.declareObjectArray(ConstructingObjectParser.constructorArg(),
            (p, c) -> (ignoreUnknownFields ? Detector.LENIENT_PARSER : Detector.STRICT_PARSER).apply(p, c).build(), DETECTORS);
        parser.declareString((builder, val) ->
            builder.setBucketSpan(TimeValue.parseTimeValue(val, BUCKET_SPAN.getPreferredName())), BUCKET_SPAN);
        parser.declareString(Builder::setCategorizationFieldName, CATEGORIZATION_FIELD_NAME);
        parser.declareStringArray(Builder::setCategorizationFilters, CATEGORIZATION_FILTERS);
        // This one is nasty - the syntax for analyzers takes either names or objects at many levels, hence it's not
        // possible to simply declare whether the field is a string or object and a completely custom parser is required
        parser.declareField(Builder::setCategorizationAnalyzerConfig,
            (p, c) -> CategorizationAnalyzerConfig.buildFromXContentFragment(p, ignoreUnknownFields),
            CATEGORIZATION_ANALYZER, ObjectParser.ValueType.OBJECT_OR_STRING);
        parser.declareString((builder, val) ->
            builder.setLatency(TimeValue.parseTimeValue(val, LATENCY.getPreferredName())), LATENCY);
        parser.declareString(Builder::setSummaryCountFieldName, SUMMARY_COUNT_FIELD_NAME);
        parser.declareStringArray(Builder::setInfluencers, INFLUENCERS);
        parser.declareBoolean(Builder::setMultivariateByFields, MULTIVARIATE_BY_FIELDS);

        return parser;
    }

    /**
     * These values apply to all detectors
     */
    private final TimeValue bucketSpan;
    private final String categorizationFieldName;
    private final List<String> categorizationFilters;
    private final CategorizationAnalyzerConfig categorizationAnalyzerConfig;
    private final TimeValue latency;
    private final String summaryCountFieldName;
    private final List<Detector> detectors;
    private final List<String> influencers;
    private final Boolean multivariateByFields;

    private AnalysisConfig(TimeValue bucketSpan, String categorizationFieldName, List<String> categorizationFilters,
                           CategorizationAnalyzerConfig categorizationAnalyzerConfig, TimeValue latency, String summaryCountFieldName,
                           List<Detector> detectors, List<String> influencers, Boolean multivariateByFields) {
        this.detectors = detectors;
        this.bucketSpan = bucketSpan;
        this.latency = latency;
        this.categorizationFieldName = categorizationFieldName;
        this.categorizationAnalyzerConfig = categorizationAnalyzerConfig;
        this.categorizationFilters = categorizationFilters == null ? null : Collections.unmodifiableList(categorizationFilters);
        this.summaryCountFieldName = summaryCountFieldName;
        this.influencers = Collections.unmodifiableList(influencers);
        this.multivariateByFields = multivariateByFields;
    }

    public AnalysisConfig(StreamInput in) throws IOException {
        bucketSpan = in.readTimeValue();
        categorizationFieldName = in.readOptionalString();
        categorizationFilters = in.readBoolean() ? Collections.unmodifiableList(in.readStringList()) : null;
        if (in.getVersion().onOrAfter(Version.V_6_2_0)) {
            categorizationAnalyzerConfig = in.readOptionalWriteable(CategorizationAnalyzerConfig::new);
        } else {
            categorizationAnalyzerConfig = null;
        }
        latency = in.readOptionalTimeValue();
        summaryCountFieldName = in.readOptionalString();
        detectors = Collections.unmodifiableList(in.readList(Detector::new));
        influencers = Collections.unmodifiableList(in.readStringList());

        multivariateByFields = in.readOptionalBoolean();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeTimeValue(bucketSpan);
        out.writeOptionalString(categorizationFieldName);
        if (categorizationFilters != null) {
            out.writeBoolean(true);
            out.writeStringCollection(categorizationFilters);
        } else {
            out.writeBoolean(false);
        }
        if (out.getVersion().onOrAfter(Version.V_6_2_0)) {
            out.writeOptionalWriteable(categorizationAnalyzerConfig);
        }
        out.writeOptionalTimeValue(latency);
        out.writeOptionalString(summaryCountFieldName);
        out.writeList(detectors);
        out.writeStringCollection(influencers);

        out.writeOptionalBoolean(multivariateByFields);
    }

    /**
     * The analysis bucket span
     *
     * @return The bucketspan or <code>null</code> if not set
     */
    public TimeValue getBucketSpan() {
        return bucketSpan;
    }

    public String getCategorizationFieldName() {
        return categorizationFieldName;
    }

    public List<String> getCategorizationFilters() {
        return categorizationFilters;
    }

    public CategorizationAnalyzerConfig getCategorizationAnalyzerConfig() {
        return categorizationAnalyzerConfig;
    }

    /**
     * The latency interval during which out-of-order records should be handled.
     *
     * @return The latency interval or <code>null</code> if not set
     */
    public TimeValue getLatency() {
        return latency;
    }

    /**
     * The name of the field that contains counts for pre-summarised input
     *
     * @return The field name or <code>null</code> if not set
     */
    public String getSummaryCountFieldName() {
        return summaryCountFieldName;
    }

    /**
     * The list of analysis detectors. In a valid configuration the list should
     * contain at least 1 {@link Detector}
     *
     * @return The Detectors used in this job
     */
    public List<Detector> getDetectors() {
        return detectors;
    }

    /**
     * The list of influence field names
     */
    public List<String> getInfluencers() {
        return influencers;
    }

    /**
     * Return the list of term fields.
     * These are the influencer fields, partition field,
     * by field and over field of each detector.
     * <code>null</code> and empty strings are filtered from the
     * config.
     *
     * @return Set of term fields - never <code>null</code>
     */
    public Set<String> termFields() {
        return termFields(getDetectors(), getInfluencers());
    }

    static SortedSet<String> termFields(List<Detector> detectors, List<String> influencers) {
        SortedSet<String> termFields = new TreeSet<>();

        detectors.forEach(d -> termFields.addAll(d.getByOverPartitionTerms()));

        for (String i : influencers) {
            addIfNotNull(termFields, i);
        }

        // remove empty strings
        termFields.remove("");

        return termFields;
    }

    public Set<String> extractReferencedFilters() {
        return detectors.stream().map(Detector::extractReferencedFilters)
                .flatMap(Set::stream).collect(Collectors.toSet());
    }

    public Boolean getMultivariateByFields() {
        return multivariateByFields;
    }

    /**
     * Return the set of fields required by the analysis.
     * These are the influencer fields, metric field, partition field,
     * by field and over field of each detector, plus the summary count
     * field and the categorization field name of the job.
     * <code>null</code> and empty strings are filtered from the
     * config.
     *
     * @return Set of required analysis fields - never <code>null</code>
     */
    public Set<String> analysisFields() {
        Set<String> analysisFields = termFields();

        addIfNotNull(analysisFields, categorizationFieldName);
        addIfNotNull(analysisFields, summaryCountFieldName);

        for (Detector d : getDetectors()) {
            addIfNotNull(analysisFields, d.getFieldName());
        }

        // remove empty strings
        analysisFields.remove("");

        return analysisFields;
    }

    private static void addIfNotNull(Set<String> fields, String field) {
        if (field != null) {
            fields.add(field);
        }
    }

    public List<String> fields() {
        return collectNonNullAndNonEmptyDetectorFields(Detector::getFieldName);
    }

    private List<String> collectNonNullAndNonEmptyDetectorFields(
            Function<Detector, String> fieldGetter) {
        Set<String> fields = new HashSet<>();

        for (Detector d : getDetectors()) {
            addIfNotNull(fields, fieldGetter.apply(d));
        }

        // remove empty strings
        fields.remove("");

        return new ArrayList<>(fields);
    }

    public List<String> byFields() {
        return collectNonNullAndNonEmptyDetectorFields(Detector::getByFieldName);
    }

    public List<String> overFields() {
        return collectNonNullAndNonEmptyDetectorFields(Detector::getOverFieldName);
    }


    public List<String> partitionFields() {
        return collectNonNullAndNonEmptyDetectorFields(Detector::getPartitionFieldName);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();
        builder.field(BUCKET_SPAN.getPreferredName(), bucketSpan.getStringRep());
        if (categorizationFieldName != null) {
            builder.field(CATEGORIZATION_FIELD_NAME.getPreferredName(), categorizationFieldName);
        }
        if (categorizationFilters != null) {
            builder.field(CATEGORIZATION_FILTERS.getPreferredName(), categorizationFilters);
        }
        if (categorizationAnalyzerConfig != null) {
            // This cannot be builder.field(CATEGORIZATION_ANALYZER.getPreferredName(), categorizationAnalyzerConfig, params);
            // because that always writes categorizationAnalyzerConfig as an object, and in the case of a global analyzer it
            // gets written as a single string.
            categorizationAnalyzerConfig.toXContent(builder, params);
        }
        if (latency != null) {
            builder.field(LATENCY.getPreferredName(), latency.getStringRep());
        }
        if (summaryCountFieldName != null) {
            builder.field(SUMMARY_COUNT_FIELD_NAME.getPreferredName(), summaryCountFieldName);
        }
        builder.startArray(DETECTORS.getPreferredName());
        for (Detector detector: detectors) {
            detector.toXContent(builder, params);
        }
        builder.endArray();
        builder.field(INFLUENCERS.getPreferredName(), influencers);
        if (multivariateByFields != null) {
            builder.field(MULTIVARIATE_BY_FIELDS.getPreferredName(), multivariateByFields);
        }
        builder.endObject();
        return builder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnalysisConfig that = (AnalysisConfig) o;
        return Objects.equals(latency, that.latency) &&
                Objects.equals(bucketSpan, that.bucketSpan) &&
                Objects.equals(categorizationFieldName, that.categorizationFieldName) &&
                Objects.equals(categorizationFilters, that.categorizationFilters) &&
                Objects.equals(categorizationAnalyzerConfig, that.categorizationAnalyzerConfig) &&
                Objects.equals(summaryCountFieldName, that.summaryCountFieldName) &&
                Objects.equals(detectors, that.detectors) &&
                Objects.equals(influencers, that.influencers) &&
                Objects.equals(multivariateByFields, that.multivariateByFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                bucketSpan, categorizationFieldName, categorizationFilters, categorizationAnalyzerConfig, latency,
                summaryCountFieldName, detectors, influencers, multivariateByFields);
    }

    public static class Builder {

        public static final TimeValue DEFAULT_BUCKET_SPAN = TimeValue.timeValueMinutes(5);

        private List<Detector> detectors;
        private TimeValue bucketSpan = DEFAULT_BUCKET_SPAN;
        private TimeValue latency;
        private String categorizationFieldName;
        private List<String> categorizationFilters;
        private CategorizationAnalyzerConfig categorizationAnalyzerConfig;
        private String summaryCountFieldName;
        private List<String> influencers = new ArrayList<>();
        private Boolean multivariateByFields;

        public Builder(List<Detector> detectors) {
            setDetectors(detectors);
        }

        public Builder(AnalysisConfig analysisConfig) {
            this.detectors = new ArrayList<>(analysisConfig.detectors);
            this.bucketSpan = analysisConfig.bucketSpan;
            this.latency = analysisConfig.latency;
            this.categorizationFieldName = analysisConfig.categorizationFieldName;
            this.categorizationFilters = analysisConfig.categorizationFilters == null ? null
                    : new ArrayList<>(analysisConfig.categorizationFilters);
            this.categorizationAnalyzerConfig = analysisConfig.categorizationAnalyzerConfig;
            this.summaryCountFieldName = analysisConfig.summaryCountFieldName;
            this.influencers = new ArrayList<>(analysisConfig.influencers);
            this.multivariateByFields = analysisConfig.multivariateByFields;
        }

        public Builder setDetectors(List<Detector> detectors) {
            if (detectors == null) {
                this.detectors = null;
                return this;
            }
            // We always assign sequential IDs to the detectors that are correct for this analysis config
            int detectorIndex = 0;
            List<Detector> sequentialIndexDetectors = new ArrayList<>(detectors.size());
            for (Detector origDetector : detectors) {
                Detector.Builder builder = new Detector.Builder(origDetector);
                builder.setDetectorIndex(detectorIndex++);
                sequentialIndexDetectors.add(builder.build());
            }
            this.detectors = sequentialIndexDetectors;
            return this;
        }

        public Builder setDetector(int detectorIndex, Detector detector) {
            detectors.set(detectorIndex, detector);
            return this;
        }

        public Builder setBucketSpan(TimeValue bucketSpan) {
            this.bucketSpan = bucketSpan;
            return this;
        }

        public Builder setLatency(TimeValue latency) {
            this.latency = latency;
            return this;
        }

        public Builder setCategorizationFieldName(String categorizationFieldName) {
            this.categorizationFieldName = categorizationFieldName;
            return this;
        }

        public Builder setCategorizationFilters(List<String> categorizationFilters) {
            this.categorizationFilters = categorizationFilters;
            return this;
        }

        public Builder setCategorizationAnalyzerConfig(CategorizationAnalyzerConfig categorizationAnalyzerConfig) {
            this.categorizationAnalyzerConfig = categorizationAnalyzerConfig;
            return this;
        }

        public Builder setSummaryCountFieldName(String summaryCountFieldName) {
            this.summaryCountFieldName = summaryCountFieldName;
            return this;
        }

        public Builder setInfluencers(List<String> influencers) {
            this.influencers = ExceptionsHelper.requireNonNull(influencers, INFLUENCERS.getPreferredName());
            return this;
        }

        public Builder setMultivariateByFields(Boolean multivariateByFields) {
            this.multivariateByFields = multivariateByFields;
            return this;
        }

        /**
         * Checks the configuration is valid
         * <ol>
         * <li>Check that if non-null BucketSpan and Latency are &gt;= 0</li>
         * <li>Check that if non-null Latency is &lt;= MAX_LATENCY</li>
         * <li>Check there is at least one detector configured</li>
         * <li>Check all the detectors are configured correctly</li>
         * <li>Check that MULTIPLE_BUCKETSPANS are set appropriately</li>
         * <li>If Per Partition normalization is configured at least one detector
         * must have a partition field and no influences can be used</li>
         * </ol>
         */
        public AnalysisConfig build() {
            TimeUtils.checkPositiveMultiple(bucketSpan, TimeUnit.SECONDS, BUCKET_SPAN);
            if (latency != null) {
                TimeUtils.checkNonNegativeMultiple(latency, TimeUnit.SECONDS, LATENCY);
            }

            verifyDetectorAreDefined();
            Detector.Builder.verifyFieldName(summaryCountFieldName);
            Detector.Builder.verifyFieldName(categorizationFieldName);

            verifyMlCategoryIsUsedWhenCategorizationFieldNameIsSet();
            verifyCategorizationAnalyzer();
            verifyCategorizationFilters();

            verifyNoMetricFunctionsWhenSummaryCountFieldNameIsSet();

            verifyNoInconsistentNestedFieldNames();

            return new AnalysisConfig(bucketSpan, categorizationFieldName, categorizationFilters, categorizationAnalyzerConfig,
                    latency, summaryCountFieldName, detectors, influencers, multivariateByFields);
        }

        private void verifyNoMetricFunctionsWhenSummaryCountFieldNameIsSet() {
            if (Strings.isNullOrEmpty(summaryCountFieldName) == false &&
                    detectors.stream().anyMatch(d -> DetectorFunction.METRIC.equals(d.getFunction()))) {
                throw ExceptionsHelper.badRequestException(
                        Messages.getMessage(Messages.JOB_CONFIG_FUNCTION_INCOMPATIBLE_PRESUMMARIZED, DetectorFunction.METRIC));
            }
        }

        private void verifyDetectorAreDefined() {
            if (detectors == null || detectors.isEmpty()) {
                throw ExceptionsHelper.badRequestException(Messages.getMessage(Messages.JOB_CONFIG_NO_DETECTORS));
            }
        }

        private void verifyNoInconsistentNestedFieldNames() {
            SortedSet<String> termFields = termFields(detectors, influencers);
            // We want to outlaw nested fields where a less nested field clashes with one of the nested levels.
            // For example, this is not allowed:
            // - a
            // - a.b
            // Nor is this:
            // - a.b
            // - a.b.c
            // But this is OK:
            // - a.b
            // - a.c
            // The sorted set makes it relatively easy to detect the situations we want to avoid.
            String prevTermField = null;
            for (String termField : termFields) {
                if (prevTermField != null && termField.startsWith(prevTermField + ".")) {
                    throw ExceptionsHelper.badRequestException("Fields [" + prevTermField + "] and [" + termField +
                            "] cannot both be used in the same analysis_config");
                }
                prevTermField = termField;
            }
        }

        private void verifyMlCategoryIsUsedWhenCategorizationFieldNameIsSet() {
            Set<String> byOverPartitionFields = new TreeSet<>();
            detectors.forEach(d -> byOverPartitionFields.addAll(d.getByOverPartitionTerms()));
            boolean isMlCategoryUsed = byOverPartitionFields.contains(ML_CATEGORY_FIELD);
            if (isMlCategoryUsed && categorizationFieldName == null) {
                throw ExceptionsHelper.badRequestException(CATEGORIZATION_FIELD_NAME.getPreferredName()
                        + " must be set for " + ML_CATEGORY_FIELD + " to be available");
            }
            if (categorizationFieldName != null && isMlCategoryUsed == false) {
                throw ExceptionsHelper.badRequestException(CATEGORIZATION_FIELD_NAME.getPreferredName()
                        + " is set but " + ML_CATEGORY_FIELD + " is not used in any detector by/over/partition field");
            }
        }

        private void verifyCategorizationAnalyzer() {
            if (categorizationAnalyzerConfig == null) {
                return;
            }

            verifyCategorizationFieldNameSetIfAnalyzerIsSet();
        }

        private void verifyCategorizationFieldNameSetIfAnalyzerIsSet() {
            if (categorizationFieldName == null) {
                throw ExceptionsHelper.badRequestException(Messages.getMessage(
                        Messages.JOB_CONFIG_CATEGORIZATION_ANALYZER_REQUIRES_CATEGORIZATION_FIELD_NAME));
            }
        }

        private void verifyCategorizationFilters() {
            if (categorizationFilters == null || categorizationFilters.isEmpty()) {
                return;
            }

            verifyCategorizationAnalyzerNotSetIfFiltersAreSet();
            verifyCategorizationFieldNameSetIfFiltersAreSet();
            verifyCategorizationFiltersAreDistinct();
            verifyCategorizationFiltersContainNoneEmpty();
            verifyCategorizationFiltersAreValidRegex();
        }

        private void verifyCategorizationAnalyzerNotSetIfFiltersAreSet() {
            if (categorizationAnalyzerConfig != null) {
                throw ExceptionsHelper.badRequestException(Messages.getMessage(
                        Messages.JOB_CONFIG_CATEGORIZATION_FILTERS_INCOMPATIBLE_WITH_CATEGORIZATION_ANALYZER));
            }
        }

        private void verifyCategorizationFieldNameSetIfFiltersAreSet() {
            if (categorizationFieldName == null) {
                throw ExceptionsHelper.badRequestException(Messages.getMessage(
                        Messages.JOB_CONFIG_CATEGORIZATION_FILTERS_REQUIRE_CATEGORIZATION_FIELD_NAME));
            }
        }

        private void verifyCategorizationFiltersAreDistinct() {
            if (categorizationFilters.stream().distinct().count() != categorizationFilters.size()) {
                throw ExceptionsHelper.badRequestException(
                        Messages.getMessage(Messages.JOB_CONFIG_CATEGORIZATION_FILTERS_CONTAINS_DUPLICATES));
            }
        }

        private void verifyCategorizationFiltersContainNoneEmpty() {
            if (categorizationFilters.stream().anyMatch(String::isEmpty)) {
                throw ExceptionsHelper.badRequestException(Messages.getMessage(Messages.JOB_CONFIG_CATEGORIZATION_FILTERS_CONTAINS_EMPTY));
            }
        }

        private void verifyCategorizationFiltersAreValidRegex() {
            for (String filter : categorizationFilters) {
                if (!isValidRegex(filter)) {
                    throw ExceptionsHelper.badRequestException(
                            Messages.getMessage(Messages.JOB_CONFIG_CATEGORIZATION_FILTERS_CONTAINS_INVALID_REGEX, filter));
                }
            }
        }

        private static boolean isValidRegex(String exp) {
            try {
                Pattern.compile(exp);
                return true;
            } catch (PatternSyntaxException e) {
                return false;
            }
        }
    }
}
