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

package org.elasticsearch.client.security.user.privileges;

import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.ConstructingObjectParser;
import org.elasticsearch.common.xcontent.ObjectParser;
import org.elasticsearch.common.xcontent.ToXContentObject;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;

import java.io.IOException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.elasticsearch.common.xcontent.ConstructingObjectParser.constructorArg;
import static org.elasticsearch.common.xcontent.ConstructingObjectParser.optionalConstructorArg;

/**
 * Represents an application specific privilege. The application name, privilege name,
 * actions and metadata are completely managed by the client and can contain arbitrary
 * string values.
 */
public final class ApplicationPrivilege implements ToXContentObject {

    private static final ParseField APPLICATION = new ParseField("application");
    private static final ParseField NAME = new ParseField("name");
    private static final ParseField ACTIONS = new ParseField("actions");
    private static final ParseField METADATA = new ParseField("metadata");

    private final String application;
    private final String name;
    private final Set<String> actions;
    private final Map<String, Object> metadata;

    public ApplicationPrivilege(String application, String name, Collection<String> actions, @Nullable Map<String, Object> metadata) {
        if (Strings.isNullOrEmpty(application)) {
            throw new IllegalArgumentException("application name must be provided");
        } else {
            this.application = application;
        }
        if (Strings.isNullOrEmpty(name)) {
            throw new IllegalArgumentException("privilege name must be provided");
        } else {
            this.name = name;
        }
        if (actions == null || actions.isEmpty()) {
            throw new IllegalArgumentException("actions must be provided");
        } else {
            this.actions = Collections.unmodifiableSet(new HashSet<>(actions));
        }
        if (metadata == null || metadata.isEmpty()) {
            this.metadata = Collections.emptyMap();
        } else {
            this.metadata = Collections.unmodifiableMap(metadata);
        }
    }

    public String getApplication() {
        return application;
    }

    public String getName() {
        return name;
    }

    public Set<String> getActions() {
        return actions;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    @SuppressWarnings("unchecked")
    public static final ConstructingObjectParser<ApplicationPrivilege, String> PARSER = new ConstructingObjectParser<>(
        "application_privilege",
        true, args -> new ApplicationPrivilege((String) args[0], (String) args[1], (Collection<String>) args[2],
        (Map<String, Object>) args[3]));

    static {
        PARSER.declareString(constructorArg(), APPLICATION);
        PARSER.declareString(constructorArg(), NAME);
        PARSER.declareStringArray(constructorArg(), ACTIONS);
        PARSER.declareField(optionalConstructorArg(), XContentParser::map, ApplicationPrivilege.METADATA, ObjectParser.ValueType.OBJECT);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationPrivilege that = (ApplicationPrivilege) o;
        return Objects.equals(application, that.application) &&
            Objects.equals(name, that.name) &&
            Objects.equals(actions, that.actions) &&
            Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(application, name, actions, metadata);
    }

    static ApplicationPrivilege fromXContent(XContentParser parser) throws IOException {
        return PARSER.parse(parser, null);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String applicationName = null;
        private String privilegeName = null;
        private Collection<String> actions = null;
        private Map<String, Object> metadata = null;

        private Builder() {
        }

        public Builder application(String applicationName) {
            this.applicationName = Objects.requireNonNull(applicationName, "application name must be provided");
            return this;
        }

        public Builder privilege(String privilegeName) {
            this.privilegeName = Objects.requireNonNull(privilegeName, "privilege name must be provided");
            return this;
        }

        public Builder actions(String... actions) {
            this.actions = Arrays.asList(Objects.requireNonNull(actions));
            return this;
        }

        public Builder actions(Collection<String> actions) {
            this.actions = Objects.requireNonNull(actions);
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public ApplicationPrivilege build() {
            return new ApplicationPrivilege(applicationName, privilegeName, actions, metadata);
        }
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject()
        .field(APPLICATION.getPreferredName(), application)
        .field(NAME.getPreferredName(), name)
        .field(ACTIONS.getPreferredName(), actions);
        if (metadata != null && metadata.isEmpty() == false) {
            builder.field(METADATA.getPreferredName(), metadata);
        }
        return builder.endObject();
    }

}
