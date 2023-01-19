package org.elasticsearch.dcdr.translog.primary;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.elasticsearch.Version;
import org.elasticsearch.cluster.AbstractDiffable;
import org.elasticsearch.cluster.Diff;
import org.elasticsearch.cluster.DiffableUtils;
import org.elasticsearch.cluster.NamedDiff;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.ConstructingObjectParser;
import org.elasticsearch.common.xcontent.XContentBuilder;

/**
 * author weizijun
 * date：2019-08-09
 */
public class DCDRMetadata implements MetaData.Custom {
    public static final String TYPE = "dcdr";

    @SuppressWarnings("unchecked")
    public static final ConstructingObjectParser<DCDRMetadata, Void> PARSER = new ConstructingObjectParser<>(
        TYPE,
        a -> new DCDRMetadata(
            ((List<DCDRIndexMetadata>) a[0]).stream()
                .collect(Collectors.toMap(DCDRIndexMetadata::name, Function.identity())),
            ((List<DCDRTemplateMetadata>) a[1]).stream()
                .collect(Collectors.toMap(DCDRTemplateMetadata::getName, Function.identity()))
        )
    );

    private static final ParseField INDICES_FIELD = new ParseField("indices");
    private static final ParseField TEMPLATES_FIELD = new ParseField("templates");
    public static final DCDRMetadata EMPTY = new DCDRMetadata(Collections.emptyMap(), Collections.emptyMap());

    static {
        PARSER.declareNamedObjects(
            ConstructingObjectParser.constructorArg(),
            (p, c, n) -> DCDRIndexMetadata.parse(p, n),
            v -> {
                throw new IllegalArgumentException("ordered " + INDICES_FIELD.getPreferredName() + " are not supported");
            },
            INDICES_FIELD
        );
        PARSER.declareNamedObjects(
            ConstructingObjectParser.constructorArg(),
            (p, c, n) -> DCDRTemplateMetadata.parse(p, n),
            v -> {
                throw new IllegalArgumentException("ordered " + TEMPLATES_FIELD.getPreferredName() + " are not supported");
            },
            TEMPLATES_FIELD
        );
    }

    private final Map<String, DCDRIndexMetadata> replicaIndices;
    private final Map<String, DCDRTemplateMetadata> replicaTemplates;

    public DCDRMetadata(Map<String, DCDRIndexMetadata> replicaIndices, Map<String, DCDRTemplateMetadata> replicaTemplates) {
        this.replicaIndices = replicaIndices;
        this.replicaTemplates = replicaTemplates;
    }

    public DCDRMetadata(StreamInput in) throws IOException {
        int indexSize = in.readVInt();
        Map<String, DCDRIndexMetadata> indexReplicas = new TreeMap<>();
        for (int i = 0; i < indexSize; ++i) {
            indexReplicas.put(in.readString(), new DCDRIndexMetadata(in));
        }
        this.replicaIndices = indexReplicas;

        int templateSize = in.readVInt();
        Map<String, DCDRTemplateMetadata> templateReplicas = new TreeMap<>();
        for (int i = 0; i < templateSize; ++i) {
            templateReplicas.put(in.readString(), new DCDRTemplateMetadata(in));
        }
        this.replicaTemplates = templateReplicas;
    }

    @Override
    public EnumSet<MetaData.XContentContext> context() {
        return MetaData.ALL_CONTEXTS;
    }

    @Override
    public Diff<MetaData.Custom> diff(MetaData.Custom previousState) {
        return new DCDRMetadataDiff((DCDRMetadata) previousState, this);
    }

    @Override
    public String getWriteableName() {
        return TYPE;
    }

    @Override
    public Version getMinimalSupportedVersion() {
        return Version.CURRENT;
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeVInt(replicaIndices.size());
        for (Map.Entry<String, DCDRIndexMetadata> entry : replicaIndices.entrySet()) {
            out.writeString(entry.getKey());
            entry.getValue().writeTo(out);
        }
        out.writeVInt(replicaTemplates.size());
        for (Map.Entry<String, DCDRTemplateMetadata> entry : replicaTemplates.entrySet()) {
            out.writeString(entry.getKey());
            entry.getValue().writeTo(out);
        }
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.field(INDICES_FIELD.getPreferredName(), replicaIndices);
        builder.field(TEMPLATES_FIELD.getPreferredName(), replicaTemplates);
        return builder;
    }

    public Map<String, DCDRIndexMetadata> getReplicaIndices() {
        return replicaIndices;
    }

    public Map<String, DCDRTemplateMetadata> getReplicaTemplates() {
        return replicaTemplates;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DCDRMetadata that = (DCDRMetadata) o;
        return Objects.equals(replicaIndices, that.replicaIndices) && Objects.equals(replicaTemplates, that.replicaTemplates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(replicaIndices, replicaTemplates);
    }

    public static class DCDRMetadataDiff implements NamedDiff<MetaData.Custom> {

        final Diff<Map<String, DCDRIndexMetadata>> indices;
        final Diff<Map<String, DCDRTemplateMetadata>> templates;

        public DCDRMetadataDiff(final StreamInput in) throws IOException {
            this.indices = DiffableUtils.readJdkMapDiff(
                in,
                DiffableUtils.getStringKeySerializer(),
                DCDRIndexMetadata::new,
                DCDRMetadataDiff::readDiffIndexFrom
            );
            this.templates = DiffableUtils.readJdkMapDiff(
                in,
                DiffableUtils.getStringKeySerializer(),
                DCDRTemplateMetadata::new,
                DCDRMetadataDiff::readDiffTemplateFrom
            );
        }

        DCDRMetadataDiff(final DCDRMetadata previous, final DCDRMetadata current) {
            this.indices = DiffableUtils.diff(previous.replicaIndices, current.replicaIndices, DiffableUtils.getStringKeySerializer());
            this.templates = DiffableUtils.diff(
                previous.replicaTemplates,
                current.replicaTemplates,
                DiffableUtils.getStringKeySerializer()
            );
        }

        @Override
        public MetaData.Custom apply(MetaData.Custom previous) {
            TreeMap<String, DCDRIndexMetadata> newIndices = new TreeMap<>(
                indices.apply(((DCDRMetadata) previous).replicaIndices)
            );
            TreeMap<String, DCDRTemplateMetadata> newTemplates = new TreeMap<>(
                templates.apply(((DCDRMetadata) previous).replicaTemplates)
            );
            return new DCDRMetadata(newIndices, newTemplates);
        }

        @Override
        public String getWriteableName() {
            return TYPE;
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            indices.writeTo(out);
            templates.writeTo(out);
        }

        static Diff<DCDRIndexMetadata> readDiffIndexFrom(StreamInput in) throws IOException {
            return AbstractDiffable.readDiffFrom(DCDRIndexMetadata::new, in);
        }

        static Diff<DCDRTemplateMetadata> readDiffTemplateFrom(StreamInput in) throws IOException {
            return AbstractDiffable.readDiffFrom(DCDRTemplateMetadata::new, in);
        }

    }
}
