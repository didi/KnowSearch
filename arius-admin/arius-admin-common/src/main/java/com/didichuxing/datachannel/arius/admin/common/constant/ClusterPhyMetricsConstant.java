package com.didichuxing.datachannel.arius.admin.common.constant;

/**
 * Created by linyunan on 2021-08-10
 */
public class ClusterPhyMetricsConstant {

    private ClusterPhyMetricsConstant() {
    }

    /*****************************************构建dsl相关常量***************************************/

    public static final String VALUE         = "value";

    public static final String VALUES        = "values";

    public static final String DOUBLE        = "double";

    public static final String LONG          = "long";

    public static final String INT           = "int";

    public static final String TIME_STAMP    = "timeStamp";

    public static final String PERCENTILES   = "percentiles";

    public static final String AVG           = "avg";

    public static final String HIST          = "hist";

    public static final String KEY           = "key";
    public static final String SUM           = "sum";

    public static final String STATIS        = "statis.";

    public static final String METRICS       = "metrics.";

    public static final String FIELD         = "field";
    public static final String MISSING       = "missing";
    public static final String MISSING_VALUE = "-1.0";

    public static final String TOTAL         = "total";
    public static final String INDICES       = "indices";
    public static final String SHARDS        = "shards";
    public static final String TIMESTAMP     = "timestamp";
    /*********************************************分位值*******************************************/

    public static final String ST90          = "90.0";
    public static final String ST70          = "70.0";
    public static final String ST50          = "50.0";
    public static final String ST99          = "99.0";
    public static final String ST95          = "95.0";
    public static final String ST75          = "75.0";
    public static final String ST55          = "55.0";

    /*********************************************pendingTask*******************************************/

    public static final String TASKS         = "tasks";
    public static final String NODES         = "nodes";

    public static final String TIME_IN_QUEUE = "time_in_queue";

    public static final String SOURCE        = "source";

    public static final String PRIORITY      = "priority";

    public static final String INSERT_PRDER  = "insert_order";

//    10 * 10000 * 10000L;
    public static final Long   ONE_BILLION   = 10 * 10000 * 10000L;

}