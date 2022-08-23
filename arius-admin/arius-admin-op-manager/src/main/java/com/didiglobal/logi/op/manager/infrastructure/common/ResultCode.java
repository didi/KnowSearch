package com.didiglobal.logi.op.manager.infrastructure.common;

import io.swagger.annotations.ApiModel;

/**
 * @author didi
 * @date 2022-07-05 10:20 上午
 */
@ApiModel(value = "返回说明")
public enum ResultCode {
    /* 成功 */
    SUCCESS(200, "成功"),

    /* 默认失败 */
    COMMON_FAIL(999, "失败"),

    /* 默认失败 */
    UNKNOW(-1, "未知"),

    /* 参数错误：1000～1999 */
    PARAM_NOT_VALID(1001, "参数无效"),
    PARAM_ERROR(1002, "参数错误"),

    /* 文件系统报错 */
    FILE_OPERATE_ERROR(2001, "文件操作失败"),

    /* zeus操作报错 */
    ZEUS_OPERATE_ERROR(3001, "zeus操作报错"),

    /*script操作报错 */
    SCRIPT_OPERATE_ERROR(4001, "脚本操作报错"),

    /*task不存在报错 */
    TASK_NOT_EXIST_ERROR(4001, "任务不存在"),
    TASK_IS_RUNNING(4002, "任务正在运行"),
    TASK_IS_FINISH(4003, "任务已经结束"),
    TASK_IS_RUNNING_OR_FINISH(4002, "任务正在运行或已结束"),
    TASK_IS_NOT_FAILED(4003, "任务处于非失败状态"),
    TASK_IS_NOT_RUNNING(4004, "任务处于非运行状态"),
    TASK_HOST_IS_NOT_EXIST(4005, "任务host不存在"),
    TASK_HOST_IS_NOT_ERROR(4006, "任务host状态未失败"),


    /*组件操作处理器处理失败 */
    HANDLER_OPERATE_ERROR(5001, "组件操作处理器处理失败");

    private final Integer code;

    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
