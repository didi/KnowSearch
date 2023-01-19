package org.elasticsearch.ingest.indextemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.elasticsearch.ingest.AbstractProcessor;
import org.elasticsearch.ingest.ConfigurationUtils;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.Processor;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * author weizijun
 * date：2019-05-24
 */
public class IndexTemplateProcessor extends AbstractProcessor {
    public static final String TYPE = "index_template";
    public static final long ONE_DAY_MILLIS = 86400000;
    public static final long EIGHT_HOURS_MILLIS = 28800000;
    public static final String MULTI_FIELD_SPLIT_STR = ",";
    public static final String MULTI_FIELD_CONTENT_JOIN_STR = "_";

    private static final String INDEX_TIME_FIELD = "field";
    private static final String DEFAULT_TIME_FIELD = "timestamp";

    private static final String INDEX_TIME_FIELD_FORMAT = "field_format";
    private static final String DEFAULT_MS_INDEX_TIME_FIELD_FORMAT = "UNIX_MS";
    private static final String DEFAULT_S_INDEX_TIME_FIELD_FORMAT = "UNIX";

    private static final String INDEX_ID_FIELD = "id_field";
    private static final String INDEX_ROUTING_FIELD = "routing_field";
    private static final String INDEX_NAME_FORMAT_FIELD = "index_name_format";
    private static final String INDEX_VERSION_FIELD = "index_version";
    private static final String INDEX_EXPIRE_DAY_FIELD = "expire_day";
    private static final String INDEX_FUTURE_DAY_FIELD = "future_day";

    private String field;
    private String fieldFormat;
    private String idField;
    private String routingField;
    private String indexNameFormat;
    private int indexVersion;
    private long expireMillis;
    private long futureMillis;

    public IndexTemplateProcessor(
        String tag, String field, String fieldFormat, String idField, String routingField,
        String indexNameFormat, int indexVersion, int expireDay, int futureDay
    ) {
        super(tag);
        this.field = field;
        this.fieldFormat = fieldFormat;
        this.idField = idField;
        this.routingField = routingField;
        this.indexNameFormat = indexNameFormat;
        this.indexVersion = indexVersion;
        this.expireMillis = (expireDay - 1) * ONE_DAY_MILLIS;
        this.futureMillis = (futureDay + 1) * ONE_DAY_MILLIS;
    }

