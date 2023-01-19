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

import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.ConstructingObjectParser;
import org.elasticsearch.common.xcontent.ToXContentObject;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static org.elasticsearch.common.xcontent.ConstructingObjectParser.constructorArg;

/**
 * Represents privileges over resources that are scoped under an application.
 * The application, resources and privileges are completely managed by the
 * client and can be arbitrary string identifiers. Elasticsearch is not
 * concerned by any resources under an application scope.
 */
public final class ApplicationResourcePrivileges implements ToXContentObject {

    private static final ParseField APPLICATION = new ParseField("application");
    private static final ParseField PRIVILEGES = new ParseField("privileges");
    private static final ParseField RESOURCES = new ParseField("resources");

    @SuppressWarnings("unchecked")
    static final ConstructingObjectParser<ApplicationResourcePrivileges, Void> PARSER = new ConstructingObjectParser<>(
            "application_privileges", false, constructorObjects -> {
                // Don't ignore unknown fields. It is dangerous if the object we parse is also
                // part of a request that we build later on, and the fields that we now ignore will
                // end up being implicitly set to null in that request.
                int i = 0;
                final String application = (String) constructorObjects[i++];
                final Collection<String> privileges = (Collection<String>) constructorObjects[i++];
                final Collection<String> resources = (Collection<String>) constructorObjects[i];
                return new ApplicationResourcePrivileges(application, privileges, resources);
            });

    static {
        PARSER.declareString(constructorArg(), APPLICATION);
        PARSER.declareStringArray(constructorArg(), PRIVILEGES);
        PARSER.declareStringArray(constructorArg(), RESOURCES);
    }

    private final String application;
    private final Set<String> privileges;
    private final Set<String> resources;

    /**
     * Constructs privileges for resources under an application scope.
     * 
     * @param application
     *            The application name. This identifier is completely under the
     *            clients control.
     * @param privileges
     *            The privileges names. Cannot be null or empty. Privilege
     *            identifiers are completely under the clients control.
     * @param resources
     *            The resources names. Cannot be null or empty. Resource identifiers
     *            are completely under the clients control.
     */
    public ApplicationResourcePrivileges(String application, Collection<String> privileges, Collection<String> resources) {
        if (Strings.isNullOrEmpty(application)) {
            throw new IllegalArgumentException("application privileges must have an application name");
        }
        if (null == privileges || privileges.isEmpty()) {
            throw new IllegalArgumentException("application privileges must define at least one privilege");
        }
        if (null == resources || resources.isEmpty()) {
            throw new IllegalArgumentException("application privileges must refer to at least one resource");
        }
        this.application = application;
        this.privileges = Collections.unmodifiableSet(new HashSet<>(privileges));
        this.resources = Collections.unmodifiableSet(new HashSet<>(resources));
    }

    public String getApplication() {
        return application;
    }

    public Set<String> getResources() {
        return this.resources;
    }

    public Set<String> getPrivileges() {
        return this.privileges;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        ApplicationResourcePrivileges that = (ApplicationResourcePrivileges) o;
        return application.equals(that.application)
                && privileges.equals(that.privileges)
                && resources.equals(that.resources);
    }

    @Override
    public int hashCode() {
        return Objects.hash(application, privileges, resources);
    }

    @Override
    public String toString() {
        try {
            return XContentHelper.toXContent(this, XContentType.JSON, true).utf8ToString();
        } catch (IOException e) {
            throw new RuntimeException("Unexpected", e);
        }
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();
        builder.field(APPLICATION.getPreferredName(), application);
        builder.field(PRIVILEGES.getPreferredName(), privileges);
        builder.field(RESOURCES.getPreferredName(), resources);
        return builder.endObject();
    }

    public static ApplicationResourcePrivileges fromXContent(XContentParser parser) {
        return PARSER.apply(parser, null);
    }

}