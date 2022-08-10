package com.didiglobal.logi.op.manager.infrastructure.common.enums;

/**
 * @author didi
 * @date 2022-07-13 10:18 上午
 */
public enum TaskStatusEnum {
    WAITING(0, "待执行"),

    RUNNING(1, "执行中"),

    FAILED(2, "失败"),

    /**
     * 主任务特有
     */
    PAUSE(3, "暂停"),

    SUCCESS(4, "成功"),

    CANCELLED(5, "取消"),

    KILLED(6, "杀死"),

    /**
     * 任务详情中host特有
     */
    TIMEOUT(7, "超时"),

    /**
     * 任务详情中host特有
     */
    IGNORED(8, "忽略"),

    /**
     * 未知状态
     */
    UN_KNOW(9, "未知");

    private int status;
    private String describe;

    TaskStatusEnum(int status, String describe) {
        this.status = status;
        this.describe = describe;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }
}
