/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */

package org.elasticsearch.xpack.core.ilm;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.Strings;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Contains information about the execution of a lifecycle policy for a single
 * index, and serializes/deserializes this information to and from custom
 * index metadata.
 */
public class LifecycleExecutionState {
    public static final String ILM_CUSTOM_METADATA_KEY = "ilm";

    private static final String PHASE = "phase";
    private static final String ACTION = "action";
    private static final String STEP = "step";
    private static final String INDEX_CREATION_DATE = "creation_date";
    private static final String PHASE_TIME = "phase_time";
    private static final String ACTION_TIME = "action_time";
    private static final String STEP_TIME = "step_time";
    private static final String FAILED_STEP = "failed_step";
    private static final String IS_AUTO_RETRYABLE_ERROR = "is_auto_retryable_error";
    private static final String FAILED_STEP_RETRY_COUNT = "failed_step_retry_count";
    private static final String STEP_INFO = "step_info";
    private static final String PHASE_DEFINITION = "phase_definition";

    private final String phase;
    private final String action;
    private final String step;
    private final String failedStep;
    private final Boolean isAutoRetryableError;
    private final Integer failedStepRetryCount;
    private final String stepInfo;
    private final String phaseDefinition;
    private final Long lifecycleDate;
    private final Long phaseTime;
    private final Long actionTime;
    private final Long stepTime;

    private LifecycleExecutionState(String phase, String action, String step, String failedStep, Boolean isAutoRetryableError,
                                    Integer failedStepRetryCount, String stepInfo, String phaseDefinition, Long lifecycleDate,
                                    Long phaseTime, Long actionTime, Long stepTime) {
        this.phase = phase;
        this.action = action;
        this.step = step;
        this.failedStep = failedStep;
        this.isAutoRetryableError = isAutoRetryableError;
        this.failedStepRetryCount = failedStepRetryCount;
        this.stepInfo = stepInfo;
        this.phaseDefinition = phaseDefinition;
        this.lifecycleDate = lifecycleDate;
        this.phaseTime = phaseTime;
        this.actionTime = actionTime;
        this.stepTime = stepTime;
    }

    /**
     * Retrieves the execution state from an {@link IndexMetaData} based on the
     * custom metadata.
     * @param indexMetaData The metadata of the index to retrieve the execution
     *                      state from.
     * @return The execution state of that index.
     */
    public static LifecycleExecutionState fromIndexMetadata(IndexMetaData indexMetaData) {
        Map<String, String> customData = indexMetaData.getCustomData(ILM_CUSTOM_METADATA_KEY);
        customData = customData == null ? new HashMap<>() : customData;
        return fromCustomMetadata(customData);
    }

