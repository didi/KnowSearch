package org.elasticsearch.cluster.routing.allocation.allocator;

import org.elasticsearch.common.settings.Setting;

/**
 * author weizijun
 * date：2019-06-12
 */
public class IndexGroupSettings {
    public static final String INDEX_GROUP_NAME = "index.group.name";
    public static final String INDEX_GROUP_FACTOR = "index.group.factor";
    public static final String INDEX_TEMPLATE_NAME = "index.template";

    public static final Setting<String> INDEX_GROUP_NAME_SETTINGS = Setting.simpleString(INDEX_GROUP_NAME, Setting.Property.Dynamic, Setting.Property.IndexScope);
    public static final Setting<Float> INDEX_GROUP_FACTOR_SETTINGS = Setting.floatSetting(INDEX_GROUP_FACTOR, 1f, Setting.Property.Dynamic, Setting.Property.IndexScope);
    public static final Setting<String> INDEX_TEMPLATE_NAME_SETTINGS = Setting.simpleString(INDEX_TEMPLATE_NAME, Setting.Property.Dynamic, Setting.Property.IndexScope);
}
