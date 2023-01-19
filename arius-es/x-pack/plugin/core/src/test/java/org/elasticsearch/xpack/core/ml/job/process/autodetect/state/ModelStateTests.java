/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.core.ml.job.process.autodetect.state;

import org.elasticsearch.test.ESTestCase;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

public class ModelStateTests extends ESTestCase {

    public void testExtractJobId_GivenValidDocId() {
        assertThat(ModelState.extractJobId("foo_model_state_3151373783#1"), equalTo("foo"));
        assertThat(ModelState.extractJobId("bar_model_state_451515#3"), equalTo("bar"));
        assertThat(ModelState.extractJobId("foo_bar_model_state_blah_blah"), equalTo("foo_bar"));
        assertThat(ModelState.extractJobId("_model_state_model_state_11111"), equalTo("_model_state"));
    }

    public void testExtractJobId_GivenInvalidDocId() {
        assertThat(ModelState.extractJobId(""), is(nullValue()));
        assertThat(ModelState.extractJobId("foo"), is(nullValue()));
        assertThat(ModelState.extractJobId("_model_3141341341"), is(nullValue()));
        assertThat(ModelState.extractJobId("_state_3141341341"), is(nullValue()));
        assertThat(ModelState.extractJobId("_model_state_3141341341"), is(nullValue()));
        assertThat(ModelState.extractJobId("foo_quantiles"), is(nullValue()));
    }
}