package com.didichuxing.datachannel.arius.admin.common.constant;

/**
 *
 * 工程常量
 *
 * 如果配置的量过多,需要拆解
 *
 */
public class ESSettingConstant {

    private ESSettingConstant() {
    }


    public static final String       DEFAULT_INDEX_MAPPING_TYPE              = "_doc";
    public static final String INDEX_NUMBER_OF_SHARDS = "index.number_of_shards";
    public static final String INDEX_REFRESH_INTERVAL = "index.refresh_interval";
    public static final String INDEX_TRANSLOG_DURABILITY = "index.translog.durability";
    public static final String INDEX_ROUTING_ALLOCATION_INCLUDE_NAME = "index.routing.allocation.include._name";
    public static final String INDEX_TRANSLOG_SYNC_INTERVAL = "index.translog.sync_interval";
    public static final String       DEFAULT_DYNAMIC_TEMPLATES_KEY           = "dynamic_templates";


}