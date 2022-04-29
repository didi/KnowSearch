package com.didichuxing.datachannel.arius.admin.common.constant.result;

/**
 * rest请求返回类型
 *
 * @author d06679
 */
public enum ResultType {
                        /**操作成功*/
                        SUCCESS(0, "操作成功"),

                        FAIL(19999, "操作失败"),

                        ILLEGAL_PARAMS(10000, "参数错误"),

                        RESOURCE_NOT_READY(10001, "资源未就绪"),

                        RESOURCE_PROCESSING(10002, "资源审批中"),

                        ES_OPERATE_ERROR(10003, "es操作失败"),

                        DUPLICATION(10004, "数据已存在"),

                        NOT_EXIST(10005, "数据不存在"),

                        APP_VERIFY_SUCCESS(10006, "APP校验通过"),

                        APP_VERIFY_FAIL_APP_NOT_EXIST(10007, "APP校验失败:APPID不存在"),

                        APP_VERIFY_FAIL_VERIFY_ERROR(10008, "APP校验失败:appsecret不正确"),

                        HTTP_REQ_ERROR(10009, "第三方http请求异常"),

                        OPERATE_FORBIDDEN_ERROR(10010, "无权限"),

                        ADMIN_OPERATE_ERROR(10011, "Admin操作失败"),

                        IN_USE_ERROR(10012, "使用中"),

                        WORK_ORDER_NOT_SUPPORT_ERROR(10013, "工单类型不支持"),

                        AMS_SERVER_ERROR(10015, "ams服务异常"),

                        ODIN_SERVER_ERROR(10016, "odin服务异常"),

                        ECM_SERVER_ERROR(10017, "ecm服务异常"),

                        ADMIN_META_ERROR(10018, "admin元数据异常"),

                        RESOURCE_NOT_ENOUGH(10019, "集群资源不足"),

                        EXTEND_SERVICE_ERROR(10020, "扩展服务异常"),

                        NO_CAPACITY_PLAN(10021, "集群没有容量规划"),

                        ADMIN_TASK_ERROR(10022, "admin任务异常"),

                        ORDER_ALREADY_HANDLED(10023, "工单已审批"),

                        STORAGE_DOWNLOAD_FILE_FAILED(10024, "download file failed"),

                        NO_FIND_SUB_CLASS(10025, "找不到实现类"),

                        CLUSTER_LOGIC_TYPE_ERROR(10026, "逻辑集群类型不存在"),

                        N9E_SERVER_ERROR(10027, "夜莺服务异常"),

                        ARIUS_GATEWAY_ERROR(10028, "arius gateway 异常"),

                        FILE_UPLOAD_ERROR(10029, "upload file failed"),

                        NOT_SUPPORT_ERROR(10030, "接口不再支持"),
    ;

    private Integer code;
    private String  message;

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