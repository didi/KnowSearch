package com.didichuxing.datachannel.arius.admin.common.constant;

public enum QueryDiagnosisTabNameEnum {
    /**慢查询列表*/
    SLOW_QUERY("slowQuery"),
    /**异常查询列表*/
    ERROR_QUERY("errorQuery");
    private String tabName;

    QueryDiagnosisTabNameEnum(String tabName) {
        this.tabName = tabName;
    }

    public String getTabName() {
        return tabName;
    }



}
