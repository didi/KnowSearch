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

import org.elasticsearch.client.Validatable;
import org.elasticsearch.client.ValidationException;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.xcontent.ConstructingObjectParser;
import org.elasticsearch.common.xcontent.ToXContentObject;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.elasticsearch.common.xcontent.ConstructingObjectParser.constructorArg;

/**
 * The configuration object for the histograms in the rollup config
 *
 * {
 *     "groups": [
 *        "terms": {
 *            "fields" : [ "foo", "bar" ]
 *        }
 *     ]
 * }
 */
public class TermsGroupConfig implements Validatable, ToXContentObject {

    static final String NAME = "terms";
    private static final String FIELDS = "fields";

    private static final ConstructingObjectParser<TermsGroupConfig, Void> PARSER;
    static {
        PARSER = new ConstructingObjectParser<>(NAME, true, args -> {
            @SuppressWarnings("unchecked") List<String> fields = (List<String>) args[0];
            return new TermsGroupConfig(fields != null ? fields.toArray(new String[fields.size()]) : null);
        });
        PARSER.declareStringArray(constructorArg(), new ParseField(FIELDS));
    }

    private final String[] fields;

    public TermsGroupConfig(final String... fields) {
        this.fields = fields;
    }

    @Override
    public Optional<ValidationException> validate() {
        final ValidationException validationException = new ValidationException();
        if (fields == null || fields.length == 0) {
            validationException.addValidationError("Fields must have at least one value");
        }
        if (validationException.validationErrors().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(validationException);
    }

    /**
     * @return the names of the fields. Never {@code null}.
     */
    public String[] getFields() {
        return fields;
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();
        {
            builder.field(FIELDS, fields);
        }
        return builder.endObject();
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        final TermsGroupConfig that = (TermsGroupConfig) other;
        return Arrays.equals(fields, that.fields);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(fields);
    }

    public static TermsGroupConfig fromXContent(final XContentParser parser) throws IOException {
        return PARSER.parse(parser, null);
    }
}
