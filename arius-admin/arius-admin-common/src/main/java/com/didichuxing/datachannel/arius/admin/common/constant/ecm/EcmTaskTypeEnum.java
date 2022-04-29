package com.didichuxing.datachannel.arius.admin.common.constant.ecm;

public enum EcmTaskTypeEnum {
                             /**新增*/
                             NEW(1, "集群新增"),

                             EXPAND(2, "集群扩容"),

                             SHRINK(3, "集群缩容"),

                             RESTART(4, "集群重启"),

                             UPGRADE(5, "集群升级"),

                             PLUG_OPERATION(6, "集群插件操作"),

                             OFFLINE(7, "集群下线操作"),

                             CONFIG_ADD(8, "集群配置新增"),

                             CONFIG_EDIT(9, "集群配置编辑"),

                             CONFIG_DELETE(10, "集群配置删除"),

                             UNKNOWN(-1, "unknown");

    EcmTaskTypeEnum(int code, String desc) {
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

    public static EcmTaskTypeEnum valueOf(Integer code) {
        if (code == null) {
            return EcmTaskTypeEnum.UNKNOWN;
        }
        for (EcmTaskTypeEnum typeEnum : EcmTaskTypeEnum.values()) {
            if (typeEnum.getCode() == code) {
                return typeEnum;
            }
        }

        return EcmTaskTypeEnum.UNKNOWN;
    }

}
