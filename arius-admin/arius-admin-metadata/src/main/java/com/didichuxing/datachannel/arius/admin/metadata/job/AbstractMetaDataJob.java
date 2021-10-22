package com.didichuxing.datachannel.arius.admin.metadata.job;

import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;

public abstract class AbstractMetaDataJob {
    protected final ILog LOGGER = LogFactory.getLog(AbstractMetaDataJob.class);

    public abstract Object handleJobTask(String params);
}
