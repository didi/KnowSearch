package com.didichuxing.datachannel.arius.admin.remote.zeus.bean.constant;

/**
 * 宙斯物理集群操作动作枚举类型
 * new  创建
 * expand  扩容  shrink 缩容
 * restart  重启
 * @author didi
 * @date 2020/9/18
 */
public enum ZeusClusterActionEnum {
                                   NEW("new"),

                                   EXPAND("expand"),

                                   SHRINK("shrink"),

                                   UPDATE("deploy"),

                                   RESTART("restart");

    private final String value;

    ZeusClusterActionEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ZeusClusterActionEnum valueFrom(String value) {
        if (value == null) {
            return null;
        }
        for (ZeusClusterActionEnum state : ZeusClusterActionEnum.values()) {
            if (state.getValue().equals(value)) {
                return state;
            }
        }
        return null;
    }
}
