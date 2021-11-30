package com.didichuxing.datachannel.arius.admin.core.notify;

import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.component.RestTool;

@Component
public class NotifyTool {

    private static final ILog LOGGER      = LogFactory.getLog(NotifyTool.class);

    @Autowired
    private RestTool restTool;

    public Result<Void> byEmail(String title, String toUsers, String copyTo, String content) {
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    String.format("class=NotifyTool||method=byEmail||debugMsg={title={%s}, toUsers={%s}, copyTo={%s}, content={%s}},", title, toUsers, copyTo, content)
            );
        }
        return Result.buildSucc();
    }

    public Result<Void> bySms(String toUsers, String content) {
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    String.format("class=NotifyTool||method=bySms||debugMsg={toUsers={%s}, content={%s}},", toUsers, content)
            );
        }
        return Result.buildSucc();
    }

    public Result<Void> byVoice(String toUsers, String content) {
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    String.format("class=NotifyTool||method=byVoice||debugMsg={toUsers={%s}, content={%s}},", toUsers, content)
            );
        }
        return Result.buildSucc();
    }

}
