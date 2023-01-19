package org.elasticsearch.ingest.indextemplate;

import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.index.VersionType;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.test.ESTestCase;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class IndexTemplateProcessorTests extends ESTestCase {

    /**
     * 测试field字段
     * 默认为timestamp + UNIX_MS
     */
    public void testExecuteField01() {
        Map<String, Object> source = new HashMap<>();
        Long createTime = System.currentTimeMillis();
        source.put("timestamp", createTime);

        String index = "index";

        IngestDocument ingestDocument = new IngestDocument(
            index,
            "type",
            "id",
            null,
            1L,
            VersionType.INTERNAL,
            source
        );

        String indexNameFormat = "_yyyy-MM-dd";
        IndexTemplateProcessor processor = new IndexTemplateProcessor(
            "tag",
            null,
            null,
            null,
            null,
            indexNameFormat,
            0,
            7,
            3
        );

        try {
            processor.execute(ingestDocument);
        } catch (Exception e) {
            fail();
        }

        DateTimeFormatter formatter = DateTimeFormat.forPattern(indexNameFormat);
        DateTime dateTime = new DateTime(createTime);

        assertEquals(index + formatter.print(dateTime), ingestDocument.getFieldValue("_index", String.class));
    }

    /**
     * 测试field字段
     * 字段不存在，默认写今天索引
     */
    public void testExecuteField02() {
        Map<String, Object> source = new HashMap<>();
        Long createTime = System.currentTimeMillis();
        // source.put("timestamp", createTime);

        String index = "index";

        IngestDocument ingestDocument = new IngestDocument(
            index,
            "type",
            "id",
            null,
            1L,
            VersionType.INTERNAL,
            source
        );

        String indexNameFormat = "_yyyy-MM-dd";
        IndexTemplateProcessor processor = new IndexTemplateProcessor(
            "tag",
            null,
            null,
            null,
            null,
            indexNameFormat,
            0,
            7,
            3
        );

        try {
            processor.execute(ingestDocument);
        } catch (Exception e) {
            fail();
        }

        DateTimeFormatter formatter = DateTimeFormat.forPattern(indexNameFormat);
        DateTime dateTime = new DateTime(createTime);

        assertEquals(index + formatter.print(dateTime), ingestDocument.getFieldValue("_index", String.class));
    }

    /**
     * 测试field字段
     * 指定时间字段，默认UNIX_MS
     */
    public void testExecuteField03() {
        Map<String, Object> source = new HashMap<>();
        Long createTime = System.currentTimeMillis();
        source.put("logTime", createTime);

        String index = "index";

        IngestDocument ingestDocument = new IngestDocument(
            index,
            "type",
            "id",
            null,
            1L,
            VersionType.INTERNAL,
            source
        );

        String indexNameFormat = "_yyyy-MM-dd";
        IndexTemplateProcessor processor = new IndexTemplateProcessor(
            "tag",
            "logTime",
            null,
            null,
            null,
            indexNameFormat,
            0,
            7,
            3
        );

        try {
            processor.execute(ingestDocument);
        } catch (Exception e) {
            fail();
        }

        DateTimeFormatter formatter = DateTimeFormat.forPattern(indexNameFormat);
        DateTime dateTime = new DateTime(createTime);

        assertEquals(index + formatter.print(dateTime), ingestDocument.getFieldValue("_index", String.class));
    }

    /**
     * 测试field字段
     * 指定时间字段，指定时间字段格式
     */
    public void testExecuteField04() {

        String field = "logTime";
        String fieldFormat = "yyyy-MM-dd HH:mm:ss";
        DateTimeFormatter formatter = DateTimeFormat.forPattern(fieldFormat);

        Map<String, Object> source = new HashMap<>();

        Long createTime = System.currentTimeMillis();
        source.put("logTime", formatter.print(createTime));

        String index = "index";

        IngestDocument ingestDocument = new IngestDocument(
            index,
            "type",
            "id",
            null,
            1L,
            VersionType.INTERNAL,
            source
        );

        String indexNameFormat = "_yyyy-MM-dd";
        IndexTemplateProcessor processor = new IndexTemplateProcessor(
            "tag",
            field,
            fieldFormat,
            null,
            null,
            indexNameFormat,
            0,
            7,
            3
        );

        try {
            processor.execute(ingestDocument);
        } catch (Exception e) {
            fail();
        }

        formatter = DateTimeFormat.forPattern(indexNameFormat);
        DateTime dateTime = new DateTime(createTime);

        assertEquals(index + formatter.print(dateTime), ingestDocument.getFieldValue("_index", String.class));
    }

    /**
     * 测试field字段
     * 指定时间字段，指定UNIX_MS
     */
    public void testExecuteField05() {

        String field = "logTime";
        String fieldFormat = "UNIX_MS";

        Map<String, Object> source = new HashMap<>();

        Long createTime = System.currentTimeMillis();
        source.put("logTime", createTime);

        String index = "index";

        IngestDocument ingestDocument = new IngestDocument(
            index,
            "type",
            "id",
            null,
            1L,
            VersionType.INTERNAL,
            source
        );

        String indexNameFormat = "_yyyy-MM-dd";
        IndexTemplateProcessor processor = new IndexTemplateProcessor(
            "tag",
            field,
            fieldFormat,
            null,
            null,
            indexNameFormat,
            0,
            7,
            3
        );

        try {
            processor.execute(ingestDocument);
        } catch (Exception e) {
            fail();
        }

        DateTimeFormatter formatter = DateTimeFormat.forPattern(indexNameFormat);
        DateTime dateTime = new DateTime(createTime);

        assertEquals(index + formatter.print(dateTime), ingestDocument.getFieldValue("_index", String.class));
    }

    /**
     * 测试field字段
     * 指定时间字段，指定UNIX
     */
    public void testExecuteField06() {

        String field = "logTime";
        String fieldFormat = "UNIX";

        Map<String, Object> source = new HashMap<>();

        Long createTime = System.currentTimeMillis();
        source.put("logTime", createTime / 1000);

        String index = "index";

        IngestDocument ingestDocument = new IngestDocument(
            index,
            "type",
            "id",
            null,
            1L,
            VersionType.INTERNAL,
            source
        );

        String indexNameFormat = "_yyyy-MM-dd";
        IndexTemplateProcessor processor = new IndexTemplateProcessor(
            "tag",
            field,
            fieldFormat,
            null,
            null,
            indexNameFormat,
            0,
            7,
            3
        );

        try {
            processor.execute(ingestDocument);
        } catch (Exception e) {
            fail();
        }

        DateTimeFormatter formatter = DateTimeFormat.forPattern(indexNameFormat);
        DateTime dateTime = new DateTime(createTime);

        assertEquals(index + formatter.print(dateTime), ingestDocument.getFieldValue("_index", String.class));
    }

    /**
     * 测试field字段
     * 指定时间字段，指定时间字段格式yyyy-MM-dd HH:mm:ss Z
     */
    public void testExecuteField07() {

        String field = "logTime";
        String fieldFormat = "yyyy-MM-dd HH:mm:ss Z";
        DateTimeFormatter formatter = DateTimeFormat.forPattern(fieldFormat);

        Map<String, Object> source = new HashMap<>();

        Long createTime = System.currentTimeMillis();
        source.put("logTime", formatter.print(createTime));

        String index = "index";

        IngestDocument ingestDocument = new IngestDocument(
            index,
            "type",
            "id",
            null,
            1L,
            VersionType.INTERNAL,
            source
        );

        String indexNameFormat = "_yyyy-MM-dd";
        IndexTemplateProcessor processor = new IndexTemplateProcessor(
            "tag",
            field,
            fieldFormat,
            null,
            null,
            indexNameFormat,
            0,
            7,
            3
        );

        try {
            processor.execute(ingestDocument);
        } catch (Exception e) {
            fail();
        }

        formatter = DateTimeFormat.forPattern(indexNameFormat);
        DateTime dateTime = new DateTime(createTime);

        assertEquals(index + formatter.print(dateTime), ingestDocument.getFieldValue("_index", String.class));
    }

    /**
     * 测试field字段
     * 时间字段不存在
     */
    public void testExecuteField08() {

        String field = "logTime";

        Map<String, Object> source = new HashMap<>();

        String index = "index";

        IngestDocument ingestDocument = new IngestDocument(
            index,
            "type",
            "id",
            null,
            1L,
            VersionType.INTERNAL,
            source
        );

        String indexNameFormat = "_yyyy-MM-dd";
        IndexTemplateProcessor processor = new IndexTemplateProcessor(
            "tag",
            field,
            null,
            null,
            null,
            indexNameFormat,
            0,
            7,
            3
        );

        try {
            processor.execute(ingestDocument);
        } catch (Exception e) {
            fail();
        }

        DateTimeFormatter formatter = DateTimeFormat.forPattern(indexNameFormat);
        DateTime dateTime = new DateTime(System.currentTimeMillis());

        assertEquals(index + formatter.print(dateTime), ingestDocument.getFieldValue("_index", String.class));
    }

    /**
     * 测试field字段
     * 指定时间字段，按月创建
     */
    public void testExecuteField09() {

        String field = "logTime";
        String fieldFormat = "yyyy-MM-dd HH:mm:ss Z";
        DateTimeFormatter formatter = DateTimeFormat.forPattern(fieldFormat);

        Map<String, Object> source = new HashMap<>();

        Long createTime = System.currentTimeMillis();
        source.put("logTime", formatter.print(createTime));

        String index = "index";

        IngestDocument ingestDocument = new IngestDocument(
            index,
            "type",
            "id",
            null,
            1L,
            VersionType.INTERNAL,
            source
        );

        String indexNameFormat = "_yyyy-MM";
        IndexTemplateProcessor processor = new IndexTemplateProcessor(
            "tag",
            field,
            fieldFormat,
            null,
            null,
            indexNameFormat,
            0,
            7,
            3
        );

        try {
            processor.execute(ingestDocument);
        } catch (Exception e) {
            fail();
        }

        formatter = DateTimeFormat.forPattern(indexNameFormat);
        DateTime dateTime = new DateTime(createTime);

        assertEquals(index + formatter.print(dateTime), ingestDocument.getFieldValue("_index", String.class));
    }

    /**
     * 测试field字段
     * 指定时间字段，不分区
     */
    public void testExecuteField10() {

        String field = "logTime";
        String fieldFormat = "yyyy-MM-dd HH:mm:ss Z";
        DateTimeFormatter formatter = DateTimeFormat.forPattern(fieldFormat);

        Map<String, Object> source = new HashMap<>();

        Long createTime = System.currentTimeMillis();
        source.put("logTime", formatter.print(createTime));

        String index = "index";

        IngestDocument ingestDocument = new IngestDocument(
            index,
            "type",
            "id",
            null,
            1L,
            VersionType.INTERNAL,
            source
        );

        String indexNameFormat = "";
        IndexTemplateProcessor processor = new IndexTemplateProcessor(
            "tag",
            field,
            fieldFormat,
            null,
            null,
            indexNameFormat,
            0,
            7,
            3
        );

        try {
            processor.execute(ingestDocument);
        } catch (Exception e) {
            fail();
        }

        assertEquals(index, ingestDocument.getFieldValue("_index", String.class));
    }

    /**
     * 测试indexVersion字段
     */
    public void testExecuteIndexVersion01() {

        String field = "logTime";
        String fieldFormat = "yyyy-MM-dd HH:mm:ss Z";
        DateTimeFormatter formatter = DateTimeFormat.forPattern(fieldFormat);

        Map<String, Object> source = new HashMap<>();

        Long createTime = System.currentTimeMillis();
        source.put("logTime", formatter.print(createTime));

        String index = "index";

        IngestDocument ingestDocument = new IngestDocument(
            index,
            "type",
            "id",
            null,
            1L,
            VersionType.INTERNAL,
            source
        );

        String indexNameFormat = "_yyyy-MM-dd";
        IndexTemplateProcessor processor = new IndexTemplateProcessor(
            "tag",
            field,
            fieldFormat,
            null,
            null,
            indexNameFormat,
            1,
            7,
            3
        );

        try {
            processor.execute(ingestDocument);
        } catch (Exception e) {
            fail();
        }

        formatter = DateTimeFormat.forPattern(indexNameFormat);
        DateTime dateTime = new DateTime(createTime);

        assertEquals(index + formatter.print(dateTime) + "_v1", ingestDocument.getFieldValue("_index", String.class));
    }

    /**
     * 测试expireDay字段
     */
    public void testExecuteExpireDay01() {

        String field = "logTime";
        String fieldFormat = "yyyy-MM-dd HH:mm:ss Z";
        DateTimeFormatter formatter = DateTimeFormat.forPattern(fieldFormat);

        Map<String, Object> source = new HashMap<>();

        Long createTime = System.currentTimeMillis() - 4 * 86400000;
        source.put("logTime", formatter.print(createTime));

        String index = "index";

        IngestDocument ingestDocument = new IngestDocument(
            index,
            "type",
            "id",
            null,
            1L,
            VersionType.INTERNAL,
            source
        );

        String indexNameFormat = "_yyyy-MM-dd";
        IndexTemplateProcessor processor = new IndexTemplateProcessor(
            "tag",
            field,
            fieldFormat,
            null,
            null,
            indexNameFormat,
            1,
            3,
            3
        );

        try {
            ingestDocument = processor.execute(ingestDocument);
        } catch (Exception e) {
            fail();
        }

        assertNull(ingestDocument);
    }

    /**
     * 测试expireDay字段
     */
    public void testExecuteExpireDay02() {

        String field = "logTime";
        String fieldFormat = "yyyy-MM-dd HH:mm:ss Z";
        DateTimeFormatter formatter = DateTimeFormat.forPattern(fieldFormat);

        Map<String, Object> source = new HashMap<>();

        Long createTime = System.currentTimeMillis() - 4 * 86400000;
        source.put("logTime", formatter.print(createTime));

        String index = "index";

        IngestDocument ingestDocument = new IngestDocument(
            index,
            "type",
            "id",
            null,
            1L,
            VersionType.INTERNAL,
            source
        );

        String indexNameFormat = "_yyyy-MM-dd";
        IndexTemplateProcessor processor = new IndexTemplateProcessor(
            "tag",
            field,
            fieldFormat,
            null,
            null,
            indexNameFormat,
            1,
            -1,
            3
        );

        try {
            ingestDocument = processor.execute(ingestDocument);
        } catch (Exception e) {
            fail();
        }

        assertNotNull(ingestDocument);
    }

    /**
     * 测试id字段
     * 一个id字段
     *
     */
    public void testExecuteIdField01() {
        Map<String, Object> source = new HashMap<>();
        source.put("id", 100);

        IngestDocument ingestDocument = new IngestDocument(
            "index",
            "type",
            null,
            null,
            1L,
            VersionType.INTERNAL,
            source
        );

        IndexTemplateProcessor processor = new IndexTemplateProcessor("tag", null, null, "id", null, null, 0, -1, 3);

        try {
            processor.execute(ingestDocument);
        } catch (Exception e) {
            fail();
        }

        assertEquals("100", ingestDocument.getFieldValue("_id", String.class));
    }

    /**
     * 测试id字段
     * 多个id字段
     *
     */
    public void testExecuteIdField02() {
        Map<String, Object> source = new HashMap<>();
        source.put("id1", 100);
        source.put("id2", "200");

        IngestDocument ingestDocument = new IngestDocument(
            "index",
            "type",
            null,
            null,
            1L,
            VersionType.INTERNAL,
            source
        );

        IndexTemplateProcessor processor = new IndexTemplateProcessor("tag", null, null, "id1,id2", null, null, 0, -1, 3);

        try {
            processor.execute(ingestDocument);
        } catch (Exception e) {
            fail();
        }

        assertEquals("100_200", ingestDocument.getFieldValue("_id", String.class));
    }

    /**
     * 测试id字段
     * id字段不存在
     *
     */
    public void testExecuteIdField03() {
        Map<String, Object> source = new HashMap<>();
        source.put("id1", 100);

        IngestDocument ingestDocument = new IngestDocument(
            "index",
            "type",
            null,
            null,
            1L,
            VersionType.INTERNAL,
            source
        );

        IndexTemplateProcessor processor = new IndexTemplateProcessor("tag", null, null, "id1,id2", null, null, 0, -1, 3);

        try {
            processor.execute(ingestDocument);
            fail();
        } catch (Exception e) {
            assertTrue(true);
        }

    }

    /**
     * 测试id字段
     * 不修改用户设置的ID
     *
     */
    public void testExecuteIdField04() {
        Map<String, Object> source = new HashMap<>();
        source.put("id1", 100);

        IngestDocument ingestDocument = new IngestDocument(
            "index",
            "type",
            "custom_id",
            null,
            1L,
            VersionType.INTERNAL,
            source
        );

        IndexTemplateProcessor processor = new IndexTemplateProcessor("tag", null, null, "id1", null, null, 0, -1, 3);

        try {
            processor.execute(ingestDocument);
        } catch (Exception e) {
            fail();
        }
        assertEquals("custom_id", ingestDocument.getFieldValue("_id", String.class));
    }

    /**
     * 测试routing字段
     * 一个routing字段
     *
     */
    public void testExecuteRoutingField01() {
        Map<String, Object> source = new HashMap<>();
        source.put("routing", "routing_content");

        IngestDocument ingestDocument = new IngestDocument(
            "index",
            "type",
            null,
            null,
            1L,
            VersionType.INTERNAL,
            source
        );

        IndexTemplateProcessor processor = new IndexTemplateProcessor("tag", null, null, null, "routing", null, 0, -1, 3);

        try {
            processor.execute(ingestDocument);
        } catch (Exception e) {
            fail();
        }

        assertEquals("routing_content", ingestDocument.getFieldValue("_routing", String.class));
    }

    /**
     * 测试routing字段
     * 多个routing字段
     *
     */
    public void testExecuteRoutingField02() {
        Map<String, Object> source = new HashMap<>();
        source.put("routing1", "routing1");
        source.put("routing2", "routing2");

        IngestDocument ingestDocument = new IngestDocument(
            "index",
            "type",
            null,
            null,
            1L,
            VersionType.INTERNAL,
            source
        );

        IndexTemplateProcessor processor = new IndexTemplateProcessor(
            "tag",
            null,
            null,
            null,
            "routing1,routing2",
            null,
            0,
            -1,
            3
        );

        try {
            processor.execute(ingestDocument);
        } catch (Exception e) {
            fail();
        }

        assertEquals("routing1_routing2", ingestDocument.getFieldValue("_routing", String.class));
    }

    /**
     * 测试routing字段
     * routing字段不存在
     *
     */
    public void testExecuteRoutingField03() {
        Map<String, Object> source = new HashMap<>();
        source.put("routing1", "routing");

        IngestDocument ingestDocument = new IngestDocument(
            "index",
            "type",
            null,
            null,
            1L,
            VersionType.INTERNAL,
            source
        );

        IndexTemplateProcessor processor = new IndexTemplateProcessor(
            "tag",
            null,
            null,
            null,
            "routing1,routing2",
            null,
            0,
            -1,
            3
        );

        try {
            processor.execute(ingestDocument);
            fail();
        } catch (Exception e) {
            assertTrue(true);
        }

    }

    /**
     * 测试routing字段
     * 不修改用户指定的routing字段
     *
     */
    public void testExecuteRoutingField04() {
        Map<String, Object> source = new HashMap<>();
        source.put("routing1", "routing");

        IngestDocument ingestDocument = new IngestDocument(
            "index",
            "type",
            null,
            "custom_routing",
            1L,
            VersionType.INTERNAL,
            source
        );

        IndexTemplateProcessor processor = new IndexTemplateProcessor("tag", null, null, null, "routing1", null, 0, -1, 3);

        try {
            processor.execute(ingestDocument);
        } catch (Exception e) {
            fail();
        }

        assertEquals("custom_routing", ingestDocument.getFieldValue("_routing", String.class));
    }

    /**
     * 测试futureDay字段
     */
    public void testExecuteFutureDay01() {

        String field = "logTime";
        String fieldFormat = "yyyy-MM-dd HH:mm:ss Z";
        DateTimeFormatter formatter = DateTimeFormat.forPattern(fieldFormat);

        Map<String, Object> source = new HashMap<>();

        Long createTime = System.currentTimeMillis() + 4 * 86400000;
        source.put("logTime", formatter.print(createTime));

        String index = "index";

        IngestDocument ingestDocument = new IngestDocument(
            index,
            "type",
            "id",
            null,
            1L,
            VersionType.INTERNAL,
            source
        );

        String indexNameFormat = "_yyyy-MM-dd";
        IndexTemplateProcessor processor = new IndexTemplateProcessor(
            "tag",
            field,
            fieldFormat,
            null,
            null,
            indexNameFormat,
            1,
            3,
            3
        );

        try {
            ingestDocument = processor.execute(ingestDocument);
        } catch (Exception e) {
            fail();
        }

        assertNull(ingestDocument);
    }

    /**
     * 测试futureDay字段
     */
    public void testExecuteFutureDay02() {

        String field = "logTime";
        String fieldFormat = "yyyy-MM-dd HH:mm:ss Z";
        DateTimeFormatter formatter = DateTimeFormat.forPattern(fieldFormat);

        Map<String, Object> source = new HashMap<>();

        Long createTime = System.currentTimeMillis() + 2 * 86400000;
        source.put("logTime", formatter.print(createTime));

        String index = "index";

        IngestDocument ingestDocument = new IngestDocument(
            index,
            "type",
            "id",
            null,
            1L,
            VersionType.INTERNAL,
            source
        );

        String indexNameFormat = "_yyyy-MM-dd";
        IndexTemplateProcessor processor = new IndexTemplateProcessor(
            "tag",
            field,
            fieldFormat,
            null,
            null,
            indexNameFormat,
            1,
            3,
            3
        );

        try {
            ingestDocument = processor.execute(ingestDocument);
        } catch (Exception e) {
            fail();
        }

        assertNotNull(ingestDocument);
    }

}
