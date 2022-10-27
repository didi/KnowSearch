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
    public static final String INDEX_ROUTING_ALLOCATION_INCLUDE_RACK = "index.routing.allocation.include.rack";
    public static final String INDEX_TRANSLOG_SYNC_INTERVAL    = "index.translog.sync_interval";
    public static final String DEFAULT_DYNAMIC_TEMPLATES_KEY   = "dynamic_templates";
    public static final String INDEX_CREATION_DATE             = "index.creation_date";
    public static final String INDEX_VERIFIED_BEFORE_CLOSE     = "index.verified_before_close";
    public static final String INDEX_VERSION_CREATED           = "index.version.created";
    public static final String INDEX_VERSION                   = "index.version";
    public static final String INDEX_UUID                      = "index.uuid";
    public static final String INDEX_PROVIDED_NAME             = "index.provided_name";
    public static final String INDEX_ROUTING_ALLOCATION_PREFIX = "index.routing.allocation";
    public static final String INDEX_PRIORITY                  = "index.priority";



    public static final String ASYNC                           = "async";
    public static final String REQUEST                         = "request";

    public static final Integer HIGH_PRIORITY                  = 10;
    public static final Integer MIDDLE_PRIORITY                = 5;
    public static final Integer LOW_PRIORITY                   = 0;
}