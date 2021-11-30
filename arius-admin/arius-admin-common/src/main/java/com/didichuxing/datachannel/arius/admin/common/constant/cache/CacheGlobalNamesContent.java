package com.didichuxing.datachannel.arius.admin.common.constant.cache;

/**
 * Created by linyunan on 2021-07-23
 */
public class CacheGlobalNamesContent {

    private CacheGlobalNamesContent(){}

    /**
     * 全局缓存名称
     */
    public static final String CACHE_GLOBAL_NAME         = "CacheGlobalManager";

    /**
     * 物理集群列表信息
     */
    public static final String CLUSTER_PHY_LIST_CACHE    = "getConsoleClusterPhyVOS";

    /**
     * 逻辑集群列表信息
     */
    public static final String CLUSTER_LOGIC_LIST_CACHE  = "getConsoleClusterVOS";

    /**
     * 物理模板列表信息
     */
    public static final String TEMPLATE_PHY_LIST_CACHE   = "getConsoleTemplatePhyVOS";

    /**
     * 逻辑模板列表信息
     */
    public static final String TEMPLATE_LOGIC_LIST_CACHE = "getConsoleTemplatesVOS";
}
