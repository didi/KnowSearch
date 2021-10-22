package com.didichuxing.datachannel.arius.admin.core.notify;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.component.RestTool;

@Component
public class NotifyTool {

    @Autowired
    private RestTool restTool;

    public Result byEmail(String title, String toUsers, String copyTo, String content) {
        return Result.buildSucc();

    }

    public Result bySms(String toUsers, String content) {
        return Result.buildSucc();
    }

    public Result byVoice(String toUsers, String content) {
        return Result.buildSucc();
    }

}
