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

package org.elasticsearch.env;

import org.elasticsearch.Version;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.xcontent.ObjectParser;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.gateway.MetaDataStateFormat;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

/**
 * Metadata associated with this node: its persistent node ID and its version.
 * The metadata is persisted in the data folder of this node and is reused across restarts.
 */
public final class NodeMetaData {

    static final String NODE_ID_KEY = "node_id";
    static final String NODE_VERSION_KEY = "node_version";

    private final String nodeId;

    private final Version nodeVersion;

    public NodeMetaData(final String nodeId, final Version nodeVersion) {
        this.nodeId = Objects.requireNonNull(nodeId);
        this.nodeVersion = Objects.requireNonNull(nodeVersion);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeMetaData that = (NodeMetaData) o;
        return nodeId.equals(that.nodeId) &&
            nodeVersion.equals(that.nodeVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId, nodeVersion);
    }

    @Override
    public String toString() {
        return "NodeMetaData{" +
            "nodeId='" + nodeId + '\'' +
            ", nodeVersion=" + nodeVersion +
            '}';
    }

    public String nodeId() {
        return nodeId;
    }

    public Version nodeVersion() {
        return nodeVersion;
    }

    public NodeMetaData upgradeToCurrentVersion() {
        if (nodeVersion.equals(Version.V_EMPTY)) {
            assert Version.CURRENT.major <= Version.V_7_0_0.major + 1 : "version is required in the node metadata from v9 onwards";
            return new NodeMetaData(nodeId, Version.CURRENT);
        }

        if (nodeVersion.before(Version.CURRENT.minimumIndexCompatibilityVersion())) {
            throw new IllegalStateException(
                "cannot upgrade a node from version [" + nodeVersion + "] directly to version [" + Version.CURRENT + "]");
        }

        if (nodeVersion.after(Version.CURRENT)) {
            throw new IllegalStateException(
                "cannot downgrade a node from version [" + nodeVersion + "] to version [" + Version.CURRENT + "]");
        }

        return nodeVersion.equals(Version.CURRENT) ? this : new NodeMetaData(nodeId, Version.CURRENT);
    }

    private static class Builder {
        String nodeId;
        Version nodeVersion;

        public void setNodeId(String nodeId) {
            this.nodeId = nodeId;
        }

        public void setNodeVersionId(int nodeVersionId) {
            this.nodeVersion = Version.fromId(nodeVersionId);
        }

        public NodeMetaData build() {
            final Version nodeVersion;
            if (this.nodeVersion == null) {
                assert Version.CURRENT.major <= Version.V_7_0_0.major + 1 : "version is required in the node metadata from v9 onwards";
                nodeVersion = Version.V_EMPTY;
            } else {
                nodeVersion = this.nodeVersion;
            }

            return new NodeMetaData(nodeId, nodeVersion);
        }
    }

    static class NodeMetaDataStateFormat extends MetaDataStateFormat<NodeMetaData> {

        private ObjectParser<Builder, Void> objectParser;

        /**
         * @param ignoreUnknownFields whether to ignore unknown fields or not. Normally we are strict about this, but
         *                            {@link OverrideNodeVersionCommand} is lenient.
         */
        NodeMetaDataStateFormat(boolean ignoreUnknownFields) {
            super("node-");
            objectParser = new ObjectParser<>("node_meta_data", ignoreUnknownFields, Builder::new);
            objectParser.declareString(Builder::setNodeId, new ParseField(NODE_ID_KEY));
            objectParser.declareInt(Builder::setNodeVersionId, new ParseField(NODE_VERSION_KEY));
        }

        @Override
        protected XContentBuilder newXContentBuilder(XContentType type, OutputStream stream) throws IOException {
            XContentBuilder xContentBuilder = super.newXContentBuilder(type, stream);
            xContentBuilder.prettyPrint();
            return xContentBuilder;
        }

        @Override
        public void toXContent(XContentBuilder builder, NodeMetaData nodeMetaData) throws IOException {
            builder.field(NODE_ID_KEY, nodeMetaData.nodeId);
            builder.field(NODE_VERSION_KEY, nodeMetaData.nodeVersion.id);
        }

        @Override
        public NodeMetaData fromXContent(XContentParser parser) throws IOException {
            return objectParser.apply(parser, null).build();
        }
    }

    public static final MetaDataStateFormat<NodeMetaData> FORMAT = new NodeMetaDataStateFormat(false);
}
