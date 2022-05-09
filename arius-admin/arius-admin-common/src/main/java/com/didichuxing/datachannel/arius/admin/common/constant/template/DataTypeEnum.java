package com.didichuxing.datachannel.arius.admin.common.constant.template;

/**
 * @author d06679
 * @date 2019/3/29
 */
public enum DataTypeEnum {
    /**
     * 日志数据
     */
    SYSTEM(0, "系统数据", "system"),

    LOG(1, "日志数据", "log"),

    OLAP(2, "用户上报数据", "olap"),

    BINLOG(3, "RDS数据", "binlog"),

    OFFLINE(4, "离线导入数据", "offline"),

    UNKNOWN(-1, "未知", "");

    DataTypeEnum(int code, String desc, String label) {
        this.code = code;
        this.desc = desc;
        this.label = label;
    }

    private int code;

    private String desc;

    private String label;

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public String getLabel() {
        return label;
    }

    public static DataTypeEnum valueOf(Integer code) {
        if (code == null) {
            return DataTypeEnum.UNKNOWN;
        }
        for (DataTypeEnum state : DataTypeEnum.values()) {
            if (state.getCode() == code) {
                return state;
            }
        }

        return DataTypeEnum.UNKNOWN;
    }

    public static boolean isExit(Integer code) {
        if (code == null) {
            return false;
        }
        for (DataTypeEnum state : DataTypeEnum.values()) {
            if (state.getCode() == code) {
                return true;
            }
        }

        return false;
    }

}
