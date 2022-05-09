package com.didichuxing.datachannel.arius.admin.common.constant.workorder;

/**
 * 工单类型
 * @author fengqiongfeng
 * @date 20/8/24
 */
public enum OrderTypeEnum {
    APPLY_INDEX         (00,     "索引申请", "applyIndexOrder"),
    DELETE_INDEX        (10,     "索引下线", "deleteIndexOrder"),
    APPLY_EXPAND_INDEX  (20,     "索引扩容", "modifyIndexOrder"),
    APPLY_REDUCE_INDEX  (21,     "索引缩容", "modifyIndexOrder"),
    APPLY_ROLE_INDEX    (30,     "索引权限申请", "deleteIndexOrder"),
    TRANSFER_INDEX      (40,     "索引转让", "transferIndexOrder"),

    APPLY_APP           (01,     "应用申请", "applyAppOrder"),

    APPLY_CLUSTER       (04,     "集群申请", "applyClusterOrder"),
    DELETE_CLUSTER      (13,     "集群下线", "deleteClusterOrder"),

    APPLY_EXPAND_CLUSTER(22,     "集群扩容", "modifyClusterOrder"),
    APPLY_REDUCE_CLUSTER(23,     "集群缩容", "modifyClusterOrder"),

    APPLY_QUERY_SENTENCE(05,     "查询语句申请", "applyQuerySentenceOrder"),
    APPLY_QUERY_LIMITING(23,     "查询语句限流", "limittingQueryOrder"),

    APPLY_CLUSTER_PLUG_INSTALL(30,     "集群插件安装", "logicClusterPlugOperation"),
    APPLY_CLUSTER_PLUG_UNINSTALL(40,     "集群插件卸载", "logicClusterPlugOperation"),

    ;

    private Integer code;

    private String message;

    private String orderName;

    OrderTypeEnum(Integer code, String message, String orderName) {
        this.code = code;
        this.message = message;
        this.orderName = orderName;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getOrderName() {
        return orderName;
    }

    public static String getOrderNameByType(Integer code) {
        for (OrderTypeEnum orderTypeEnum : values()) {
            if (orderTypeEnum.getCode().equals(code)) {
                return orderTypeEnum.getOrderName();
            }
        }
        return null;
    }
}
