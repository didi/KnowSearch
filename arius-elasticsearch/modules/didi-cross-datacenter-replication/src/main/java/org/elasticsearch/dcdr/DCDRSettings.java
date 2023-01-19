package org.elasticsearch.dcdr;

import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Setting.Property;

/**
 * author weizijun
 * date：2019-08-15
 */
public class DCDRSettings {
    // prevent construction
    private DCDRSettings() {

    }

    /**
     * Index setting for a dcdr replica index.
     */
    public static final Setting<Boolean> DCDR_REPLICA_INDEX_SETTING =
        Setting.boolSetting("index.dcdr.replica_index", false, Property.IndexScope, Property.Dynamic);
}
