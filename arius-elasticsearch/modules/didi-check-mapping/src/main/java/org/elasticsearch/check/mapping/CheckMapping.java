package org.elasticsearch.check.mapping;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.common.settings.ClusterSettings;
import org.elasticsearch.common.settings.IndexScopedSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsFilter;
import org.elasticsearch.plugins.ActionPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestHandler;

public class CheckMapping extends Plugin implements ActionPlugin {

    /**
     * Actions added by this plugin.
     */
    @Override
    public List<ActionHandler<? extends ActionRequest, ? extends ActionResponse>> getActions() {
        return Collections.singletonList(
            new ActionHandler<>(CheckMappingAction.INSTANCE, TransportCheckMappingAction.class)
        );
    }

    /**
     * Rest handlers added by this plugin.
     *
     * @param settings settings
     * @param restController restController
     * @param clusterSettings clusterSettings
     * @param indexScopedSettings indexScopedSettings
     * @param settingsFilter settingsFilter
     * @param indexNameExpressionResolver indexNameExpressionResolver
     * @param nodesInCluster nodesInCluster
     */
    @Override
    public List<RestHandler> getRestHandlers(
        Settings settings,
        RestController restController,
        ClusterSettings clusterSettings,
        IndexScopedSettings indexScopedSettings,
        SettingsFilter settingsFilter,
        IndexNameExpressionResolver indexNameExpressionResolver,
        Supplier<DiscoveryNodes> nodesInCluster
    ) {
        return Collections.singletonList(new RestCheckMappingAction(settings, restController));
    }

}
