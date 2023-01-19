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

import org.elasticsearch.client.security.GetUserPrivilegesResponse;
import org.elasticsearch.common.xcontent.ConstructingObjectParser;
import org.elasticsearch.common.xcontent.XContentParser;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.elasticsearch.common.xcontent.ConstructingObjectParser.constructorArg;
import static org.elasticsearch.common.xcontent.ConstructingObjectParser.optionalConstructorArg;

/**
 * Represents an "index" privilege in the {@link GetUserPrivilegesResponse}. This differs from the
 * {@link org.elasticsearch.client.security.user.privileges.IndicesPrivileges}" object in a
 * {@link org.elasticsearch.client.security.user.privileges.Role}
 * as it supports an array value for {@link #getFieldSecurity() field_security} and {@link #getQueries() query}.
 * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/security-api-get-user-privileges.html">the API docs</a>
 */
public class UserIndicesPrivileges extends AbstractIndicesPrivileges {

    private final Set<IndicesPrivileges.FieldSecurity> fieldSecurity;
    private final Set<String> query;

    private static final ConstructingObjectParser<UserIndicesPrivileges, Void> PARSER = new ConstructingObjectParser<>(
        "user_indices_privilege", true, UserIndicesPrivileges::buildObjectFromParserArgs);

    static {
        PARSER.declareStringArray(constructorArg(), IndicesPrivileges.NAMES);
        PARSER.declareStringArray(constructorArg(), IndicesPrivileges.PRIVILEGES);
        PARSER.declareBoolean(constructorArg(), IndicesPrivileges.ALLOW_RESTRICTED_INDICES);
        PARSER.declareObjectArray(optionalConstructorArg(), IndicesPrivileges.FieldSecurity::parse, IndicesPrivileges.FIELD_PERMISSIONS);
        PARSER.declareStringArray(optionalConstructorArg(), IndicesPrivileges.QUERY);
    }

    @SuppressWarnings("unchecked")
    private static UserIndicesPrivileges buildObjectFromParserArgs(Object[] args) {
        return new UserIndicesPrivileges(
            (List<String>) args[0],
            (List<String>) args[1],
            (Boolean) args[2],
            (List<IndicesPrivileges.FieldSecurity>) args[3],
            (List<String>) args[4]
        );
    }

    public static UserIndicesPrivileges fromXContent(XContentParser parser) throws IOException {
        return PARSER.parse(parser, null);
    }

    public UserIndicesPrivileges(Collection<String> indices, Collection<String> privileges, boolean allowRestrictedIndices,
                                 Collection<IndicesPrivileges.FieldSecurity> fieldSecurity, Collection<String> query) {
        super(indices, privileges, allowRestrictedIndices);
        this.fieldSecurity = fieldSecurity == null ? Collections.emptySet() : Collections.unmodifiableSet(new HashSet<>(fieldSecurity));
        this.query = query == null ? Collections.emptySet() : Collections.unmodifiableSet(new HashSet<>(query));
    }

    public Set<IndicesPrivileges.FieldSecurity> getFieldSecurity() {
        return fieldSecurity;
    }

    public Set<String> getQueries() {
        return query;
    }

    @Override
    public boolean isUsingDocumentLevelSecurity() {
        return query.isEmpty() == false;
    }

    @Override
    public boolean isUsingFieldLevelSecurity() {
        return fieldSecurity.stream().anyMatch(FieldSecurity::isUsingFieldLevelSecurity);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final UserIndicesPrivileges that = (UserIndicesPrivileges) o;
        return Objects.equals(indices, that.indices) &&
            Objects.equals(privileges, that.privileges) &&
            allowRestrictedIndices == that.allowRestrictedIndices &&
            Objects.equals(fieldSecurity, that.fieldSecurity) &&
            Objects.equals(query, that.query);
    }

    @Override
    public int hashCode() {
        return Objects.hash(indices, privileges, allowRestrictedIndices, fieldSecurity, query);
    }

    @Override
    public String toString() {
        return "UserIndexPrivilege{" +
            "indices=" + indices +
            ", privileges=" + privileges +
            ", allow_restricted_indices=" + allowRestrictedIndices +
            ", fieldSecurity=" + fieldSecurity +
            ", query=" + query +
            '}';
    }
}
