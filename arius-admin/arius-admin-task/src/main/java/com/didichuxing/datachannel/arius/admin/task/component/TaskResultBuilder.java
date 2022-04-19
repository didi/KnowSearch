package com.didichuxing.datachannel.arius.admin.task.component;
import com.didiglobal.logi.job.common.TaskResult;

public class TaskResultBuilder {
    private StringBuffer failMsg = new StringBuffer();

    public void append(String msg) {
        failMsg.append(msg + '\n');
    }

    public TaskResult build() {
        if (failMsg.length() > 0) {
            return new TaskResult(TaskResult.FAIL_CODE, failMsg.toString());
        } else {
            return TaskResult.SUCCESS;
        }
    }

}
