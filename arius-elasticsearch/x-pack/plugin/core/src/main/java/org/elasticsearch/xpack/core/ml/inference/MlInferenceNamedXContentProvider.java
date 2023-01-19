/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.core.ml.inference;

import org.elasticsearch.common.io.stream.NamedWriteableRegistry;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.plugins.spi.NamedXContentProvider;
import org.elasticsearch.xpack.core.ml.inference.preprocessing.CustomWordEmbedding;
import org.elasticsearch.xpack.core.ml.inference.results.ClassificationInferenceResults;
import org.elasticsearch.xpack.core.ml.inference.results.InferenceResults;
import org.elasticsearch.xpack.core.ml.inference.results.RegressionInferenceResults;
import org.elasticsearch.xpack.core.ml.inference.trainedmodel.ClassificationConfig;
import org.elasticsearch.xpack.core.ml.inference.trainedmodel.InferenceConfig;
import org.elasticsearch.xpack.core.ml.inference.trainedmodel.LenientlyParsedTrainedModel;
import org.elasticsearch.xpack.core.ml.inference.trainedmodel.RegressionConfig;
import org.elasticsearch.xpack.core.ml.inference.trainedmodel.TrainedModel;
import org.elasticsearch.xpack.core.ml.inference.trainedmodel.StrictlyParsedTrainedModel;
import org.elasticsearch.xpack.core.ml.inference.trainedmodel.ensemble.Ensemble;
import org.elasticsearch.xpack.core.ml.inference.trainedmodel.ensemble.LenientlyParsedOutputAggregator;
import org.elasticsearch.xpack.core.ml.inference.trainedmodel.ensemble.LogisticRegression;
import org.elasticsearch.xpack.core.ml.inference.trainedmodel.ensemble.OutputAggregator;
import org.elasticsearch.xpack.core.ml.inference.trainedmodel.ensemble.StrictlyParsedOutputAggregator;
import org.elasticsearch.xpack.core.ml.inference.trainedmodel.ensemble.WeightedMode;
import org.elasticsearch.xpack.core.ml.inference.trainedmodel.ensemble.WeightedSum;
import org.elasticsearch.xpack.core.ml.inference.trainedmodel.langident.LangIdentNeuralNetwork;
import org.elasticsearch.xpack.core.ml.inference.trainedmodel.tree.Tree;
import org.elasticsearch.xpack.core.ml.inference.preprocessing.FrequencyEncoding;
import org.elasticsearch.xpack.core.ml.inference.preprocessing.LenientlyParsedPreProcessor;
import org.elasticsearch.xpack.core.ml.inference.preprocessing.OneHotEncoding;
import org.elasticsearch.xpack.core.ml.inference.preprocessing.PreProcessor;
import org.elasticsearch.xpack.core.ml.inference.preprocessing.StrictlyParsedPreProcessor;
import org.elasticsearch.xpack.core.ml.inference.preprocessing.TargetMeanEncoding;

import java.util.ArrayList;
import java.util.List;

public class MlInferenceNamedXContentProvider implements NamedXContentProvider {

