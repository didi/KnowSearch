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
package org.elasticsearch.client.ml.inference.trainedmodel.langident;

import org.elasticsearch.client.ml.inference.trainedmodel.TrainedModel;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.xcontent.ConstructingObjectParser;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.elasticsearch.common.xcontent.ConstructingObjectParser.constructorArg;

/**
 * Shallow, fully connected, feed forward NN modeled after and ported from https://github.com/google/cld3
 */
public class LangIdentNeuralNetwork implements TrainedModel {

    public static final String NAME = "lang_ident_neural_network";
    public static final ParseField EMBEDDED_VECTOR_FEATURE_NAME = new ParseField("embedded_vector_feature_name");
    public static final ParseField HIDDEN_LAYER = new ParseField("hidden_layer");
    public static final ParseField SOFTMAX_LAYER = new ParseField("softmax_layer");
    public static final ConstructingObjectParser<LangIdentNeuralNetwork, Void> PARSER = new ConstructingObjectParser<>(
        NAME,
        true,
        a -> new LangIdentNeuralNetwork((String) a[0],
            (LangNetLayer) a[1],
            (LangNetLayer) a[2]));

    static {
        PARSER.declareString(constructorArg(), EMBEDDED_VECTOR_FEATURE_NAME);
        PARSER.declareObject(constructorArg(), LangNetLayer.PARSER::apply, HIDDEN_LAYER);
        PARSER.declareObject(constructorArg(), LangNetLayer.PARSER::apply, SOFTMAX_LAYER);
    }

    public static LangIdentNeuralNetwork fromXContent(XContentParser parser) {
        return PARSER.apply(parser, null);
    }

    private final LangNetLayer hiddenLayer;
    private final LangNetLayer softmaxLayer;
    private final String embeddedVectorFeatureName;

    LangIdentNeuralNetwork(String embeddedVectorFeatureName,
                                  LangNetLayer hiddenLayer,
                                  LangNetLayer softmaxLayer) {
        this.embeddedVectorFeatureName = embeddedVectorFeatureName;
        this.hiddenLayer = hiddenLayer;
        this.softmaxLayer = softmaxLayer;
    }

    @Override
    public List<String> getFeatureNames() {
        return Collections.singletonList(embeddedVectorFeatureName);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, ToXContent.Params params) throws IOException {
        builder.startObject();
        builder.field(EMBEDDED_VECTOR_FEATURE_NAME.getPreferredName(), embeddedVectorFeatureName);
        builder.field(HIDDEN_LAYER.getPreferredName(), hiddenLayer);
        builder.field(SOFTMAX_LAYER.getPreferredName(), softmaxLayer);
        builder.endObject();
        return builder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LangIdentNeuralNetwork that = (LangIdentNeuralNetwork) o;
        return Objects.equals(embeddedVectorFeatureName, that.embeddedVectorFeatureName)
            && Objects.equals(hiddenLayer, that.hiddenLayer)
            && Objects.equals(softmaxLayer, that.softmaxLayer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(embeddedVectorFeatureName, hiddenLayer, softmaxLayer);
    }

}
