package com.didichuxing.datachannel.arius.admin.common.constant.metrics;

public enum ConfigTypeEnum {
    /**
     * code表示编号
     * config_type表示是哪个功能的个性化配置
     */
    /**
     * dashboard和指标看板
     */
    DASHBOARD_AND_METRICS_BOARD("dashboard_and_metrics_board",1),
    /**
     * 检索查询大菜单下的查询模板
     */
    RETRIEVE_TEMPLATE("retrieve_template",2);

    private String configType;
    private int code;
    ConfigTypeEnum(String configType,int code){
        this.configType=configType;
        this.code=code;
    }

    public String getConfigType() {
        return configType;
    }

    public int getCode() {
        return code;
    }
}
