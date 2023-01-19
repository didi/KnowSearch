package org.elasticsearch.cluster.routing.allocation.group.allocator;

import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.cluster.routing.allocation.allocator.GroupBalancedShardsAllocator;
import org.elasticsearch.cluster.routing.allocation.allocator.IndexGroupSettings;
import org.elasticsearch.cluster.routing.allocation.allocator.ShardsAllocator;
import org.elasticsearch.common.settings.ClusterSettings;
import org.elasticsearch.common.settings.IndexScopedSettings;
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsFilter;
import org.elasticsearch.plugins.ActionPlugin;
import org.elasticsearch.plugins.ClusterPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestHandler;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * author weizijun
 * date：2019-06-12
 */
public class GroupAllocatorPlugin extends Plugin implements ClusterPlugin, ActionPlugin {

    public static final String GROUP_BALANCED_ALLOCATOR = "group_balanced";

    @Override
    public List<Setting<?>> getSettings() {
        return Arrays.asList(
            GroupBalancedShardsAllocator.INDEX_BALANCE_FACTOR_SETTING,
            GroupBalancedShardsAllocator.SHARD_BALANCE_FACTOR_SETTING,
            GroupBalancedShardsAllocator.GROUP_BALANCE_FACTOR_SETTING,
            GroupBalancedShardsAllocator.THRESHOLD_SETTING,
            IndexGroupSettings.INDEX_GROUP_FACTOR_SETTINGS,
            IndexGroupSettings.INDEX_GROUP_NAME_SETTINGS,
            IndexGroupSettings.INDEX_TEMPLATE_NAME_SETTINGS
        );
    }

    @Override
    public Map<String, Supplier<ShardsAllocator>> getShardsAllocators(Settings settings, ClusterSettings clusterSettings) {
        return Collections.singletonMap(GROUP_BALANCED_ALLOCATOR, () -> new GroupBalancedShardsAllocator(settings, clusterSettings));
    }

    @Override
    public List<RestHandler> getRestHandlers(Settings settings, RestController restController, ClusterSettings clusterSettings,
                                             IndexScopedSettings indexScopedSettings, SettingsFilter settingsFilter, IndexNameExpressionResolver indexNameExpressionResolver,
                                             Supplier<DiscoveryNodes> nodesInCluster) {
        return Arrays.asList(
            new RestGetGroupFactorAction(settings, restController),
            new RestGetNodeGroupFactorAction(settings, restController));
    }
}
