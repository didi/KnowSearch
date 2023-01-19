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

package org.elasticsearch.client.security.user;

import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.Strings;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * A user to be utilized with security APIs.
 * Can be an existing authenticated user or it can be a new user to be enrolled to the native realm.
 */
public final class User {

    private final String username;
    private final Set<String> roles;
    private final Map<String, Object> metadata;
    @Nullable private final String fullName;
    @Nullable private final String email;

    /**
     * Builds the user to be utilized with security APIs.
     *
     * @param username the username, also known as the principal, unique for in the scope of a realm
     * @param roles the roles that this user is assigned
     * @param metadata a map of additional user attributes that may be used in templating roles
     * @param fullName the full name of the user that may be used for display purposes
     * @param email the email address of the user
     */
    public User(String username, Collection<String> roles, Map<String, Object> metadata, @Nullable String fullName,
            @Nullable String email) {
        this.username = username = Objects.requireNonNull(username, "`username` is required, cannot be null");
        this.roles = Collections.unmodifiableSet(new HashSet<>(
                Objects.requireNonNull(roles, "`roles` is required, cannot be null. Pass an empty Collection instead.")));
        this.metadata = Collections
                .unmodifiableMap(Objects.requireNonNull(metadata, "`metadata` is required, cannot be null. Pass an empty map instead."));
        this.fullName = fullName;
        this.email = email;
    }

    /**
     * Builds the user to be utilized with security APIs.
     *
     * @param username the username, also known as the principal, unique for in the scope of a realm
     * @param roles the roles that this user is assigned
     */
    public User(String username, Collection<String> roles) {
        this(username, roles, Collections.emptyMap(), null, null);
    }

    /**
     * @return  The principal of this user - effectively serving as the
     *          unique identity of the user. Can never be {@code null}.
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * @return  The roles this user is associated with. The roles are
     *          identified by their unique names and each represents as
     *          set of permissions. Can never be {@code null}.
     */
    public Set<String> getRoles() {
        return this.roles;
    }

    /**
     * @return  The metadata that is associated with this user. Can never be {@code null}.
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * @return  The full name of this user. May be {@code null}.
     */
    public @Nullable String getFullName() {
        return fullName;
    }

    /**
     * @return  The email of this user. May be {@code null}.
     */
    public @Nullable String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("User[username=").append(username);
        sb.append(",roles=[").append(Strings.collectionToCommaDelimitedString(roles)).append("]");
        sb.append(",metadata=").append(metadata);
        sb.append(",fullName=").append(fullName);
        sb.append(",email=").append(email);
        sb.append("]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        final User that = (User) o;
        return Objects.equals(username, that.username)
                && Objects.equals(roles, that.roles)
                && Objects.equals(metadata, that.metadata)
                && Objects.equals(fullName, that.fullName)
                && Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, roles, metadata, fullName, email);
    }

}
