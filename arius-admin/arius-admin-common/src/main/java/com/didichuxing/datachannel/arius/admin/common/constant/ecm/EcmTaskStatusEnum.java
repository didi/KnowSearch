package com.didichuxing.datachannel.arius.admin.common.constant.ecm;

import java.util.Set;

/**
 * 宙斯物理集群操作动作枚举类型
 * success     成功
 * failed      失败
 * running    执行中
 * waiting     等待
 * cancel      取消
 * unknown     未知
 * @author     didi
 * @date 2020/10/10
 */
public enum EcmTaskStatusEnum {
                               /**success*/
                               SUCCESS("success"),

                               FAILED("failed"),

                               RUNNING("running"),

                               WAITING("waiting"),

                               PAUSE("pause"),

                               IGNORE("ignore"),

                               CANCEL("cancel"),

                               UNKNOWN("unknown");

    private String value;

    EcmTaskStatusEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static EcmTaskStatusEnum valueFrom(String value) {
        if (value == null) {
            return EcmTaskStatusEnum.UNKNOWN;
        }
        for (EcmTaskStatusEnum state : EcmTaskStatusEnum.values()) {
            if (state.getValue().equals(value)) {
                return state;
            }
        }
        return EcmTaskStatusEnum.UNKNOWN;
    }

    /**
     * 计算工单状态
     * @param subStatusSet
     * @return
     */
    public static EcmTaskStatusEnum calTaskStatus(Set<EcmTaskStatusEnum> subStatusSet) {
        if (subStatusSet == null) {
            return EcmTaskStatusEnum.UNKNOWN;
        }

        if (subStatusSet.isEmpty()) {
            return EcmTaskStatusEnum.SUCCESS;
        }

        if (subStatusSet.contains(EcmTaskStatusEnum.WAITING) && subStatusSet.size() == 1) {
            return EcmTaskStatusEnum.PAUSE;
        }
        if (subStatusSet.contains(EcmTaskStatusEnum.CANCEL) && subStatusSet.size() == 1) {
            return EcmTaskStatusEnum.CANCEL;
        }

        if (subStatusSet.contains(EcmTaskStatusEnum.FAILED)) {
            return EcmTaskStatusEnum.FAILED;
        }

        if (subStatusSet.contains(EcmTaskStatusEnum.RUNNING)) {
            return EcmTaskStatusEnum.RUNNING;
        }

        if ((subStatusSet.contains(EcmTaskStatusEnum.SUCCESS) && subStatusSet.size() == 1)
            || (subStatusSet.contains(EcmTaskStatusEnum.IGNORE) && subStatusSet.size() == 1)
            || (subStatusSet.contains(EcmTaskStatusEnum.SUCCESS) && subStatusSet.contains(EcmTaskStatusEnum.IGNORE)
                && subStatusSet.size() == 2)) {
            //1 只有SUCCESS；2 只有IGNORE；3 只有SUCCESS和IGNORE  --> SUCCESS
            return EcmTaskStatusEnum.SUCCESS;
        }

        if (subStatusSet.contains(EcmTaskStatusEnum.PAUSE)
            || (subStatusSet.contains(EcmTaskStatusEnum.WAITING) && subStatusSet.contains(EcmTaskStatusEnum.SUCCESS)
                && subStatusSet.size() == 2)) {
            //1 只有PAUSE；2 走到暂停点，只有WAITING 和 SUCCESS
            return EcmTaskStatusEnum.PAUSE;
        }

        if (subStatusSet.contains(EcmTaskStatusEnum.UNKNOWN)) {
            return EcmTaskStatusEnum.UNKNOWN;
        }

        return EcmTaskStatusEnum.UNKNOWN;
    }
}
