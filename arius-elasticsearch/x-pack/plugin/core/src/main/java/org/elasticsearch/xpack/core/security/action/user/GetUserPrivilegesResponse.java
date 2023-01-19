/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.core.security.action.user;

import org.elasticsearch.Version;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.common.xcontent.ToXContentObject;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.xpack.core.security.authz.RoleDescriptor;
import org.elasticsearch.xpack.core.security.authz.permission.FieldPermissionsDefinition;
import org.elasticsearch.xpack.core.security.authz.privilege.ConfigurableClusterPrivilege;
import org.elasticsearch.xpack.core.security.authz.privilege.ConfigurableClusterPrivileges;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Response for a {@link GetUserPrivilegesRequest}
 */
public final class GetUserPrivilegesResponse extends ActionResponse {

    private Set<String> cluster;
    private Set<ConfigurableClusterPrivilege> configurableClusterPrivileges;
    private Set<Indices> index;
    private Set<RoleDescriptor.ApplicationResourcePrivileges> application;
    private Set<String> runAs;

    public GetUserPrivilegesResponse(StreamInput in) throws IOException {
        super(in);
        cluster = Collections.unmodifiableSet(in.readSet(StreamInput::readString));
        configurableClusterPrivileges = Collections.unmodifiableSet(in.readSet(ConfigurableClusterPrivileges.READER));
        index = Collections.unmodifiableSet(in.readSet(Indices::new));
        application = Collections.unmodifiableSet(in.readSet(RoleDescriptor.ApplicationResourcePrivileges::new));
        runAs = Collections.unmodifiableSet(in.readSet(StreamInput::readString));
    }

    public GetUserPrivilegesResponse(Set<String> cluster, Set<ConfigurableClusterPrivilege> conditionalCluster,
                                     Set<Indices> index,
                                     Set<RoleDescriptor.ApplicationResourcePrivileges> application,
                                     Set<String> runAs) {
        this.cluster = Collections.unmodifiableSet(cluster);
        this.configurableClusterPrivileges = Collections.unmodifiableSet(conditionalCluster);
        this.index = Collections.unmodifiableSet(index);
        this.application = Collections.unmodifiableSet(application);
        this.runAs = Collections.unmodifiableSet(runAs);
    }

    public Set<String> getClusterPrivileges() {
        return cluster;
    }

    public Set<ConfigurableClusterPrivilege> getConditionalClusterPrivileges() {
        return configurableClusterPrivileges;
    }

    public Set<Indices> getIndexPrivileges() {
        return index;
    }

    public Set<RoleDescriptor.ApplicationResourcePrivileges> getApplicationPrivileges() {
        return application;
    }

    public Set<String> getRunAs() {
        return runAs;
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeCollection(cluster, StreamOutput::writeString);
        out.writeCollection(configurableClusterPrivileges, ConfigurableClusterPrivileges.WRITER);
        out.writeCollection(index);
        out.writeCollection(application);
        out.writeCollection(runAs, StreamOutput::writeString);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        final GetUserPrivilegesResponse that = (GetUserPrivilegesResponse) other;
        return Objects.equals(cluster, that.cluster) &&
            Objects.equals(configurableClusterPrivileges, that.configurableClusterPrivileges) &&
            Objects.equals(index, that.index) &&
            Objects.equals(application, that.application) &&
            Objects.equals(runAs, that.runAs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cluster, configurableClusterPrivileges, index, application, runAs);
    }

    /**
     * This is modelled on {@link RoleDescriptor.IndicesPrivileges}, with support for multiple DLS and FLS field sets.
     */
    public static class Indices implements ToXContentObject, Writeable {

        private final Set<String> indices;
        private final Set<String> privileges;
        private final Set<FieldPermissionsDefinition.FieldGrantExcludeGroup> fieldSecurity;
        private final Set<BytesReference> queries;
        private final boolean allowRestrictedIndices;

        public Indices(Collection<String> indices, Collection<String> privileges,
                Set<FieldPermissionsDefinition.FieldGrantExcludeGroup> fieldSecurity, Set<BytesReference> queries,
                boolean allowRestrictedIndices) {
            // The use of TreeSet is to provide a consistent order that can be relied upon in tests
            this.indices = Collections.unmodifiableSet(new TreeSet<>(Objects.requireNonNull(indices)));
            this.privileges = Collections.unmodifiableSet(new TreeSet<>(Objects.requireNonNull(privileges)));
            this.fieldSecurity = Collections.unmodifiableSet(Objects.requireNonNull(fieldSecurity));
            this.queries = Collections.unmodifiableSet(Objects.requireNonNull(queries));
            this.allowRestrictedIndices = allowRestrictedIndices;
        }

