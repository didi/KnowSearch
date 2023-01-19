package org.elasticsearch.region;

import org.elasticsearch.common.Strings;
import org.elasticsearch.common.settings.Setting;

import java.util.Collections;
import java.util.List;

public class RegionSettings {
    public static final Setting.AffixSetting<List<String>> CLUSTER_REGION_SEEDS = Setting.affixKeySetting(
        "cluster.region.",
        "seeds",
        (ns, key) -> Setting.listSetting(
            key,
            // the default needs to be emptyList() when fallback is removed
            Collections.emptyList(),
            s -> {
                // validate seed address
                validate(s);
                return s;
            },
            Setting.Property.Dynamic,
            Setting.Property.NodeScope));

    private static void validate(String s) {
        if (Strings.isNullOrEmpty(s)) {
            throw new IllegalArgumentException("seed must not be null");
        }
    }
}
