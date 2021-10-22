package com.didichuxing.datachannel.arius.admin.task;

/**
 * 定时任务并发度配置
 * @author wangshu
 * @date 2020/06/05
 */
public class TaskConcurrentConstants {
    public static final int ADJUST_PIPELINE_RATE_LIMIT_TASK_CONCURRENT = 100;
    public static final int CAPACITY_AREA_CHECK_TASK_CONCURRENT = 10;
    public static final int CAPACITY_AREA_PLAN_TASK_CONCURRENT = 5;
    public static final int CAPACITY_AREA_STATICS_TASK_CONCURRENT = 1;
    public static final int CLOSE_CLUSTER_TASK_CONCURRENT = 20;
    public static final int CLUSTER_CURRENT_TEST_TASK_CONCURRENT = 20;
    public static final int COLD_DATA_MOVE_TASK_CONCURRENT = 10;
    public static final int COLLECT_CLUSTER_NODE_SETTING_FROM_ES_TASK_CONCURRENT = 20;
    public static final int COPY_INDEX_MAPPING2_TEMPLATE_TASK_CONCURRENT = 20;
    public static final int DELETE_EXPIRE_INDEX_TASK_CONCURRENT = 20;
    public static final int PRE_CREATE_INDEX_TASK_CONCURRENT = 20;
    public static final int SECURITY_META_CHECK_TASK_CONCURRENT = 20;
    public static final int SYNC_TEMPLATE_META_DATE_TASK_CONCURRENT = 20;
    public static final int TEMPLATE_QUOTA_CTL_TASK_CONCURRENT = 20;

    public static final int SLEEP_SECONDS_PER_EXECUTE = 3;
    public static final int SLEEP_SECONDS_PER_BATCH = 60;
}