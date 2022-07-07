package com.didichuxing.datachannel.arius.admin.common.constant.ecm;

import java.util.Set;

/**
 * 宙斯物理集群操作动作枚举类型
 * running   运行中
 * creating  创建中
 * failed    失败
 * skip      跳过
 * rodo      重做
 * ready     准备
 * updated   待更新
 * unknown   未知
 * @author   didi
 * @date 2020/9/25
 */
public enum EcmHostStatusEnum {
                               /**killing*/
                               KILLING("killing"),

                               KILL_FAILED("kill_failed"),

                               CANCELLED("cancelled"),

                               IGNORE("ignored"),

                               TIMEOUT("timeout"),

                               WAITING("waiting"),

                               RUNNING("running"),

                               FAILED("failed"),

                               READY("ready"),

                               UPDATED("updated"),

                               UPDATING("updating"),

                               SUCCESS("success"),

                               UNKNOWN("unknown");

    private final String value;

    EcmHostStatusEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static EcmHostStatusEnum valueFrom(String value) {
        if (value == null) {
            return null;
        }
        for (EcmHostStatusEnum state : EcmHostStatusEnum.values()) {
            if (state.getValue().equals(value)) {
                return state;
            }
        }
        return null;
    }

    public static boolean isSuccessFinished(Set<EcmHostStatusEnum> statusEnumSet) {
        if (statusEnumSet == null || statusEnumSet.isEmpty()) {
            return false;
        }
        return (statusEnumSet.contains(SUCCESS) && statusEnumSet.size() == 1)
                || (statusEnumSet.contains(IGNORE) && statusEnumSet.size() == 1)
                || (statusEnumSet.contains(IGNORE) && statusEnumSet.contains(SUCCESS) && statusEnumSet.size() == 2);
    }
}
