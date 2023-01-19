package org.elasticsearch.region;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.elasticsearch.cluster.ClusterChangedEvent;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterStateListener;
import org.elasticsearch.cluster.ClusterStateUpdateTask;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.block.ClusterBlockLevel;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.routing.allocation.AllocationService;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.cluster.metadata.IndexMetaData.INDEX_SETTING_REGION_ATTRIBUTE;

public class RegionService implements ClusterStateListener {
    private static final Logger logger = LogManager.getLogger(RegionService.class);

    private final ClusterService clusterService;
    private final AllocationService allocationService;
    private final RegionClusterStateUpdateTask regionClusterStateUpdateTask;

    @Inject
    public RegionService(ClusterService clusterService, AllocationService allocationService) {
        this.clusterService = clusterService;
        this.allocationService = allocationService;
        this.clusterService.addListener(this);
        this.regionClusterStateUpdateTask = new RegionClusterStateUpdateTask();
        clusterService.getClusterSettings().addAffixUpdateConsumer(RegionSettings.CLUSTER_REGION_SEEDS, this::updateRegion, this::validate);
    }

    @Override
    public void clusterChanged(ClusterChangedEvent event) {
        if (event.state().nodes().isLocalNodeElectedMaster() == false) {
            return;
        }

        Settings previousPersistentSettings = event.previousState().metaData().persistentSettings();
        Settings currentPersistentSettings = event.state().metaData().persistentSettings();
        if (previousPersistentSettings == currentPersistentSettings && event.nodesDelta().hasChanges() == false) {
            return;
        }

        ClusterBlockException blockException = event.state().blocks().globalBlockedException(ClusterBlockLevel.METADATA_WRITE);
        if (blockException != null) {
            throw blockException;
        }

        if (RegionSettings.CLUSTER_REGION_SEEDS.getAsMap(event.state().metaData().transientSettings()) != null
            && RegionSettings.CLUSTER_REGION_SEEDS.getAsMap(event.state().metaData().transientSettings()).isEmpty() == false) {
            throw new UnsupportedOperationException("region must not in transient");
        }

        clusterService.submitStateUpdateTask("update-region", regionClusterStateUpdateTask);
    }

    private void updateRegion(String region, List<String> seeds) {
        logger.info(String.format("update region:[%s], seeds:[%s]", region, Strings.collectionToDelimitedString(seeds, ",")));
    }

    private void validate(String region, List<String> seeds) {
        if (Strings.isNullOrEmpty(region)) {
            throw new IllegalArgumentException("region must not be null");
        }
        for (DiscoveryNode node : clusterService.state().nodes()) {
            String regionName = node.getAttributes().get(INDEX_SETTING_REGION_ATTRIBUTE);
            if (Strings.hasText(regionName)
                && regionName.equals(region) == false
                && seeds != null
                && (seeds.contains(node.getHostName()) || seeds.contains(node.getName()) || seeds.contains(node.getHostAddress()))) {
                // a node can only be in one region
                throw new IllegalStateException("The node:[" + node.getName() + "] already in region:[" + regionName + "]");
            }
        }
    }

    class RegionClusterStateUpdateTask extends ClusterStateUpdateTask {
        @Override
        public ClusterState execute(ClusterState currentState) throws Exception {
            Settings persistentSettings = currentState.metaData().persistentSettings();
            Map<String, List<String>> persistentSeeds = RegionSettings.CLUSTER_REGION_SEEDS.getAsMap(persistentSettings);

            for (DiscoveryNode node : currentState.nodes()) {
                String regionName = node.getAttributes().get(INDEX_SETTING_REGION_ATTRIBUTE);
                if (Strings.isNullOrEmpty(regionName)) {
                    // region not exists
                    for (Map.Entry<String, List<String>> entry : persistentSeeds.entrySet()) {
                        List<String> seeds = entry.getValue();
                        if (seeds == null || seeds.isEmpty()) {
                            continue;
                        }
                        if (seeds.contains(node.getHostName())
                            || seeds.contains(node.getName())
                            || seeds.contains(node.getHostAddress())) {

                            Map<String, String> attributes = new HashMap<>(node.getAttributes());
                            attributes.put(INDEX_SETTING_REGION_ATTRIBUTE, entry.getKey());
                            node.setAttributes(attributes);
                            // a node can only be in one region
                            break;
                        }
                    }
                } else {
                    // region exists
                    List<String> seeds = persistentSeeds.get(regionName);
                    if (seeds == null
                        || seeds.isEmpty()
                        || (seeds.contains(node.getHostName()) == false
                            && seeds.contains(node.getName()) == false
                            && seeds.contains(node.getHostAddress()) == false)) {

                        Map<String, String> attributes = new HashMap<>(node.getAttributes());
                        attributes.remove(INDEX_SETTING_REGION_ATTRIBUTE);
                        node.setAttributes(attributes);
                    }
                }
            }

            return allocationService.reroute(ClusterState.builder(currentState).build(), "update-region");
        }

        @Override
        public void onFailure(String source, Exception e) {
            logger.error("update region failed", e);
        }
    }
}
