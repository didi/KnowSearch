package org.elasticsearch.dcdr.translog.primary;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

import org.elasticsearch.cluster.AbstractDiffable;
import org.elasticsearch.cluster.Diffable;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.ConstructingObjectParser;
import org.elasticsearch.common.xcontent.ToXContentObject;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;

/**
 * author weizijun
 * date：2019-08-09
 */
public class DCDRTemplateMetadata extends AbstractDiffable<DCDRTemplateMetadata> implements ToXContentObject,
    Diffable<DCDRTemplateMetadata> {

    private static final ParseField NAME_FIELD = new ParseField("name");
    private static final ParseField TEMPLATE_FIELD = new ParseField("template");
    private static final ParseField REPLICA_CLUSTER_FIELD = new ParseField("replica_cluster");

    @SuppressWarnings("unchecked")
    public static final ConstructingObjectParser<DCDRTemplateMetadata, String> PARSER = new ConstructingObjectParser<>(
        "replica_info",
        a -> new DCDRTemplateMetadata((String) a[0], (String) a[1], (String) a[2])
    );

    static {
        PARSER.declareString(ConstructingObjectParser.constructorArg(), NAME_FIELD);
        PARSER.declareString(ConstructingObjectParser.constructorArg(), TEMPLATE_FIELD);
        PARSER.declareString(ConstructingObjectParser.constructorArg(), REPLICA_CLUSTER_FIELD);
    }

    public static DCDRTemplateMetadata parse(XContentParser parser, String name) {
        return PARSER.apply(parser, name);
    }

    private String name;
    private String template;
    private String replicaCluster;

    public DCDRTemplateMetadata(StreamInput in) throws IOException {
        this.name = in.readString();
        this.template = in.readString();
        this.replicaCluster = in.readString();
    }

    public DCDRTemplateMetadata(String name, String template, String replicaCluster) {
        this.name = name;
        this.template = template;
        this.replicaCluster = replicaCluster;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getReplicaCluster() {
        return replicaCluster;
    }

    public void setReplicaCluster(String replicaCluster) {
        this.replicaCluster = replicaCluster;
    }

    public static String name(String template, String replicaCluster) {
        return String.format(Locale.US, "%s(%s)", template, replicaCluster);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DCDRTemplateMetadata that = (DCDRTemplateMetadata) o;
        return Objects.equals(template, that.template) &&
            Objects.equals(replicaCluster, that.replicaCluster);
    }

    @Override
    public int hashCode() {
        return Objects.hash(template, replicaCluster);
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeString(name);
        out.writeString(template);
        out.writeString(replicaCluster);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();
        builder.field(NAME_FIELD.getPreferredName(), name);
        builder.field(TEMPLATE_FIELD.getPreferredName(), template);
        builder.field(REPLICA_CLUSTER_FIELD.getPreferredName(), replicaCluster);
        builder.endObject();
        return builder;
    }

    @Override
    public String toString() {
        return "{" +
            "na='" + name + '\'' +
            "te='" + template + '\'' +
            ", rc='" + replicaCluster + '\'' +
            '}';
    }
}
