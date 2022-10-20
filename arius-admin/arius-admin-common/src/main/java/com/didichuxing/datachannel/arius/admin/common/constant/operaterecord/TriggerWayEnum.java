package com.didichuxing.datachannel.arius.admin.common.constant.operaterecord;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 触发方式枚举
 *
 * @author shizeying
 * @date 2022/06/14
 */
public enum TriggerWayEnum {
                            /**
                             * 手动触发
                             */
                            MANUAL_TRIGGER("手动触发", 0),
                            /**
                             * 系统触发
                             */
                            SYSTEM_TRIGGER("系统触发", 1),
                            /**
                             * 调度任务scheduling_tasks
                             */
                            SCHEDULING_TASKS("调度任务", 2);

    /**
     * 触发方式
     */
    private final String  triggerWay;
    private final Integer code;

    TriggerWayEnum(String triggerWay, int code) {
        this.triggerWay = triggerWay;
        this.code = code;
    }

    public String getTriggerWay() {
        return triggerWay;
    }

    public Integer getCode() {
        return code;
    }

    public static Map<String, Integer> getOperationList() {
        return Arrays.stream(TriggerWayEnum.values())
            .collect(Collectors.toMap(TriggerWayEnum::getTriggerWay, TriggerWayEnum::getCode));

    }

    public static TriggerWayEnum getTriggerWayEnum(Integer code) {
        return Arrays.stream(TriggerWayEnum.values()).filter(triggerWayEnum -> triggerWayEnum.getCode().equals(code))
            .findFirst().orElse(null);
    }
}