/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.check.mapping;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.elasticsearch.Version;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.support.master.TransportMasterNodeAction;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.block.ClusterBlockLevel;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.compress.CompressedXContent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.settings.IndexScopedSettings;
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AnalyzerScope;
import org.elasticsearch.index.analysis.IndexAnalyzers;
import org.elasticsearch.index.analysis.NamedAnalyzer;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.similarity.SimilarityService;
import org.elasticsearch.indices.IndicesModule;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.plugins.MapperPlugin;
import org.elasticsearch.plugins.PluginsService;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

/**
 * Put mapping action.
 */
public class TransportCheckMappingAction extends TransportMasterNodeAction<CheckMappingAction.Request, AcknowledgedResponse> {

    private final IndicesService indicesService;

    private final PluginsService pluginsService;

    @Inject
    public TransportCheckMappingAction(
        IndicesService indicesService, PluginsService pluginsService,
        Settings settings, TransportService transportService, ClusterService clusterService,
        ThreadPool threadPool, ActionFilters actionFilters, IndexNameExpressionResolver indexNameExpressionResolver
    ) {
        super(
            CheckMappingAction.NAME,
            transportService,
            clusterService,
            threadPool,
            actionFilters,
            CheckMappingAction.Request::new,
            indexNameExpressionResolver
        );
        this.indicesService = indicesService;
        this.pluginsService = pluginsService;
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
    protected ClusterBlockException checkBlock(CheckMappingAction.Request request, ClusterState state) {
        return state.blocks().globalBlockedException(ClusterBlockLevel.METADATA_READ);

    }

    @Override
    protected void masterOperation(
        final CheckMappingAction.Request request,
        final ClusterState state,
        final ActionListener<AcknowledgedResponse> listener
    ) throws IOException {
        MapperService mapperService = getMapperService(request, state);
        mapperService.parse(request.getType(), new CompressedXContent(request.getSource()), true);
        mapperService.close();
        listener.onResponse(new AcknowledgedResponse(true));
    }

    private MapperService getMapperService(CheckMappingAction.Request request, ClusterState state) throws IOException {

        if (request.getIndex() != null) {
            IndexMetaData indexMetaData = state.metaData().index(request.getIndex());
            if (indexMetaData == null) {
                throw new IndexNotFoundException(request.getIndex());
            }
            return indicesService.createIndexMapperService(indexMetaData);
        }


        Index index = new Index("check_mapping_index", "check_mapping_index_UUID");
        IndexSettings indexSettings = newIndexSettings(index, Settings.EMPTY, Settings.EMPTY);
        NamedAnalyzer namedAnalyzer = new NamedAnalyzer("default", AnalyzerScope.INDEX, new StandardAnalyzer());
        IndexAnalyzers indexAnalyzers = new IndexAnalyzers(
            Collections.singletonMap("default", namedAnalyzer),
            Collections.singletonMap("default", namedAnalyzer),
            Collections.singletonMap("default", namedAnalyzer)
        );
        SimilarityService similarityService = new SimilarityService(indexSettings, null, Collections.emptyMap());

        // 实例化MapperService服务
        return new MapperService(
            indexSettings,
            indexAnalyzers,
            NamedXContentRegistry.EMPTY, // new NamedXContentRegistry(ClusterModule.getNamedXWriteables())
            similarityService,
            new IndicesModule(pluginsService.filterPlugins(MapperPlugin.class)).getMapperRegistry(),
            () -> null,
            () -> true
        );
    }

    private IndexSettings newIndexSettings(Index index, Settings indexSetting, Settings nodeSettings) {
        Settings build = Settings.builder()
            .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
            .put(IndexMetaData.SETTING_NUMBER_OF_REPLICAS, 1)
            .put(IndexMetaData.SETTING_NUMBER_OF_SHARDS, 1)
            .put(indexSetting)
            .build();
        IndexMetaData metaData = IndexMetaData.builder(index.getName()).settings(build).build();
        Set<Setting<?>> settingSet = new HashSet<>(IndexScopedSettings.BUILT_IN_INDEX_SETTINGS);
        return new IndexSettings(metaData, nodeSettings, new IndexScopedSettings(Settings.EMPTY, settingSet));
    }

}
