package com.didichuxing.datachannel.arius.admin.client.constant.esconfig;

/**
 * @author lyn
 * @date 2021-01-21
 */
public enum EsConfigActionEnum {
                                /**新增*/
                                ADD(1, "集群配置新增"),

                                EDIT(2, "集群配置编辑"),

                                DELETE(3, "集群配置删除"),

                                UNKNOWN(-1, "未知");

    EsConfigActionEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private int    code;

    private String desc;

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static EsConfigActionEnum valueOf(Integer code) {
        if (code == null) {
            return EsConfigActionEnum.UNKNOWN;
        }
        for (EsConfigActionEnum typeEnum : EsConfigActionEnum.values()) {
            if (typeEnum.getCode() == code) {
                return typeEnum;
            }
        }

        return EsConfigActionEnum.UNKNOWN;
    }
}
