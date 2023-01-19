package org.elasticsearch.plugin.spatial;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.io.stream.NamedWriteableRegistry;
import org.elasticsearch.common.settings.*;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.NodeEnvironment;
import org.elasticsearch.ingest.Processor;
import org.elasticsearch.plugin.spatial.action.RestSpatialDeleteAction;
import org.elasticsearch.plugin.spatial.action.RestSpatialMultiSearchAction;
import org.elasticsearch.plugin.spatial.action.RestSpatialSearchAction;
import org.elasticsearch.plugin.spatial.index.SpatialProcessor;
import org.elasticsearch.plugin.spatial.router.Router;
import org.elasticsearch.plugins.ActionPlugin;
import org.elasticsearch.plugins.IngestPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestHandler;
import org.elasticsearch.script.ScriptService;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.watcher.ResourceWatcherService;

import java.util.*;
import java.util.function.Supplier;

import static org.elasticsearch.plugin.spatial.config.SpatialConfig.*;


public class SpatialIndexPlugin extends Plugin implements ActionPlugin, IngestPlugin {
    private Router router;
    private Object lock = new Object();

    @Override
    public List<Setting<?>> getSettings() {
        return Arrays.asList(
            INDEX_SPATIAL_S2DATA_SETTINGS,
            INDEX_SPATIAL_FIELD_GEO_SETTINGS,
            INDEX_SPATIAL_FIELD_CITYID_SETTINGS,
            INDEX_SPATIAL_MAXCELL_SETTINGS,
            INDEX_SPATIAL_MAXLEVEL_SETTINGS
        );
    }


    @Override
    public Collection<Object> createComponents(Client client, ClusterService clusterService, ThreadPool threadPool,
                                               ResourceWatcherService resourceWatcherService, ScriptService scriptService,
                                               NamedXContentRegistry xContentRegistry, Environment environment,
                                               NodeEnvironment nodeEnvironment, NamedWriteableRegistry namedWriteableRegistry) {
        initRouter((NodeClient) client);
        return Collections.emptyList();
    }


    @Override
    public List<RestHandler> getRestHandlers(Settings settings,
                                             RestController restController,
                                             ClusterSettings clusterSettings,
                                             IndexScopedSettings indexScopedSettings,
                                             SettingsFilter settingsFilter,
                                             IndexNameExpressionResolver indexNameExpressionResolver,
                                             Supplier<DiscoveryNodes> nodesInCluster) {

        initRouter(null);

        return Arrays.asList(
            new RestSpatialSearchAction(settings, restController, router),
            new RestSpatialMultiSearchAction(settings, restController, router),
            new RestSpatialDeleteAction(settings, restController, router)
        );
    }

    @Override
    public Map<String, Processor.Factory> getProcessors(Processor.Parameters parameters) {
        initRouter( null);

        Map<String, Processor.Factory> processors = new HashMap<>();
        processors.put(SpatialProcessor.TYPE, new SpatialProcessor.Factory(router));
        return Collections.unmodifiableMap(processors);
    }


    private void initRouter(NodeClient client) {
        synchronized (lock) {
            if (router == null) {
                router = new Router();
            }

            if (client != null) {
                router.setNodeClient(client);
            }
        }
    }
}


