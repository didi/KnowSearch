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
    ;

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