    @Override
    public IngestDocument execute(IngestDocument ingestDocument) throws Exception {
        Long createTime = analyzeTimeField2MilliSecond(ingestDocument, field, fieldFormat);

        // 判断是否是过期数据
        if (indexNameFormat != null && indexNameFormat.length() > 0) {
            Long earliestTime = getEarliestTime();
            if (createTime < earliestTime) {
                // expire data, drop
                return null;
            }

            Long latestTime = getLatestTime();
            if (createTime > latestTime) {
                // expire data, drop
                return null;
            }
        }

        // 生成索引名字
        refreshIndexName(ingestDocument, indexNameFormat, createTime, indexVersion);

        // 生成ID字段
        refreshIdField(ingestDocument, idField);

        // 生成routing字段
        refreshRoutingField(ingestDocument, routingField);

        return ingestDocument;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public static final class Factory implements Processor.Factory {

        @Override
        public IndexTemplateProcessor create(
            Map<String, Processor.Factory> registry,
            String tag,
            Map<String, Object> config
        ) throws Exception {
            String field = ConfigurationUtils.readStringProperty(
                TYPE,
                tag,
                config,
                INDEX_TIME_FIELD,
                DEFAULT_TIME_FIELD
            );
            String fieldFormat = ConfigurationUtils.readStringProperty(
                TYPE,
                tag,
                config,
                INDEX_TIME_FIELD_FORMAT,
                DEFAULT_MS_INDEX_TIME_FIELD_FORMAT
            );
            String idField = ConfigurationUtils.readOptionalStringProperty(TYPE, tag, config, INDEX_ID_FIELD);
            String routingField = ConfigurationUtils.readOptionalStringProperty(TYPE, tag, config, INDEX_ROUTING_FIELD);
            String indexNameFormat = ConfigurationUtils.readOptionalStringProperty(TYPE, tag, config, INDEX_NAME_FORMAT_FIELD);
            int indexVersion = ConfigurationUtils.readIntProperty(TYPE, tag, config, INDEX_VERSION_FIELD, 0);
            int expireDay = ConfigurationUtils.readIntProperty(TYPE, tag, config, INDEX_EXPIRE_DAY_FIELD, -1);
            int futureDay = ConfigurationUtils.readIntProperty(TYPE, tag, config, INDEX_FUTURE_DAY_FIELD, 3);

            return new IndexTemplateProcessor(
                tag,
                field,
                fieldFormat,
                idField,
                routingField,
                indexNameFormat,
                indexVersion,
                expireDay,
                futureDay
            );
        }
    }

    private void refreshRoutingField(IngestDocument ingestDocument, String routingField) {

        if (ingestDocument.getSourceAndMetadata().containsKey(IngestDocument.MetaData.ROUTING.getFieldName())) {
            return;
        }

        if (routingField == null || "".equals(routingField)) {
            return;
        }

        List<String> routingList = new ArrayList<>();
        for (String routing : routingField.split(MULTI_FIELD_SPLIT_STR)) {
            routingList.add(String.valueOf(ingestDocument.getFieldValue(routing, Object.class)));
        }

        ingestDocument.setFieldValue(
            IngestDocument.MetaData.ROUTING.getFieldName(),
            String.join(MULTI_FIELD_CONTENT_JOIN_STR, routingList)
        );
    }

    private void refreshIdField(IngestDocument ingestDocument, String idField) {

        if (ingestDocument.getFieldValue(IngestDocument.MetaData.ID.getFieldName(), String.class) != null) {
            return;
        }

        if (idField == null || "".equals(idField)) {
            return;
        }

        List<String> idList = new ArrayList<>();
        for (String id : idField.split(MULTI_FIELD_SPLIT_STR)) {
            idList.add(String.valueOf(ingestDocument.getFieldValue(id, Object.class)));
        }

        ingestDocument.setFieldValue(
            IngestDocument.MetaData.ID.getFieldName(),
            String.join(MULTI_FIELD_CONTENT_JOIN_STR, idList)
        );
    }

    private void refreshIndexName(
        IngestDocument ingestDocument,
        String indexNameFormat,
        Long createTime,
        int indexVersion
    ) {
        String indexExpression = ingestDocument.getFieldValue(
            IngestDocument.MetaData.INDEX.getFieldName(),
            String.class
        );
        if (indexExpression.endsWith("*")) {
            indexExpression = indexExpression.substring(0, indexExpression.length() - 1);
        }

        StringBuilder indexNameBuilder = new StringBuilder();
        indexNameBuilder.append(indexExpression);

        if (indexNameFormat != null && indexNameFormat.length() > 0) {
            DateTimeFormatter formatter = DateTimeFormat.forPattern(indexNameFormat);
            DateTime dateTime = new DateTime(createTime);
            indexNameBuilder.append(formatter.print(dateTime));
        }

        if (indexVersion > 0) {
            indexNameBuilder.append("_v").append(indexVersion);
        }

        ingestDocument.setFieldValue(IngestDocument.MetaData.INDEX.getFieldName(), indexNameBuilder.toString());
    }

    private Long getEarliestTime() {
        if (expireMillis >= 0) {
            long now = System.currentTimeMillis();
            return now - (now + EIGHT_HOURS_MILLIS) % ONE_DAY_MILLIS - expireMillis;
        }
        return 0L;
    }

    private Long getLatestTime() {
        long now = System.currentTimeMillis();
        if (futureMillis >= 0) {
            return now - (now + EIGHT_HOURS_MILLIS) % ONE_DAY_MILLIS + futureMillis - 1;
        }
        return Long.MAX_VALUE;
    }

    private Long analyzeTimeField2MilliSecond(IngestDocument ingestDocument, String field, String fieldFormat) {
        try {
            if (fieldFormat.equals(DEFAULT_MS_INDEX_TIME_FIELD_FORMAT)) {
                return getLongFromIngestDocument(ingestDocument, field);
            }

            if (fieldFormat.equals(DEFAULT_S_INDEX_TIME_FIELD_FORMAT)) {
                return 1000 * getLongFromIngestDocument(ingestDocument, field);
            }

            String strCreateTime = ingestDocument.getFieldValue(field, String.class);
            DateTimeFormatter formatter = DateTimeFormat.forPattern(fieldFormat);
            return formatter.parseMillis(strCreateTime);

        } catch (Exception e) {

        }
        // return current time
        return System.currentTimeMillis();
    }

    private Long getLongFromIngestDocument(IngestDocument ingestDocument, String field) {
        Object object = ingestDocument.getFieldValue(field, Object.class);
        if (object instanceof Long) {
            return (Long) object;
        }

        if (object instanceof Integer) {
            return (long) (Integer) object;
        }

        return Long.valueOf(String.valueOf(object));
    }

}
