package com.didichuxing.datachannel.arius.admin.common.constant.task;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

/**
 * 数据迁移索引子任务状态
 *
 * @author didi
 * @date 2022/11/01
 */
public enum FastIndexTaskStatusEnum {
                                     /**
                                      * 状态：-1.未提交 0.等待执行 1.执行中 2.执行成功 3.执行失败 4.已取消
                                      */
                                     NOT_SUBMITTED(-1, "unSubmit", "未提交"),

                                     WAITING(0, "waiting", "等待执行"),

                                     RUNNING(1, "running", "执行中"),

                                     SUCCESS(2, "success", "执行成功"),

                                     FAILED(3, "failed", "执行失败"),

                                     CANCEL(4, "cancel", "已取消"),

                                     PAUSE(5, "pause", "暂停");

    FastIndexTaskStatusEnum(Integer value, String code, String status) {
        this.value = value;
        this.code = code;
        this.status = status;
    }

    private Integer value;

    private String  code;
    private String  status;

    public Integer getValue() {
        return value;
    }

    public String getCode() {
        return code;
    }

    public String getStatus() {
        return status;
    }

    public static FastIndexTaskStatusEnum enumOfCode(String code) {

        for (FastIndexTaskStatusEnum statusEnum : FastIndexTaskStatusEnum.values()) {
            if (StringUtils.equals(code, statusEnum.getCode())) {
                return statusEnum;
            }
        }
        return FastIndexTaskStatusEnum.NOT_SUBMITTED;
    }

    public static FastIndexTaskStatusEnum enumOfValue(Integer value) {

        for (FastIndexTaskStatusEnum statusEnum : FastIndexTaskStatusEnum.values()) {
            if (Objects.equals(value, statusEnum.getValue())) {
                return statusEnum;
            }
        }
        return FastIndexTaskStatusEnum.NOT_SUBMITTED;
    }

}
