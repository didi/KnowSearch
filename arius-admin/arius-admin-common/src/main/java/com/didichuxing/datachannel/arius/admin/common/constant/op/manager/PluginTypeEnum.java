package com.didichuxing.datachannel.arius.admin.common.constant.op.manager;

import lombok.Getter;

/**
 * 插件枚举类型
 *
 * @author shizeying
 * @date 2022/10/20
 * @since 0.3.2
 */
@Getter
public enum PluginTypeEnum {
    GATEWAY_PLATFORM_PLUGIN("gateway 平台插件", 1),
    ES_PLATFORM_PLUGIN("gateway 平台插件", 2),
    ES_ENGINE_PLUGIN("elasticsearch 引擎插件", 3),
    
    UNKNOWN("unknown", -1);
    private final String memo;
    
    private final Integer type;
    
    PluginTypeEnum(String memo, Integer type) {
        this.memo = memo;
        this.type = type;
    }
    
    
    /**
     * > 如果类型为 null，则返回 UNKNOWN。否则，遍历枚举的所有值并返回与类型匹配的值
     *
     * @param type 插件的类型。
     * @return PluginTypeEnum
     */
    public static PluginTypeEnum valueOfType(Integer type) {
        if (type == null) {
            return PluginTypeEnum.UNKNOWN;
        }
        for (PluginTypeEnum typeEnum : PluginTypeEnum.values()) {
            if (type.equals(typeEnum.getType())) {
                return typeEnum;
            }
        }
        
        return PluginTypeEnum.UNKNOWN;
    }
}