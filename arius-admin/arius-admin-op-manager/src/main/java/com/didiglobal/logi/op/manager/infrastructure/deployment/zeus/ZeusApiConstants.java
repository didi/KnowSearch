package com.didiglobal.logi.op.manager.infrastructure.deployment.zeus;

/**
 * @author didi
 * @date 2022-09-08 16:59
 */
public class ZeusApiConstants {

    public static final String API_TEMPLATE = "http://%s/api/grp/%s/tpl/new?token=%s";

    public static final String API_EDIT_TEMPLATE = "http://%s/api/tpl/%s/edit?token=%s";

    public static final String API_EXECUTE_TASK = "http://%s/api/task?token=%s";

    public static final String API_TASK_STATUS = "http://%s/api/task/%s/result";

    public static final String API_TEMPLATE_REMOVE = "http://%s/api/tpl/%s?token=%s";

    public static final String API_TASK_ACTION = "http://%s/api/task/action?token=%s";

    public static final String API_HOST_ACTION = "http://%s/api/task/host-action?token=%s";

    public static final String API_TASK_STDOUTS = "http://%s/api/task/%s/stdouts.json?hostname=%s";

    public static final String API_TASK_STDERRS = "http://%s/api/task/%s/stderrs.json?hostname=%s";
}
