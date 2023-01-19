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
package org.elasticsearch.client.rollup.job.config;

import org.elasticsearch.client.ValidationException;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.test.AbstractXContentTestCase;

import java.io.IOException;
import java.util.Optional;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class HistogramGroupConfigTests extends AbstractXContentTestCase<HistogramGroupConfig> {

    @Override
    protected HistogramGroupConfig createTestInstance() {
        return randomHistogramGroupConfig();
    }

    @Override
    protected HistogramGroupConfig doParseInstance(final XContentParser parser) throws IOException {
        return HistogramGroupConfig.fromXContent(parser);
    }

    @Override
    protected boolean supportsUnknownFields() {
        return true;
    }

    public void testValidateNullFields() {
        final HistogramGroupConfig config = new HistogramGroupConfig(60L);

        Optional<ValidationException> validation = config.validate();
        assertThat(validation, notNullValue());
        assertThat(validation.isPresent(), is(true));
        ValidationException validationException = validation.get();
        assertThat(validationException.validationErrors().size(), is(1));
        assertThat(validationException.validationErrors(), contains(is("Fields must have at least one value")));
    }

    public void testValidatEmptyFields() {
        final HistogramGroupConfig config = new HistogramGroupConfig(60L, Strings.EMPTY_ARRAY);

        Optional<ValidationException> validation = config.validate();
        assertThat(validation, notNullValue());
        assertThat(validation.isPresent(), is(true));
        ValidationException validationException = validation.get();
        assertThat(validationException.validationErrors().size(), is(1));
        assertThat(validationException.validationErrors(), contains(is("Fields must have at least one value")));
    }

    public void testValidateNegativeInterval() {
        final HistogramGroupConfig config = new HistogramGroupConfig(-1L, randomHistogramGroupConfig().getFields());

        Optional<ValidationException> validation = config.validate();
        assertThat(validation, notNullValue());
        assertThat(validation.isPresent(), is(true));
        ValidationException validationException = validation.get();
        assertThat(validationException.validationErrors().size(), is(1));
        assertThat(validationException.validationErrors(), contains(is("Interval must be a positive long")));
    }

    public void testValidateZeroInterval() {
        final HistogramGroupConfig config = new HistogramGroupConfig(0L, randomHistogramGroupConfig().getFields());

        Optional<ValidationException> validation = config.validate();
        assertThat(validation, notNullValue());
        assertThat(validation.isPresent(), is(true));
        ValidationException validationException = validation.get();
        assertThat(validationException.validationErrors().size(), is(1));
        assertThat(validationException.validationErrors(), contains(is("Interval must be a positive long")));
    }

    public void testValidate() {
        final HistogramGroupConfig config = randomHistogramGroupConfig();

        Optional<ValidationException> validation = config.validate();
        assertThat(validation, notNullValue());
        assertThat(validation.isPresent(), is(false));
    }
    static HistogramGroupConfig randomHistogramGroupConfig() {
        final long interval = randomNonNegativeLong();
        final String[] fields = new String[randomIntBetween(1, 10)];
        for (int i = 0; i < fields.length; i++) {
            fields[i] = randomAlphaOfLength(randomIntBetween(3, 10));
        }
        return new HistogramGroupConfig(interval, fields);
    }
}
