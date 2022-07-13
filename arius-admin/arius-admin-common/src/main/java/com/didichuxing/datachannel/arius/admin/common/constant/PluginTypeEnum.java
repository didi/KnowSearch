package com.didichuxing.datachannel.arius.admin.common.constant;

/**
 * 插件类型集合
 * @author didi
 */
public enum PluginTypeEnum {
                            /**
                             * 系统默认安装，在集群部署的时候已经在插件包内
                             */
                            DEFAULT_PLUGIN(0, "系统默认"),
                            /**
                             * 用户自定义上传插件中的ES能力插件，被ES引擎所使用
                             */
                            ES_PLUGIN(1, "ES能力"),
                            /**
                             * 用户自定插件中的平台能力插件，用于平台对集群的其他能力，如心跳插件
                             */
                            ADMIN_PLUGIN(2, "平台能力"),
                            /**
                             * 未知类型
                             */
                            UNKNOWN(-1, "未知类型");

    int    code;
    String desc;

    PluginTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static PluginTypeEnum valueOf(Integer code) {
        if (null == code) {
            return UNKNOWN;
        }

        for (PluginTypeEnum param : PluginTypeEnum.values()) {
            if (param.getCode() == code) {
                return param;
            }
        }

        return UNKNOWN;
    }
}
