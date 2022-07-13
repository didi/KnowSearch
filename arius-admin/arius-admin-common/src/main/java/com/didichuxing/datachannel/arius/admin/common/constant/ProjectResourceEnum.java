package com.didichuxing.datachannel.arius.admin.common.constant;

/**
 * 项目资源枚举
 *
 * @author shizeying
 * @date 2022/05/26
 */
public enum ProjectResourceEnum {
                                 /**
                                  * 项目绑定的逻辑集群
                                  */
                                 PROJECT_CLUSTER_LOGIC(0, "逻辑集群"),
                                 /**
                                  * 项目绑定的es user
                                  */
                                 PROJECT_ES_USER(1, "es user"),
                                 /**
                                  * 项目绑定的索引模板
                                  */
                                 PROJECT_INDEX_TEMPLATE(2, "索引模板");

    int    code;
    String desc;

    ProjectResourceEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

}