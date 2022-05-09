package com.didichuxing.datachannel.arius.admin.common.constant.operaterecord;

public enum TemplateOperateRecordEnum {

    /**
     * 模板操作类型
     */
    NEW("new", 0),
    ROLLOVER("rollover", 1),
    MAPPING("mapping", 2),
    SETTING("setting", 3),
    WRITE("write", 4),
    READ("read", 5),
    CONFIG("config", 6),
    TRANSFER("transfer", 7),
    UPGRADE("upgrade", 8);

    public String getDesc() {
        return desc;
    }

    public int getCode() {
        return code;
    }

    TemplateOperateRecordEnum(String desc, int code) {
        this.desc = desc;
        this.code = code;
    }

    private int code;

    private String desc;
}
