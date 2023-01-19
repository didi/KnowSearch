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

package org.elasticsearch.action.admin.indices.flush;

import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.support.broadcast.BroadcastRequest;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

import java.io.IOException;

import static org.elasticsearch.action.ValidateActions.addValidationError;

/**
 * A flush request to flush one or more indices. The flush process of an index basically frees memory from the index
 * by flushing data to the index storage and clearing the internal transaction log. By default, Elasticsearch uses
 * memory heuristics in order to automatically trigger flush operations as required in order to clear memory.
 * <p>
 * Best created with {@link org.elasticsearch.client.Requests#flushRequest(String...)}.
 *
 * @see org.elasticsearch.client.Requests#flushRequest(String...)
 * @see org.elasticsearch.client.IndicesAdminClient#flush(FlushRequest)
 * @see FlushResponse
 */
public class FlushRequest extends BroadcastRequest<FlushRequest> {

    private boolean force = false;
    private boolean waitIfOngoing = true;
    private boolean updateCheckpoint = false;

    /**
     * Constructs a new flush request against one or more indices. If nothing is provided, all indices will
     * be flushed.
     */
    public FlushRequest(String... indices) {
        super(indices);
    }

    public FlushRequest(StreamInput in) throws IOException {
        super(in);
        force = in.readBoolean();
        waitIfOngoing = in.readBoolean();
        updateCheckpoint = in.readBoolean();
    }

    /**
     * Returns {@code true} iff a flush should block
     * if a another flush operation is already running. Otherwise {@code false}
     */
    public boolean waitIfOngoing() {
        return this.waitIfOngoing;
    }

    /**
     * if set to {@code true} the flush will block
     * if a another flush operation is already running until the flush can be performed.
     * The default is <code>true</code>
     */
    public FlushRequest waitIfOngoing(boolean waitIfOngoing) {
        this.waitIfOngoing = waitIfOngoing;
        return this;
    }

    /**
     * Force flushing, even if one is possibly not needed.
     */
    public boolean force() {
        return force;
    }

    /**
     * Force flushing, even if one is possibly not needed.
     */
    public FlushRequest force(boolean force) {
        this.force = force;
        return this;
    }

    public boolean updateCheckpoint() {
        return updateCheckpoint;
    }

    public FlushRequest updateCheckpoint(boolean updateCheckpoint) {
        this.updateCheckpoint = updateCheckpoint;
        return this;
    }

    @Override
    public ActionRequestValidationException validate() {
        ActionRequestValidationException validationError = super.validate();
        if (force && waitIfOngoing == false) {
            validationError = addValidationError("wait_if_ongoing must be true for a force flush", validationError);
        }
        if (updateCheckpoint && indices != null && indices.length > 1) {
            validationError = addValidationError("only one index can update checkpoint at a time", validationError);
        }
        return validationError;
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeBoolean(force);
        out.writeBoolean(waitIfOngoing);
        out.writeBoolean(updateCheckpoint);
    }

    @Override
    public String toString() {
        return "FlushRequest{" +
                "waitIfOngoing=" + waitIfOngoing +
                ", force=" + force +
                ", updateCheckpoint=" + updateCheckpoint +
            "}";
    }
}
