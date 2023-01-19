package org.elasticsearch.dcdr.action;

import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.ExceptionsHelper;
import org.elasticsearch.ResourceAlreadyExistsException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.support.master.TransportMasterNodeAction;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.AckedClusterStateUpdateTask;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.block.ClusterBlockLevel;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.Priority;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.dcdr.DCDRSettings;
import org.elasticsearch.dcdr.translog.primary.DCDRIndexMetadata;
import org.elasticsearch.dcdr.translog.primary.DCDRMetadata;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.RemoteTransportException;
import org.elasticsearch.transport.TransportService;

/**
 * author weizijun
 * date：2019-08-12
 */
public class TransportCreateReplicationAction extends TransportMasterNodeAction<CreateReplicationAction.Request, AcknowledgedResponse> {

    private final Client client;
    private final IndicesService indicesService;
    private final static long CREATE_INDEX_REQUEST_TIMEOUT = 5000;

    @Inject
    public TransportCreateReplicationAction(
        Client client, IndicesService indicesService, Settings settings, TransportService transportService, ClusterService clusterService,
        ThreadPool threadPool, ActionFilters actionFilters, IndexNameExpressionResolver indexNameExpressionResolver
    ) {
        super(
            CreateReplicationAction.NAME,
            transportService,
            clusterService,
            threadPool,
            actionFilters,
            CreateReplicationAction.Request::new,
            indexNameExpressionResolver
        );
        this.client = client;
        this.indicesService = indicesService;
    }

    @Override
    protected String executor() {
        return ThreadPool.Names.SAME;
    }

    @Override
    protected AcknowledgedResponse read(StreamInput in) throws IOException {
        return new AcknowledgedResponse(in.readBoolean());
    }

    @Override
    protected void masterOperation(
        CreateReplicationAction.Request request,
        ClusterState state,
        ActionListener<AcknowledgedResponse> listener
    ) throws Exception {
        Client replicaClient = client.getRemoteClusterClient(request.getReplicaCluster());
        if (replicaClient == null) {
            listener.onFailure(new ElasticsearchException("replica cluster [" + request.getReplicaCluster() + "] not found"));
            return;
        }

        IndexMetaData primaryIndexMetaData = state.metaData().index(request.getPrimaryIndex());
        if (primaryIndexMetaData == null) {
            listener.onFailure(new IndexNotFoundException("primary index [" + request.getPrimaryIndex() + "] not found"));
            return;
        }

        CreateIndexRequest createIndexRequest = new CreateIndexRequest(request.getReplicaIndex());

        Settings.Builder settingsBuilder = Settings.builder();
        for (String setting : primaryIndexMetaData.getSettings().keySet()) {
            if (setting.equals("index.uuid")
                || setting.equals("index.version.created")
                || setting.equals("index.version.upgraded")
                || setting.startsWith("index.routing.allocation")
                || setting.startsWith("index.group")
                || setting.equals("index.creation_date")
                // || setting.equals("index.number_of_replicas")
                || setting.equals("index.template")
                || setting.equals("index.provided_name")) {
                continue;
            }

            settingsBuilder.put(setting, primaryIndexMetaData.getSettings().get(setting));
        }

        settingsBuilder.put(DCDRSettings.DCDR_REPLICA_INDEX_SETTING.getKey(), true);
        createIndexRequest.settings(settingsBuilder);

        primaryIndexMetaData.getMappings()
            .forEach(
                (cursor) -> {
                    createIndexRequest.mapping(cursor.key, cursor.value.sourceAsMap());
                }
            );

        replicaClient.admin().indices().create(createIndexRequest, new ActionListener<CreateIndexResponse>() {
            @Override
            public void onResponse(CreateIndexResponse createIndexResponse) {
                createReplication(request, listener);
            }

            @Override
            public void onFailure(Exception e) {
                // 索引已存在
                Throwable throwable = ExceptionsHelper.unwrapCause(e);
                if (throwable instanceof ResourceAlreadyExistsException) {
                    createReplication(request, listener);
                    return;
                }

                listener.onFailure(e);
            }
        });
    }

    private void createReplication(CreateReplicationAction.Request request, ActionListener<AcknowledgedResponse> listener) {
        clusterService.submitStateUpdateTask(
            "create-replication-" + request.getPrimaryIndex(),
            new AckedClusterStateUpdateTask<AcknowledgedResponse>(Priority.URGENT, request, listener) {
                @Override
                public String taskType() {
                    return "create-replication";
                }

                @Override
                public ClusterState execute(ClusterState currentState) throws Exception {
                    ClusterState.Builder newState = ClusterState.builder(currentState);
                    DCDRMetadata dcdrMetadata = currentState.metaData().custom(DCDRMetadata.TYPE);
                    if (dcdrMetadata == null) {
                        dcdrMetadata = DCDRMetadata.EMPTY;
                    }

                    SortedMap<String, DCDRIndexMetadata> replicaIndices = new TreeMap<>(dcdrMetadata.getReplicaIndices());

                    DCDRIndexMetadata dcdrIndexMetadata = new DCDRIndexMetadata(
                        request.getPrimaryIndex(),
                        request.getReplicaIndex(),
                        request.getReplicaCluster(),
                        Boolean.TRUE
                    );
                    replicaIndices.put(
                        DCDRIndexMetadata.name(
                            request.getPrimaryIndex(),
                            request.getReplicaIndex(),
                            request.getReplicaCluster()
                        ),
                        dcdrIndexMetadata
                    );

                    DCDRMetadata newMetadata = new DCDRMetadata(replicaIndices, dcdrMetadata.getReplicaTemplates());
                    newState.metaData(
                        MetaData.builder(currentState.getMetaData())
                            .putCustom(DCDRMetadata.TYPE, newMetadata)
                            .build()
                    );

                    return newState.build();
                }

                @Override
                protected AcknowledgedResponse newResponse(boolean acknowledged) {
                    return new AcknowledgedResponse(acknowledged);
                }
            }
        );
    }

    @Override
    protected ClusterBlockException checkBlock(CreateReplicationAction.Request request, ClusterState state) {
        return state.blocks().globalBlockedException(ClusterBlockLevel.METADATA_WRITE);
    }
}
