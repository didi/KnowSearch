package com.didichuxing.datachannel.arius.admin.common.constant.ecm;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * 弹性云集群操作接口动作枚举类型
 *
 *
 * @author didi
 * @date 2020/12/02
 */
public enum EcmCloudOpreateActionEnum {
                                       /**创建*/
                                       CREATE("create", "创建"),

                                       UPDATE("update", "升级或重启"),

                                       SCALE("scale", "扩缩容"),

                                       UNKNOWN("unknown", "unknown");

    EcmCloudOpreateActionEnum(String action, String desc) {
        this.action = action;
        this.desc = desc;
    }

    private String action;

    private String desc;

    public String getAction() {
        return action;
    }

    public String getDesc() {
        return desc;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = Maps.newHashMap();
        map.put("action", action);
        map.put("desc", desc);
        return map;
    }

    public static boolean validate(String action) {
        if (action == null) {
            return false;
        }
        for (EcmCloudOpreateActionEnum state : EcmCloudOpreateActionEnum.values()) {
            if (state.getAction().equals(action)) {
                return true;
            }
        }

        return false;
    }

}
