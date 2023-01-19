/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.core.ilm;

import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.ObjectParser;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * A {@link LifecycleAction} which sets the index to be read-only.
 */
public class ReadOnlyAction implements LifecycleAction {
    public static final String NAME = "readonly";

    private static final ObjectParser<ReadOnlyAction, Void> PARSER = new ObjectParser<>(NAME, false, ReadOnlyAction::new);

    public static ReadOnlyAction parse(XContentParser parser) {
        return PARSER.apply(parser, null);
    }

    public ReadOnlyAction() {
    }

    public ReadOnlyAction(StreamInput in) {
    }

    @Override
    public String getWriteableName() {
        return NAME;
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();
        builder.endObject();
        return builder;
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
    }

    @Override
    public boolean isSafeAction() {
        return true;
    }

    @Override
    public List<Step> toSteps(Client client, String phase, Step.StepKey nextStepKey) {
        Step.StepKey key = new Step.StepKey(phase, NAME, NAME);
        Settings readOnlySettings = Settings.builder().put(IndexMetaData.SETTING_BLOCKS_WRITE, true).build();
        return Collections.singletonList(new UpdateSettingsStep(key, nextStepKey, client, readOnlySettings));
    }

    @Override
    public int hashCode() {
        return ReadOnlyAction.class.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return Strings.toString(this);
    }
}
