/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.watcher.notification.email.attachment;

import org.elasticsearch.ElasticsearchParseException;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.xpack.core.watcher.execution.WatchExecutionContext;
import org.elasticsearch.xpack.core.watcher.watch.Payload;
import org.elasticsearch.xpack.watcher.common.http.HttpRequestTemplate;
import org.elasticsearch.xpack.watcher.common.http.Scheme;
import org.elasticsearch.xpack.watcher.notification.email.Attachment;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;

public class EmailAttachmentParsersTests extends ESTestCase {

    private WatchExecutionContext ctx = mock(WatchExecutionContext.class);

    public void testThatCustomParsersCanBeRegistered() throws Exception {
        Map<String, EmailAttachmentParser> parsers = new HashMap<>();
        parsers.put("test", new TestEmailAttachmentParser());
        EmailAttachmentsParser parser = new EmailAttachmentsParser(parsers);

        XContentBuilder builder = jsonBuilder();
        builder.startObject()
                .startObject("my-id")
                .startObject("test")
                .field("foo", "bar")
                .endObject()
                .endObject()
                .startObject("my-other-id")
                .startObject("test")
                .field("foo", "baz")
                .endObject()
                .endObject()
                .endObject();

        logger.info("JSON: {}", Strings.toString(builder));
        XContentParser xContentParser = createParser(builder);
        EmailAttachments attachments = parser.parse(xContentParser);
        assertThat(attachments.getAttachments(), hasSize(2));

        List<EmailAttachmentParser.EmailAttachment> emailAttachments = new ArrayList<>(attachments.getAttachments());
        EmailAttachmentParser.EmailAttachment emailAttachment = emailAttachments.get(0);
        assertThat(emailAttachment, instanceOf(TestEmailAttachment.class));

        Attachment attachment = parsers.get("test").toAttachment(ctx, new Payload.Simple(), emailAttachment);
        assertThat(attachment.name(), is("my-id"));
        assertThat(attachment.contentType(), is("personalContentType"));

        assertThat(parsers.get("test").toAttachment(ctx, new Payload.Simple(), emailAttachments.get(1)).id(), is("my-other-id"));
    }

    public void testThatUnknownParserThrowsException() throws IOException {
        EmailAttachmentsParser parser = new EmailAttachmentsParser(Collections.emptyMap());

        XContentBuilder builder = jsonBuilder();
        String type = randomAlphaOfLength(8);
        builder.startObject().startObject("some-id").startObject(type).endObject().endObject().endObject();

        XContentParser xContentParser = createParser(builder);
        try {
            parser.parse(xContentParser);
            fail("Expected random parser of type [" + type + "] to throw an exception");
        } catch (ElasticsearchParseException e) {
            assertThat(e.getMessage(), containsString("Cannot parse attachment of type [" + type + "]"));
        }
    }

    public void testThatToXContentSerializationWorks() throws Exception {
        List<EmailAttachmentParser.EmailAttachment> attachments = new ArrayList<>();
        attachments.add(new DataAttachment("my-name.json", org.elasticsearch.xpack.watcher.notification.email.DataAttachment.JSON));

        HttpRequestTemplate requestTemplate = HttpRequestTemplate.builder("localhost", 80).scheme(Scheme.HTTP).path("/").build();
        boolean inline = randomBoolean();
        HttpRequestAttachment httpRequestAttachment = new HttpRequestAttachment("other-id", requestTemplate, inline, null);

        attachments.add(httpRequestAttachment);
        EmailAttachments emailAttachments = new EmailAttachments(attachments);
        XContentBuilder builder = jsonBuilder();
        builder.startObject();
        emailAttachments.toXContent(builder, ToXContent.EMPTY_PARAMS);
        builder.endObject();
        logger.info("JSON is: {}", Strings.toString(builder));
        assertThat(Strings.toString(builder), containsString("my-name.json"));
        assertThat(Strings.toString(builder), containsString("json"));
        assertThat(Strings.toString(builder), containsString("other-id"));
        assertThat(Strings.toString(builder), containsString("localhost"));
        assertThat(Strings.toString(builder), containsString("/"));
        if (inline) {
            assertThat(Strings.toString(builder), containsString("inline"));
        }
    }

    public class TestEmailAttachmentParser implements EmailAttachmentParser<TestEmailAttachment> {

        @Override
        public String type() {
            return "test";
        }

        @Override
        public TestEmailAttachment parse(String id, XContentParser parser) throws IOException {
            TestEmailAttachment attachment = null;
            String currentFieldName = null;
            XContentParser.Token token;
            while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
                if (token == XContentParser.Token.FIELD_NAME) {
                    currentFieldName = parser.currentName();
                } else {
                    if ("foo".equals(currentFieldName)) {
                        attachment = new TestEmailAttachment(id, parser.text());
                    }
                }
            }

            if (attachment == null) {
                throw new ElasticsearchParseException("Expected test parser to have field [foo]");
            }

            return attachment;
        }

        @Override
        public Attachment toAttachment(WatchExecutionContext ctx, Payload payload, TestEmailAttachment attachment) {
            return new Attachment.Bytes(attachment.id(), attachment.getValue().getBytes(StandardCharsets.UTF_8),
                                        "personalContentType", false);
        }
    }

    public static class TestEmailAttachment implements EmailAttachmentParser.EmailAttachment {

        private final String value;
        private final String id;

        interface Fields {
            ParseField FOO = new ParseField("foo");
        }

        public TestEmailAttachment(String id, String value) {
            this.id = id;
            this.value = value;
        }

        @Override
        public String type() {
            return "test";
        }

        public String getValue() {
            return value;
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public boolean inline() {
            return false;
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            return builder.startObject(id)
                    .startObject(type())
                    .field(Fields.FOO.getPreferredName(), value)
                    .endObject()
                    .endObject();
        }
    }
}
