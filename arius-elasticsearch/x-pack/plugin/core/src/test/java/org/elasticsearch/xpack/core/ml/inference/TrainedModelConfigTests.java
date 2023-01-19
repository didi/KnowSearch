/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.core.ml.inference;

import org.elasticsearch.Version;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.io.stream.NamedWriteableRegistry;
import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.DeprecationHandler;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.license.License;
import org.elasticsearch.license.XPackLicenseState;
import org.elasticsearch.search.SearchModule;
import org.elasticsearch.test.AbstractSerializingTestCase;
import org.elasticsearch.xpack.core.ml.job.messages.Messages;
import org.elasticsearch.xpack.core.ml.utils.MlStrings;
import org.elasticsearch.xpack.core.ml.utils.ToXContentParams;
import org.junit.Before;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.elasticsearch.test.AbstractXContentTestCase.xContentTester;
import static org.elasticsearch.xpack.core.ml.utils.ToXContentParams.FOR_INTERNAL_STORAGE;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TrainedModelConfigTests extends AbstractSerializingTestCase<TrainedModelConfig> {

    private boolean lenient;

    public static TrainedModelConfig.Builder createTestInstance(String modelId) {
        List<String> tags = Arrays.asList(generateRandomStringArray(randomIntBetween(0, 5), 15, false));
        return TrainedModelConfig.builder()
            .setInput(TrainedModelInputTests.createRandomInput())
            .setMetadata(randomBoolean() ? null : Collections.singletonMap(randomAlphaOfLength(10), randomAlphaOfLength(10)))
            .setCreateTime(Instant.ofEpochMilli(randomLongBetween(Instant.MIN.getEpochSecond(), Instant.MAX.getEpochSecond())))
            .setVersion(Version.CURRENT)
            .setModelId(modelId)
            .setCreatedBy(randomAlphaOfLength(10))
            .setDescription(randomBoolean() ? null : randomAlphaOfLength(100))
            .setEstimatedHeapMemory(randomNonNegativeLong())
            .setEstimatedOperations(randomNonNegativeLong())
            .setLicenseLevel(randomFrom(License.OperationMode.PLATINUM.description(),
                License.OperationMode.GOLD.description(),
                License.OperationMode.BASIC.description()))
            .setTags(tags);
    }

    @Before
    public void chooseStrictOrLenient() {
        lenient = randomBoolean();
    }

    @Override
    protected TrainedModelConfig doParseInstance(XContentParser parser) throws IOException {
        return TrainedModelConfig.fromXContent(parser, lenient).build();
    }

    @Override
    protected boolean supportsUnknownFields() {
        return lenient;
    }

    @Override
    protected Predicate<String> getRandomFieldsExcludeFilter() {
        return field -> !field.isEmpty();
    }

    @Override
    protected TrainedModelConfig createTestInstance() {
        return createTestInstance(randomAlphaOfLength(10)).build();
    }

    @Override
    protected Writeable.Reader<TrainedModelConfig> instanceReader() {
        return TrainedModelConfig::new;
    }

    @Override
    protected NamedXContentRegistry xContentRegistry() {
        List<NamedXContentRegistry.Entry> namedXContent = new ArrayList<>();
        namedXContent.addAll(new MlInferenceNamedXContentProvider().getNamedXContentParsers());
        namedXContent.addAll(new SearchModule(Settings.EMPTY, false, Collections.emptyList()).getNamedXContents());
        return new NamedXContentRegistry(namedXContent);
    }

    @Override
    protected NamedWriteableRegistry getNamedWriteableRegistry() {
        List<NamedWriteableRegistry.Entry> entries = new ArrayList<>();
        entries.addAll(new MlInferenceNamedXContentProvider().getNamedWriteables());
        return new NamedWriteableRegistry(entries);
    }

    @Override
    protected ToXContent.Params getToXContentParams() {
        return lenient ? ToXContent.EMPTY_PARAMS : new ToXContent.MapParams(Collections.singletonMap(FOR_INTERNAL_STORAGE, "true"));
    }

    @Override
    protected boolean assertToXContentEquivalence() {
        return false;
    }

    public void testToXContentWithParams() throws IOException {
        TrainedModelConfig.LazyModelDefinition lazyModelDefinition = TrainedModelConfig.LazyModelDefinition
            .fromParsedDefinition(TrainedModelDefinitionTests.createRandomBuilder().build());
        TrainedModelConfig config = new TrainedModelConfig(
            randomAlphaOfLength(10),
            randomAlphaOfLength(10),
            Version.CURRENT,
            randomBoolean() ? null : randomAlphaOfLength(100),
            Instant.ofEpochMilli(randomNonNegativeLong()),
            lazyModelDefinition,
            Collections.emptyList(),
            randomBoolean() ? null : Collections.singletonMap(randomAlphaOfLength(10), randomAlphaOfLength(10)),
            TrainedModelInputTests.createRandomInput(),
            randomNonNegativeLong(),
            randomNonNegativeLong(),
            "platinum");

        BytesReference reference = XContentHelper.toXContent(config, XContentType.JSON, ToXContent.EMPTY_PARAMS, false);
        assertThat(reference.utf8ToString(), containsString("\"definition\""));

        reference = XContentHelper.toXContent(config,
            XContentType.JSON,
            new ToXContent.MapParams(Collections.singletonMap(ToXContentParams.FOR_INTERNAL_STORAGE, "true")),
            false);
        assertThat(reference.utf8ToString(), not(containsString("definition")));

        reference = XContentHelper.toXContent(config,
            XContentType.JSON,
            new ToXContent.MapParams(Collections.singletonMap(TrainedModelConfig.DECOMPRESS_DEFINITION, "false")),
            false);
        assertThat(reference.utf8ToString(), not(containsString("\"definition\"")));
        assertThat(reference.utf8ToString(), containsString("compressed_definition"));
        assertThat(reference.utf8ToString(), containsString(lazyModelDefinition.getCompressedString()));
    }

    public void testParseWithBothDefinitionAndCompressedSupplied() throws IOException {
        TrainedModelConfig.LazyModelDefinition lazyModelDefinition = TrainedModelConfig.LazyModelDefinition
            .fromParsedDefinition(TrainedModelDefinitionTests.createRandomBuilder().build());
        TrainedModelConfig config = new TrainedModelConfig(
            randomAlphaOfLength(10),
            randomAlphaOfLength(10),
            Version.CURRENT,
            randomBoolean() ? null : randomAlphaOfLength(100),
            Instant.ofEpochMilli(randomNonNegativeLong()),
            lazyModelDefinition,
            Collections.emptyList(),
            randomBoolean() ? null : Collections.singletonMap(randomAlphaOfLength(10), randomAlphaOfLength(10)),
            TrainedModelInputTests.createRandomInput(),
            randomNonNegativeLong(),
            randomNonNegativeLong(),
            "platinum");

        BytesReference reference = XContentHelper.toXContent(config, XContentType.JSON, ToXContent.EMPTY_PARAMS, false);
        Map<String, Object> objectMap = XContentHelper.convertToMap(reference, true, XContentType.JSON).v2();

        objectMap.put(TrainedModelConfig.COMPRESSED_DEFINITION.getPreferredName(), lazyModelDefinition.getCompressedString());

        try(XContentBuilder xContentBuilder = XContentFactory.jsonBuilder().map(objectMap);
            XContentParser parser = XContentType.JSON
                .xContent()
                .createParser(xContentRegistry(),
                    DeprecationHandler.THROW_UNSUPPORTED_OPERATION,
                    BytesReference.bytes(xContentBuilder).streamInput())) {
            IllegalArgumentException ex = expectThrows(IllegalArgumentException.class, () -> TrainedModelConfig.fromXContent(parser, true));
            assertThat(ex.getCause().getMessage(), equalTo("both [compressed_definition] and [definition] cannot be set."));
        }
    }

    public void testValidateWithNullDefinition() {
        ActionRequestValidationException ex = expectThrows(ActionRequestValidationException.class,
            () -> TrainedModelConfig.builder().validate());
        assertThat(ex.getMessage(), containsString("[definition] must not be null."));
    }

    public void testValidateWithInvalidID() {
        String modelId = "InvalidID-";
        ActionRequestValidationException ex = expectThrows(ActionRequestValidationException.class,
            () -> TrainedModelConfig.builder()
                .setParsedDefinition(TrainedModelDefinitionTests.createRandomBuilder())
                .setModelId(modelId).validate());
        assertThat(ex.getMessage(), containsString(Messages.getMessage(Messages.INVALID_ID, "model_id", modelId)));
    }

    public void testValidateWithLongID() {
        String modelId = IntStream.range(0, 100).mapToObj(x -> "a").collect(Collectors.joining());
        ActionRequestValidationException ex = expectThrows(ActionRequestValidationException.class,
            () -> TrainedModelConfig.builder()
                .setParsedDefinition(TrainedModelDefinitionTests.createRandomBuilder())
                .setModelId(modelId).validate());
        assertThat(ex.getMessage(),
            containsString(Messages.getMessage(Messages.ID_TOO_LONG, "model_id", modelId, MlStrings.ID_LENGTH_LIMIT)));
    }

    public void testValidateWithIllegallyUserProvidedFields() {
        String modelId = "simplemodel";
        ActionRequestValidationException ex = expectThrows(ActionRequestValidationException.class,
            () -> TrainedModelConfig.builder()
                .setParsedDefinition(TrainedModelDefinitionTests.createRandomBuilder())
                .setCreateTime(Instant.now())
                .setModelId(modelId).validate(true));
        assertThat(ex.getMessage(), containsString("illegal to set [create_time] at inference model creation"));

        ex = expectThrows(ActionRequestValidationException.class,
            () -> TrainedModelConfig.builder()
                .setParsedDefinition(TrainedModelDefinitionTests.createRandomBuilder())
                .setVersion(Version.CURRENT)
                .setModelId(modelId).validate(true));
        assertThat(ex.getMessage(), containsString("illegal to set [version] at inference model creation"));

        ex = expectThrows(ActionRequestValidationException.class,
            () -> TrainedModelConfig.builder()
                .setParsedDefinition(TrainedModelDefinitionTests.createRandomBuilder())
                .setCreatedBy("ml_user")
                .setModelId(modelId).validate(true));
        assertThat(ex.getMessage(), containsString("illegal to set [created_by] at inference model creation"));
    }

    public void testSerializationWithLazyDefinition() throws IOException {
        xContentTester(this::createParser,
            () -> {
            try {
                String compressedString = InferenceToXContentCompressor.deflate(TrainedModelDefinitionTests.createRandomBuilder().build());
                return createTestInstance(randomAlphaOfLength(10))
                    .setDefinitionFromString(compressedString)
                    .build();
            } catch (IOException ex) {
                fail(ex.getMessage());
                return null;
            }
            },
            ToXContent.EMPTY_PARAMS,
            (p) -> TrainedModelConfig.fromXContent(p, true).build())
            .numberOfTestRuns(NUMBER_OF_TEST_RUNS)
            .supportsUnknownFields(false)
            .shuffleFieldsExceptions(getShuffleFieldsExceptions())
            .randomFieldsExcludeFilter(getRandomFieldsExcludeFilter())
            .assertEqualsConsumer((def1, def2) -> {
                try {
                    assertThat(def1.ensureParsedDefinition(xContentRegistry()).getModelDefinition(),
                        equalTo(def2.ensureParsedDefinition(xContentRegistry()).getModelDefinition()));
                } catch(IOException ex) {
                    fail(ex.getMessage());
                }
            })
            .assertToXContentEquivalence(true)
            .test();
    }

    public void testSerializationWithCompressedLazyDefinition() throws IOException {
        xContentTester(this::createParser,
            () -> {
                try {
                    String compressedString =
                        InferenceToXContentCompressor.deflate(TrainedModelDefinitionTests.createRandomBuilder().build());
                    return createTestInstance(randomAlphaOfLength(10))
                        .setDefinitionFromString(compressedString)
                        .build();
                } catch (IOException ex) {
                    fail(ex.getMessage());
                    return null;
                }
            },
            new ToXContent.MapParams(Collections.singletonMap(TrainedModelConfig.DECOMPRESS_DEFINITION, "false")),
            (p) -> TrainedModelConfig.fromXContent(p, true).build())
            .numberOfTestRuns(NUMBER_OF_TEST_RUNS)
            .supportsUnknownFields(false)
            .shuffleFieldsExceptions(getShuffleFieldsExceptions())
            .randomFieldsExcludeFilter(getRandomFieldsExcludeFilter())
            .assertEqualsConsumer((def1, def2) -> {
                try {
                    assertThat(def1.ensureParsedDefinition(xContentRegistry()).getModelDefinition(),
                        equalTo(def2.ensureParsedDefinition(xContentRegistry()).getModelDefinition()));
                } catch(IOException ex) {
                    fail(ex.getMessage());
                }
            })
            .assertToXContentEquivalence(true)
            .test();
    }

    public void testIsAvailableWithLicense() {
        TrainedModelConfig.Builder builder = createTestInstance(randomAlphaOfLength(10));

        XPackLicenseState licenseState = mock(XPackLicenseState.class);
        when(licenseState.isActive()).thenReturn(false);
        when(licenseState.getOperationMode()).thenReturn(License.OperationMode.BASIC);

        assertFalse(builder.setLicenseLevel(License.OperationMode.PLATINUM.description()).build().isAvailableWithLicense(licenseState));
        assertTrue(builder.setLicenseLevel(License.OperationMode.BASIC.description()).build().isAvailableWithLicense(licenseState));


        when(licenseState.isActive()).thenReturn(true);
        when(licenseState.getOperationMode()).thenReturn(License.OperationMode.PLATINUM);
        assertTrue(builder.setLicenseLevel(License.OperationMode.PLATINUM.description()).build().isAvailableWithLicense(licenseState));
        assertTrue(builder.setLicenseLevel(License.OperationMode.BASIC.description()).build().isAvailableWithLicense(licenseState));
        assertTrue(builder.setLicenseLevel(License.OperationMode.GOLD.description()).build().isAvailableWithLicense(licenseState));

        when(licenseState.isActive()).thenReturn(false);
        assertFalse(builder.setLicenseLevel(License.OperationMode.PLATINUM.description()).build().isAvailableWithLicense(licenseState));
        assertTrue(builder.setLicenseLevel(License.OperationMode.BASIC.description()).build().isAvailableWithLicense(licenseState));
        assertFalse(builder.setLicenseLevel(License.OperationMode.GOLD.description()).build().isAvailableWithLicense(licenseState));

        when(licenseState.isActive()).thenReturn(true);
        when(licenseState.getOperationMode()).thenReturn(License.OperationMode.GOLD);
        assertFalse(builder.setLicenseLevel(License.OperationMode.PLATINUM.description()).build().isAvailableWithLicense(licenseState));
        assertTrue(builder.setLicenseLevel(License.OperationMode.BASIC.description()).build().isAvailableWithLicense(licenseState));
        assertTrue(builder.setLicenseLevel(License.OperationMode.GOLD.description()).build().isAvailableWithLicense(licenseState));

        when(licenseState.isActive()).thenReturn(false);
        assertFalse(builder.setLicenseLevel(License.OperationMode.PLATINUM.description()).build().isAvailableWithLicense(licenseState));
        assertTrue(builder.setLicenseLevel(License.OperationMode.BASIC.description()).build().isAvailableWithLicense(licenseState));
        assertFalse(builder.setLicenseLevel(License.OperationMode.GOLD.description()).build().isAvailableWithLicense(licenseState));
    }

}
