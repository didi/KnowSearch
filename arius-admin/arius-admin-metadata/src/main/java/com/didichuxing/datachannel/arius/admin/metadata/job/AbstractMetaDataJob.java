package com.didichuxing.datachannel.arius.admin.metadata.job;

import java.util.List;

import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;

public abstract class AbstractMetaDataJob {
    protected static final ILog LOGGER           = LogFactory.getLog(AbstractMetaDataJob.class);
    public static final int     WARN_BLOCK_SIZE  = 10;
    public static final int     ERROR_BLOCK_SIZE = 30;

    public abstract Object handleJobTask(String params);

    public Object handleBrocastJobTask(String params, String curretnWorker, List<String> allWorders) {
        return null;
    }
}
