package com.didichuxing.datachannel.arius.admin.common.constant.template;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

import static com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateLabelAttributeEnum.SYS;
import static com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateLabelAttributeEnum.USER;
import static com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateLabelLevelEnum.*;
import static com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateLabelSourceEnum.*;

/**
 * @author didi
 */
public enum TemplateLabelEnum {

    /**
     *
     */
    DSL_NEED_REVIEW("01", "DSL查询需要审核", ADMIN, SYS, GREEN, TemplateLabelPeriodEnum.FOREVER),

    /**
     *
     */
    IMPORTANT("02", "重要索引", ADMIN, SYS, GREEN, TemplateLabelPeriodEnum.FOREVER),

    /**
     *
     */
    FULL_LINK_SUPPORT("03", "全链路保证", ADMIN, USER, GREEN, TemplateLabelPeriodEnum.FOREVER),

    /**
     *
     */
    SUSPEND_GOVERN("04", "暂停治理", ADMIN, USER, GREEN, TemplateLabelPeriodEnum.THREE_MONTH),

    /**
     *
     */
    QUOTA_USAGE_LOW("20", "Quota利用率过低", PLATFORM_GOVERN, USER, YELLOW, TemplateLabelPeriodEnum.FOREVER),

    /**
     *
     */
    QUOTA_USAGE_HIGH("21", "Quota利用率过高", PLATFORM_GOVERN, USER, RED, TemplateLabelPeriodEnum.FOREVER),

    /**
     *
     */
    EXPIRE_DAYS_LONG("22", "保存周期过长", PLATFORM_GOVERN, USER, YELLOW, TemplateLabelPeriodEnum.FOREVER),

    /**
     *
     */
    HAS_DELETED_DOC("40", "有删除操作", AMS, SYS, GREEN, TemplateLabelPeriodEnum.FOREVER),

    /**
     *
     */
    VALUE_LOW("41", "低价值", AMS, USER, YELLOW, TemplateLabelPeriodEnum.FOREVER),

    /**
     *
     */
    MAPPING_NEED_IMPROVE("42", "mapping待优化", AMS, USER, YELLOW, TemplateLabelPeriodEnum.FOREVER),

    /**
     *
     */
    QUERY_ERROR("43", "异常查询", AMS, USER, YELLOW, TemplateLabelPeriodEnum.FOREVER),

    /**
     *
     */
    QUERY_SLOW("44", "慢查询", AMS, USER, YELLOW, TemplateLabelPeriodEnum.FOREVER),

    /**
     *
     */
    NO_DATA("45", "无数据", AMS, USER, RED, TemplateLabelPeriodEnum.FOREVER),

    /**
     *
     */
    NO_WRITE("46", "无写入", AMS, USER, YELLOW, TemplateLabelPeriodEnum.FOREVER),

    /**
     *
     */
    NO_QUERY("47", "无查询", AMS, USER, RED, TemplateLabelPeriodEnum.FOREVER),

    /**
     *
     */
    INVALID("48", "废弃索引", AMS, USER, RED, TemplateLabelPeriodEnum.FOREVER);


    public static final String LABEL_ID = "labelId";

    public static final String LABEL_NAME = "labelName";

    private final String id;

    private final String name;

    private final TemplateLabelSourceEnum source;

    private final TemplateLabelAttributeEnum attribute;

    private final TemplateLabelLevelEnum level;

    private final TemplateLabelPeriodEnum period;

    TemplateLabelEnum(String id, String name, TemplateLabelSourceEnum source, TemplateLabelAttributeEnum attribute, TemplateLabelLevelEnum level, TemplateLabelPeriodEnum period) {
        this.id = id;
        this.name = name;
        this.source = source;
        this.attribute = attribute;
        this.level = level;
        this.period = period;
    }

    public static TemplateLabelEnum valueBy(String labelId) {
        for (TemplateLabelEnum labelEnum : TemplateLabelEnum.values()) {
            if (labelEnum.getId().equals(labelId)) {
                return labelEnum;
            }
        }
        return null;
    }

    public String getId() {
        return source.getId() + attribute.getId() + level.getId() + id;
    }

    public String getName() {
        return name;
    }

    public TemplateLabelSourceEnum getSource() {
        return source;
    }

    public TemplateLabelAttributeEnum getAttribute() {
        return attribute;
    }

    public TemplateLabelLevelEnum getLevel() {
        return level;
    }

    public TemplateLabelPeriodEnum getPeriod() {
        return period;
    }

    public static List<Map<String, Object>> getLabelList() {
        List<Map<String, Object>> list = Lists.newArrayList();
        for (TemplateLabelEnum labelEnum : TemplateLabelEnum.values()) {
            Map<String, Object> map = Maps.newHashMap();
            map.put(LABEL_ID, labelEnum.getId());
            map.put(LABEL_NAME, labelEnum.getName());
            list.add(map);
        }
        return list;
    }

}
