package com.didichuxing.datachannel.arius.admin.common.constant;

/**
 * @author: didi
 * @description: 集群角色
 * @date: Create on 2020/10/12 下午14:58
 */
public class CloudClusterCreateParamConstant {

    private CloudClusterCreateParamConstant(){}

    public static final  String VOLUME_TYPE_RDB         = "rdb";

    public static final  String VOLUME_TYPE_HOSTPATH    = "hostpath";

    public static final  String ODIN_CATEGORY_SERVICE   = "service";

    public static final  String ODIN_CATEGORY_GROUP     = "group";

    public static final  Integer ODIN_CATEGORY_LEVEL_1  = 1;

    public static final  Integer ODIN_CATEGORY_LEVEL_2  = 2;

    public static final  String DISK_SUFFIX             = "g";

    public static final  String CLOUD_CLUSTER_ENV_NAME_TICK_TIME   = "TICK_TIME";

    public static final  String CLOUD_CLUSTER_ENV_NAME_TICK_TIME_VALUE   = "2000";

    public static final  String CLOUD_CLUSTER_ENV_NAME_CLUSTERNAME = "CLUSTERNAME";

    public static final  String CLOUD_CLUSTER_ENV_NAME_NODEROLE    = "NODEROLE";

    public static final  String CLOUD_CLUSTER_ENV_NAME_MASTERNODES = "MASTERNODES";

    public static final  String CLOUD_CLUSTER_ENV_NAME_T_C         = "T_C";

    public static final  String CLOUD_CLUSTER_ENV_NAME_T_C_VALUE    = "21";

    public static final  String CLOUD_CLUSTER_ENV_NAME_ECM_URL     = "ECM_URL";

    public static final  String CLOUD_CLUSTER_ENV_NAME_JOB_ID     = "JOB_ID";

    public static final  String CLOUD_CLUSTER_SCENE               = "online";

    public static final  String CLOUD_CLUSTER_VOLUME_PATH         = "/home/data1";

    public static final  Integer CLOUD_CLUSTER_DEPLOY_TIMEOUT_START         = 1200;

    public static final  Integer CLOUD_CLUSTER_DEPLOY_GROUP_ONE_COUNT       = 20;

    public static final  Integer CLOUD_CLUSTER_DEPLOY_GROUP_TWO_COUNT       = 50;

    public static final  Integer CLOUD_CLUSTER_DEPLOY_GROUP_THREE_COUNT     = 100;
}
