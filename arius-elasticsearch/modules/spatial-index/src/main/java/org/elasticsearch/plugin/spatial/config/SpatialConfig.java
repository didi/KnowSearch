package org.elasticsearch.plugin.spatial.config;

import org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequestBuilder;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsResponse;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.settings.Setting;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class SpatialConfig {
    private static final String INDEX_SPATIAL_S2DATA = "index.spatial.s2data";
    public static final Setting<String> INDEX_SPATIAL_S2DATA_SETTINGS = Setting.simpleString(INDEX_SPATIAL_S2DATA, Setting.Property.Dynamic, Setting.Property.IndexScope);

    private static final String INDEX_SPATIAL_FIELD_GEO = "index.spatial.field.geo";
    public static final Setting<String> INDEX_SPATIAL_FIELD_GEO_SETTINGS = Setting.simpleString(INDEX_SPATIAL_FIELD_GEO, Setting.Property.Dynamic, Setting.Property.IndexScope);

    private static final String INDEX_SPATIAL_FIELD_CITYID = "index.spatial.field.cityid";
    public static final Setting<String> INDEX_SPATIAL_FIELD_CITYID_SETTINGS = Setting.simpleString(INDEX_SPATIAL_FIELD_CITYID, Setting.Property.Dynamic, Setting.Property.IndexScope);

    // 用于拟合的最大cell数
    private static final String INDEX_SPATIAL_MAXCELL = "index.spatial.maxCell";
    public static final Setting<Integer> INDEX_SPATIAL_MAXCELL_SETTINGS = Setting.intSetting(INDEX_SPATIAL_MAXCELL, 10, 0, Setting.Property.Dynamic, Setting.Property.IndexScope);

    // 希尔伯特编码最大阶数
    private static final String INDEX_SPATIAL_MAXLEVEL = "index.spatial.maxLevel";
    public static final Setting<Integer> INDEX_SPATIAL_MAXLEVEL_SETTINGS = Setting.intSetting(INDEX_SPATIAL_MAXLEVEL, 12, 0, Setting.Property.Dynamic, Setting.Property.IndexScope);

    private NodeClient client;
    private Thread thread;

    /** 支持单机房多版本的存储 */
    private Map<String/*indexName*/, IndexSpatialConfig> indexConfigMap = new ConcurrentHashMap<>();

    public SpatialConfig(NodeClient client) {
        this.client = client;

        // 异步更新setting线程
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(10 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    for (String indexName : indexConfigMap.keySet()) {
                        try {
                            indexConfigMap.put(indexName, getConfigFromSetting(indexName));
                        } catch (Throwable t) {
                            indexConfigMap.remove(indexName);
                        }
                    }
                }
            }
        });

        thread.setName("spatial_config_refresh");
        thread.setDaemon(true);
        thread.start();
    }

    public IndexSpatialConfig getIndexConfig(String indexName ) {
        IndexSpatialConfig indexSpatialConfig = indexConfigMap.get(indexName);
        if (indexSpatialConfig != null) {
            return indexSpatialConfig;
        }

        indexSpatialConfig = getConfigFromSetting(indexName);
        indexConfigMap.put(indexName, indexSpatialConfig);
        return indexSpatialConfig;
    }

    private static final String DEFAULT_MAX_CELL = "10";
    private static final String DEFAULT_MAX_LEVEL = "12";
    private IndexSpatialConfig getConfigFromSetting(String indexName) {
        // 获得索引配置
        GetSettingsRequestBuilder requestBuilder = client.admin().indices().prepareGetSettings(indexName);
        requestBuilder.setNames(
            INDEX_SPATIAL_S2DATA,
            INDEX_SPATIAL_FIELD_GEO,
            INDEX_SPATIAL_FIELD_CITYID,
            INDEX_SPATIAL_MAXCELL,
            INDEX_SPATIAL_MAXLEVEL,
            IndexMetaData.SETTING_NUMBER_OF_SHARDS
        );
        GetSettingsResponse response = requestBuilder.execute().actionGet(1000);

        // 加载索引数据,并构建, TODO 增加动态更新 TODO 对indexConfigStr做判断
        String s2data = response.getSetting(indexName, INDEX_SPATIAL_S2DATA);
        if (s2data == null) {
            throw new RuntimeException("index config(" + INDEX_SPATIAL_S2DATA + ") not set");
        }

        String geoName = response.getSetting(indexName, INDEX_SPATIAL_FIELD_GEO);
        if (geoName == null) {
            throw new RuntimeException("index config(" + INDEX_SPATIAL_FIELD_GEO + ") not set");
        }

        String cityIdName = response.getSetting(indexName, INDEX_SPATIAL_FIELD_CITYID);

        String maxCell = response.getSetting(indexName, INDEX_SPATIAL_MAXCELL);
        if (maxCell == null) {
            maxCell = DEFAULT_MAX_CELL;
        }

        String maxLevel = response.getSetting(indexName, INDEX_SPATIAL_MAXLEVEL);
        if (maxLevel == null) {
            maxLevel = DEFAULT_MAX_LEVEL;
        }

        int shardNum = Integer.valueOf(response.getSetting(indexName, IndexMetaData.SETTING_NUMBER_OF_SHARDS));

        return new IndexSpatialConfig(indexName, s2data, geoName, cityIdName, Integer.valueOf(maxCell), Integer.valueOf(maxLevel), shardNum);
    }

}