    @Override
    public List<NamedXContentRegistry.Entry> getNamedXContentParsers() {
        List<NamedXContentRegistry.Entry> namedXContent = new ArrayList<>();

        // PreProcessing Lenient
        namedXContent.add(new NamedXContentRegistry.Entry(LenientlyParsedPreProcessor.class, OneHotEncoding.NAME,
            OneHotEncoding::fromXContentLenient));
        namedXContent.add(new NamedXContentRegistry.Entry(LenientlyParsedPreProcessor.class, TargetMeanEncoding.NAME,
            TargetMeanEncoding::fromXContentLenient));
        namedXContent.add(new NamedXContentRegistry.Entry(LenientlyParsedPreProcessor.class, FrequencyEncoding.NAME,
            FrequencyEncoding::fromXContentLenient));
        namedXContent.add(new NamedXContentRegistry.Entry(LenientlyParsedPreProcessor.class, CustomWordEmbedding.NAME,
            CustomWordEmbedding::fromXContentLenient));

        // PreProcessing Strict
        namedXContent.add(new NamedXContentRegistry.Entry(StrictlyParsedPreProcessor.class, OneHotEncoding.NAME,
            OneHotEncoding::fromXContentStrict));
        namedXContent.add(new NamedXContentRegistry.Entry(StrictlyParsedPreProcessor.class, TargetMeanEncoding.NAME,
            TargetMeanEncoding::fromXContentStrict));
        namedXContent.add(new NamedXContentRegistry.Entry(StrictlyParsedPreProcessor.class, FrequencyEncoding.NAME,
            FrequencyEncoding::fromXContentStrict));
        namedXContent.add(new NamedXContentRegistry.Entry(StrictlyParsedPreProcessor.class, CustomWordEmbedding.NAME,
            CustomWordEmbedding::fromXContentStrict));

        // Model Lenient
        namedXContent.add(new NamedXContentRegistry.Entry(LenientlyParsedTrainedModel.class, Tree.NAME, Tree::fromXContentLenient));
        namedXContent.add(new NamedXContentRegistry.Entry(LenientlyParsedTrainedModel.class, Ensemble.NAME, Ensemble::fromXContentLenient));
        namedXContent.add(new NamedXContentRegistry.Entry(LenientlyParsedTrainedModel.class,
            LangIdentNeuralNetwork.NAME,
            LangIdentNeuralNetwork::fromXContentLenient));

        // Output Aggregator Lenient
        namedXContent.add(new NamedXContentRegistry.Entry(LenientlyParsedOutputAggregator.class,
            WeightedMode.NAME,
            WeightedMode::fromXContentLenient));
        namedXContent.add(new NamedXContentRegistry.Entry(LenientlyParsedOutputAggregator.class,
            WeightedSum.NAME,
            WeightedSum::fromXContentLenient));
        namedXContent.add(new NamedXContentRegistry.Entry(LenientlyParsedOutputAggregator.class,
            LogisticRegression.NAME,
            LogisticRegression::fromXContentLenient));

        // Model Strict
        namedXContent.add(new NamedXContentRegistry.Entry(StrictlyParsedTrainedModel.class, Tree.NAME, Tree::fromXContentStrict));
        namedXContent.add(new NamedXContentRegistry.Entry(StrictlyParsedTrainedModel.class, Ensemble.NAME, Ensemble::fromXContentStrict));
        namedXContent.add(new NamedXContentRegistry.Entry(StrictlyParsedTrainedModel.class,
            LangIdentNeuralNetwork.NAME,
            LangIdentNeuralNetwork::fromXContentStrict));

        // Output Aggregator Strict
        namedXContent.add(new NamedXContentRegistry.Entry(StrictlyParsedOutputAggregator.class,
            WeightedMode.NAME,
            WeightedMode::fromXContentStrict));
        namedXContent.add(new NamedXContentRegistry.Entry(StrictlyParsedOutputAggregator.class,
            WeightedSum.NAME,
            WeightedSum::fromXContentStrict));
        namedXContent.add(new NamedXContentRegistry.Entry(StrictlyParsedOutputAggregator.class,
            LogisticRegression.NAME,
            LogisticRegression::fromXContentStrict));

        return namedXContent;
    }

    public List<NamedWriteableRegistry.Entry> getNamedWriteables() {
        List<NamedWriteableRegistry.Entry> namedWriteables = new ArrayList<>();

        // PreProcessing
        namedWriteables.add(new NamedWriteableRegistry.Entry(PreProcessor.class, OneHotEncoding.NAME.getPreferredName(),
            OneHotEncoding::new));
        namedWriteables.add(new NamedWriteableRegistry.Entry(PreProcessor.class, TargetMeanEncoding.NAME.getPreferredName(),
            TargetMeanEncoding::new));
        namedWriteables.add(new NamedWriteableRegistry.Entry(PreProcessor.class, FrequencyEncoding.NAME.getPreferredName(),
            FrequencyEncoding::new));
        namedWriteables.add(new NamedWriteableRegistry.Entry(PreProcessor.class, CustomWordEmbedding.NAME.getPreferredName(),
            CustomWordEmbedding::new));

        // Model
        namedWriteables.add(new NamedWriteableRegistry.Entry(TrainedModel.class, Tree.NAME.getPreferredName(), Tree::new));
        namedWriteables.add(new NamedWriteableRegistry.Entry(TrainedModel.class, Ensemble.NAME.getPreferredName(), Ensemble::new));
        namedWriteables.add(new NamedWriteableRegistry.Entry(LangIdentNeuralNetwork.class,
            LangIdentNeuralNetwork.NAME.getPreferredName(),
            LangIdentNeuralNetwork::new));

        // Output Aggregator
        namedWriteables.add(new NamedWriteableRegistry.Entry(OutputAggregator.class,
            WeightedSum.NAME.getPreferredName(),
            WeightedSum::new));
        namedWriteables.add(new NamedWriteableRegistry.Entry(OutputAggregator.class,
            WeightedMode.NAME.getPreferredName(),
            WeightedMode::new));
        namedWriteables.add(new NamedWriteableRegistry.Entry(OutputAggregator.class,
            LogisticRegression.NAME.getPreferredName(),
            LogisticRegression::new));

        // Inference Results
        namedWriteables.add(new NamedWriteableRegistry.Entry(InferenceResults.class,
            ClassificationInferenceResults.NAME,
            ClassificationInferenceResults::new));
        namedWriteables.add(new NamedWriteableRegistry.Entry(InferenceResults.class,
            RegressionInferenceResults.NAME,
            RegressionInferenceResults::new));

        // Inference Configs
        namedWriteables.add(new NamedWriteableRegistry.Entry(InferenceConfig.class, ClassificationConfig.NAME, ClassificationConfig::new));
        namedWriteables.add(new NamedWriteableRegistry.Entry(InferenceConfig.class, RegressionConfig.NAME, RegressionConfig::new));

        return namedWriteables;
    }
}
