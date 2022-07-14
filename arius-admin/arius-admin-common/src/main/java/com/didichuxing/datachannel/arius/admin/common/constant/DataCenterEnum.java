package com.didichuxing.datachannel.arius.admin.common.constant;

public enum DataCenterEnum {
                            US01("us01", "美东机房"), CN("cn", "国内"), UNKNOWN("unknown", "未知");

    DataCenterEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private String code;

    private String desc;

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static DataCenterEnum valueOfCode(String code) {
        if (code == null) {
            return DataCenterEnum.UNKNOWN;
        }
        for (DataCenterEnum state : DataCenterEnum.values()) {
            if (state.getCode().equals(code)) {
                return state;
            }
        }

        return DataCenterEnum.UNKNOWN;
    }

    /**
     * 校验数据中心是否合法
     * @param dataCenter 数据中心str
     * @return 合法 true  不合法 false
     */
    public static boolean validate(String dataCenter) {
        return DataCenterEnum.UNKNOWN != DataCenterEnum.valueOfCode(dataCenter);
    }

    public boolean sameDC(DataCenterEnum dc) {
        return this.code.equals(dc.getCode());
    }
}