        public Indices(StreamInput in) throws IOException {
            // The use of TreeSet is to provide a consistent order that can be relied upon in tests
            indices = Collections.unmodifiableSet(new TreeSet<>(in.readSet(StreamInput::readString)));
            privileges = Collections.unmodifiableSet(new TreeSet<>(in.readSet(StreamInput::readString)));
            fieldSecurity = Collections.unmodifiableSet(in.readSet(input -> {
                final String[] grant = input.readOptionalStringArray();
                final String[] exclude = input.readOptionalStringArray();
                return new FieldPermissionsDefinition.FieldGrantExcludeGroup(grant, exclude);
            }));
            queries = Collections.unmodifiableSet(in.readSet(StreamInput::readBytesReference));
            if (in.getVersion().onOrAfter(Version.V_6_7_0)) {
                this.allowRestrictedIndices = in.readBoolean();
            } else {
                this.allowRestrictedIndices = false;
            }
        }

        public Set<String> getIndices() {
            return indices;
        }

        public Set<String> getPrivileges() {
            return privileges;
        }

        public Set<FieldPermissionsDefinition.FieldGrantExcludeGroup> getFieldSecurity() {
            return fieldSecurity;
        }

        public Set<BytesReference> getQueries() {
            return queries;
        }

        public boolean allowRestrictedIndices() {
            return allowRestrictedIndices;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(getClass().getSimpleName())
                .append("[")
                .append("indices=[").append(Strings.collectionToCommaDelimitedString(indices))
                .append("], allow_restricted_indices=[").append(allowRestrictedIndices)
                .append("], privileges=[").append(Strings.collectionToCommaDelimitedString(privileges))
                .append("]");
            if (fieldSecurity.isEmpty() == false) {
                sb.append(", fls=[").append(Strings.collectionToCommaDelimitedString(fieldSecurity)).append("]");
            }
            if (queries.isEmpty() == false) {
                sb.append(", dls=[")
                    .append(queries.stream().map(BytesReference::utf8ToString).collect(Collectors.joining(",")))
                    .append("]");
            }
            sb.append("]");
            return sb.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Indices that = (Indices) o;

            return this.indices.equals(that.indices)
                && this.privileges.equals(that.privileges)
                && this.fieldSecurity.equals(that.fieldSecurity)
                && this.queries.equals(that.queries)
                && this.allowRestrictedIndices == that.allowRestrictedIndices;
        }

        @Override
        public int hashCode() {
            return Objects.hash(indices, privileges, fieldSecurity, queries, allowRestrictedIndices);
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            builder.startObject();
            builder.field(RoleDescriptor.Fields.NAMES.getPreferredName(), indices);
            builder.field(RoleDescriptor.Fields.PRIVILEGES.getPreferredName(), privileges);
            if (fieldSecurity.stream().anyMatch(g -> nonEmpty(g.getGrantedFields()) || nonEmpty(g.getExcludedFields()))) {
                builder.startArray(RoleDescriptor.Fields.FIELD_PERMISSIONS.getPreferredName());
                for (FieldPermissionsDefinition.FieldGrantExcludeGroup group : this.fieldSecurity) {
                    builder.startObject();
                    if (nonEmpty(group.getGrantedFields())) {
                        builder.array(RoleDescriptor.Fields.GRANT_FIELDS.getPreferredName(), group.getGrantedFields());
                    }
                    if (nonEmpty(group.getExcludedFields())) {
                        builder.array(RoleDescriptor.Fields.EXCEPT_FIELDS.getPreferredName(), group.getExcludedFields());
                    }
                    builder.endObject();
                }
                builder.endArray();
            }
            if (queries.isEmpty() == false) {
                builder.startArray(RoleDescriptor.Fields.QUERY.getPreferredName());
                for (BytesReference q : queries) {
                    builder.value(q.utf8ToString());
                }
                builder.endArray();
            }
            builder.field(RoleDescriptor.Fields.ALLOW_RESTRICTED_INDICES.getPreferredName(), allowRestrictedIndices);
            return builder.endObject();
        }

        private boolean nonEmpty(String[] grantedFields) {
            return grantedFields != null && grantedFields.length != 0;
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            out.writeCollection(indices, StreamOutput::writeString);
            out.writeCollection(privileges, StreamOutput::writeString);
            out.writeCollection(fieldSecurity, (output, fields) -> {
                output.writeOptionalStringArray(fields.getGrantedFields());
                output.writeOptionalStringArray(fields.getExcludedFields());
            });
            out.writeCollection(queries, StreamOutput::writeBytesReference);
            if (out.getVersion().onOrAfter(Version.V_6_7_0)) {
                out.writeBoolean(allowRestrictedIndices);
            }
        }
    }
}
