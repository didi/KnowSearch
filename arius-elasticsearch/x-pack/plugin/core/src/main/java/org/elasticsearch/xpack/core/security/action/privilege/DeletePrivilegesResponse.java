/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.core.security.action.privilege;

import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.ToXContentObject;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Response when deleting application privileges.
 * Returns a collection of privileges that were successfully found and deleted.
 */
public final class DeletePrivilegesResponse extends ActionResponse implements ToXContentObject {

    private Set<String> found;

    public DeletePrivilegesResponse(StreamInput in) throws IOException {
        super(in);
        this.found = Collections.unmodifiableSet(in.readSet(StreamInput::readString));
    }

    public DeletePrivilegesResponse(Collection<String> found) {
        this.found = Collections.unmodifiableSet(new HashSet<>(found));
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject().field("found", found).endObject();
        return builder;
    }

    public Set<String> found() {
        return this.found;
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeCollection(found, StreamOutput::writeString);
    }

}
