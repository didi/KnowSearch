package com.didi.cloud.fastdump.common.content;


public enum ResultType {
                        /**操作成功*/
                        SUCCESS(200, "操作成功"),
                        FAST_DUMP_EXIST(1000, "服务存在"),
                        FAST_DUMP_NOT_EXIST(1001, "服务不存在"),
                        FAIL(19999, "操作失败"),
                        ILLEGAL_PARAMS(10000, "参数错误"),
                        HTTP_REQ_ERROR(10001, "第三方http请求异常"),
                        OPERATE_FORBIDDEN_ERROR(10002, "无权限"),
                        NOT_FIND_SUB_CLASS(10003, "找不到实现类"),
                        NOT_SUPPORT_ES_VERSION(10004, "不支持ES版本"),

                        NOT_FIND_ES_CLIENT(10005, "获取ESClient失败"),
                        TRANSPORT_ERROR(10006, "请求失败"),
                        ES_OPERATE_ERROR(10007, "es操作失败");

    private final Integer code;
    private final String  message;

    ResultType(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}