package com.didichuxing.datachannel.arius.admin.common.constant.task;

/**
 * 数据迁移常量
 *
 * @author didi
 * @date 2022/10/31
 */
public class FastIndexConstant {
    public static final Integer DATA_TYPE_TOTAL          = 0;

    public static final Integer DATA_TYPE_TEMPLATE       = 1;

    public static final Integer DATA_TYPE_INDEX          = 2;

    public static final Integer RELATION_TYPE_ALL_TO_ONE = 1;

    public static final Integer RELATION_TYPE_ONE_TO_ONE = 2;

    public static final Integer WRITE_TYPE_INDEX_WITH_ID = 1;

    public static final Integer WRITE_TYPE_INDEX         = 2;

    public static final Integer WRITE_TYPE_CREATE        = 3;

    public static final Integer CLUSTER_TYPE_ES          = 1;

    public static final Integer TRANSFER_STATUS_NO_NEED  = -1;
    public static final Integer TRANSFER_STATUS_WAITING  = 0;
    public static final Integer TRANSFER_STATUS_SUCCESS = 1;
    public static final Integer TRANSFER_STATUS_ROLLBACK = 2;
}