    /**
     * Retrieves the current {@link Step.StepKey} from the lifecycle state. Note that
     * it is illegal for the step to be set with the phase and/or action unset,
     * or for the step to be unset with the phase and/or action set. All three
     * settings must be either present or missing.
     *
     * @param lifecycleState the index custom data to extract the {@link Step.StepKey} from.
     */
    @Nullable
    public static Step.StepKey getCurrentStepKey(LifecycleExecutionState lifecycleState) {
        Objects.requireNonNull(lifecycleState, "cannot determine current step key as lifecycle state is null");
        String currentPhase = lifecycleState.getPhase();
        String currentAction = lifecycleState.getAction();
        String currentStep = lifecycleState.getStep();
        if (Strings.isNullOrEmpty(currentStep)) {
            assert Strings.isNullOrEmpty(currentPhase) : "Current phase is not empty: " + currentPhase;
            assert Strings.isNullOrEmpty(currentAction) : "Current action is not empty: " + currentAction;
            return null;
        } else {
            assert Strings.isNullOrEmpty(currentPhase) == false;
            assert Strings.isNullOrEmpty(currentAction) == false;
            return new Step.StepKey(currentPhase, currentAction, currentStep);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(LifecycleExecutionState state) {
        return new Builder()
            .setPhase(state.phase)
            .setAction(state.action)
            .setStep(state.step)
            .setFailedStep(state.failedStep)
            .setIsAutoRetryableError(state.isAutoRetryableError)
            .setFailedStepRetryCount(state.failedStepRetryCount)
            .setStepInfo(state.stepInfo)
            .setPhaseDefinition(state.phaseDefinition)
            .setIndexCreationDate(state.lifecycleDate)
            .setPhaseTime(state.phaseTime)
            .setActionTime(state.actionTime)
            .setStepTime(state.stepTime);
    }

    static LifecycleExecutionState fromCustomMetadata(Map<String, String> customData) {
        Builder builder = builder();
        if (customData.containsKey(PHASE)) {
            builder.setPhase(customData.get(PHASE));
        }
        if (customData.containsKey(ACTION)) {
            builder.setAction(customData.get(ACTION));
        }
        if (customData.containsKey(STEP)) {
            builder.setStep(customData.get(STEP));
        }
        if (customData.containsKey(FAILED_STEP)) {
            builder.setFailedStep(customData.get(FAILED_STEP));
        }
        if (customData.containsKey(IS_AUTO_RETRYABLE_ERROR)) {
            builder.setIsAutoRetryableError(Boolean.parseBoolean(customData.get(IS_AUTO_RETRYABLE_ERROR)));
        }
        if (customData.containsKey(FAILED_STEP_RETRY_COUNT)) {
            builder.setFailedStepRetryCount(Integer.parseInt(customData.get(FAILED_STEP_RETRY_COUNT)));
        }
        if (customData.containsKey(STEP_INFO)) {
            builder.setStepInfo(customData.get(STEP_INFO));
        }
        if (customData.containsKey(PHASE_DEFINITION)) {
            builder.setPhaseDefinition(customData.get(PHASE_DEFINITION));
        }
        if (customData.containsKey(INDEX_CREATION_DATE)) {
            try {
                builder.setIndexCreationDate(Long.parseLong(customData.get(INDEX_CREATION_DATE)));
            } catch (NumberFormatException e) {
                throw new ElasticsearchException("Custom metadata field [{}] does not contain a valid long. Actual value: [{}]",
                    e, INDEX_CREATION_DATE, customData.get(INDEX_CREATION_DATE));
            }
        }
        if (customData.containsKey(PHASE_TIME)) {
            try {
                builder.setPhaseTime(Long.parseLong(customData.get(PHASE_TIME)));
            } catch (NumberFormatException e) {
                throw new ElasticsearchException("Custom metadata field [{}] does not contain a valid long. Actual value: [{}]",
                    e, PHASE_TIME, customData.get(PHASE_TIME));
            }
        }
        if (customData.containsKey(ACTION_TIME)) {
            try {
                builder.setActionTime(Long.parseLong(customData.get(ACTION_TIME)));
            } catch (NumberFormatException e) {
                throw new ElasticsearchException("Custom metadata field [{}] does not contain a valid long. Actual value: [{}]",
                    e, ACTION_TIME, customData.get(ACTION_TIME));
            }
        }
        if (customData.containsKey(STEP_TIME)) {
            try {
                builder.setStepTime(Long.parseLong(customData.get(STEP_TIME)));
            } catch (NumberFormatException e) {
                throw new ElasticsearchException("Custom metadata field [{}] does not contain a valid long. Actual value: [{}]",
                    e, STEP_TIME, customData.get(STEP_TIME));
            }
        }
        return builder.build();
    }

    /**
     * Converts this object to an immutable map representation for use with
     * {@link IndexMetaData.Builder#putCustom(String, Map)}.
     * @return An immutable Map representation of this execution state.
     */
    public Map<String, String> asMap() {
        Map<String, String> result = new HashMap<>();
        if (phase != null) {
            result.put(PHASE, phase);
        }
        if (action != null) {
            result.put(ACTION, action);
        }
        if (step != null) {
            result.put(STEP, step);
        }
        if (failedStep != null) {
            result.put(FAILED_STEP, failedStep);
        }
        if (isAutoRetryableError != null) {
            result.put(IS_AUTO_RETRYABLE_ERROR, String.valueOf(isAutoRetryableError));
        }
        if (failedStepRetryCount != null) {
            result.put(FAILED_STEP_RETRY_COUNT, String.valueOf(failedStepRetryCount));
        }
        if (stepInfo != null) {
            result.put(STEP_INFO, stepInfo);
        }
        if (lifecycleDate != null) {
            result.put(INDEX_CREATION_DATE, String.valueOf(lifecycleDate));
        }
        if (phaseTime != null) {
            result.put(PHASE_TIME, String.valueOf(phaseTime));
        }
        if (actionTime != null) {
            result.put(ACTION_TIME, String.valueOf(actionTime));
        }
        if (stepTime != null) {
            result.put(STEP_TIME, String.valueOf(stepTime));
        }
        if (phaseDefinition != null) {
            result.put(PHASE_DEFINITION, String.valueOf(phaseDefinition));
        }
        return Collections.unmodifiableMap(result);
    }

    public String getPhase() {
        return phase;
    }

    public String getAction() {
        return action;
    }

    public String getStep() {
        return step;
    }

    public String getFailedStep() {
        return failedStep;
    }

    public Boolean isAutoRetryableError() {
        return isAutoRetryableError;
    }

    public Integer getFailedStepRetryCount() {
        return failedStepRetryCount;
    }

    public String getStepInfo() {
        return stepInfo;
    }

    public String getPhaseDefinition() {
        return phaseDefinition;
    }

    public Long getLifecycleDate() {
        return lifecycleDate;
    }

    public Long getPhaseTime() {
        return phaseTime;
    }

    public Long getActionTime() {
        return actionTime;
    }

    public Long getStepTime() {
        return stepTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LifecycleExecutionState that = (LifecycleExecutionState) o;
        return Objects.equals(getLifecycleDate(), that.getLifecycleDate()) &&
            Objects.equals(getPhaseTime(), that.getPhaseTime()) &&
            Objects.equals(getActionTime(), that.getActionTime()) &&
            Objects.equals(getStepTime(), that.getStepTime()) &&
            Objects.equals(getPhase(), that.getPhase()) &&
            Objects.equals(getAction(), that.getAction()) &&
            Objects.equals(getStep(), that.getStep()) &&
            Objects.equals(getFailedStep(), that.getFailedStep()) &&
            Objects.equals(isAutoRetryableError(), that.isAutoRetryableError()) &&
            Objects.equals(getFailedStepRetryCount(), that.getFailedStepRetryCount()) &&
            Objects.equals(getStepInfo(), that.getStepInfo()) &&
            Objects.equals(getPhaseDefinition(), that.getPhaseDefinition());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPhase(), getAction(), getStep(), getFailedStep(), isAutoRetryableError(), getFailedStepRetryCount(),
            getStepInfo(), getPhaseDefinition(), getLifecycleDate(), getPhaseTime(), getActionTime(), getStepTime());
    }

    @Override
    public String toString() {
        return asMap().toString();
    }

    public static class Builder {
        private String phase;
        private String action;
        private String step;
        private String failedStep;
        private String stepInfo;
        private String phaseDefinition;
        private Long indexCreationDate;
        private Long phaseTime;
        private Long actionTime;
        private Long stepTime;
        private Boolean isAutoRetryableError;
        private Integer failedStepRetryCount;

        public Builder setPhase(String phase) {
            this.phase = phase;
            return this;
        }

        public Builder setAction(String action) {
            this.action = action;
            return this;
        }

        public Builder setStep(String step) {
            this.step = step;
            return this;
        }

        public Builder setFailedStep(String failedStep) {
            this.failedStep = failedStep;
            return this;
        }

        public Builder setStepInfo(String stepInfo) {
            this.stepInfo = stepInfo;
            return this;
        }

        public Builder setPhaseDefinition(String phaseDefinition) {
            this.phaseDefinition = phaseDefinition;
            return this;
        }

        public Builder setIndexCreationDate(Long indexCreationDate) {
            this.indexCreationDate = indexCreationDate;
            return this;
        }

        public Builder setPhaseTime(Long phaseTime) {
            this.phaseTime = phaseTime;
            return this;
        }

        public Builder setActionTime(Long actionTime) {
            this.actionTime = actionTime;
            return this;
        }

        public Builder setStepTime(Long stepTime) {
            this.stepTime = stepTime;
            return this;
        }

        public Builder setIsAutoRetryableError(Boolean isAutoRetryableError) {
            this.isAutoRetryableError = isAutoRetryableError;
            return this;
        }

        public Builder setFailedStepRetryCount(Integer failedStepRetryCount) {
            this.failedStepRetryCount = failedStepRetryCount;
            return this;
        }

        public LifecycleExecutionState build() {
            return new LifecycleExecutionState(phase, action, step, failedStep, isAutoRetryableError, failedStepRetryCount, stepInfo,
                phaseDefinition, indexCreationDate, phaseTime, actionTime, stepTime);
        }
    }

}
