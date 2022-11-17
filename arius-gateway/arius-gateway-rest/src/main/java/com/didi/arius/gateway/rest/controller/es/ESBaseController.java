package com.didi.arius.gateway.rest.controller.es;

import com.didi.arius.gateway.rest.controller.BaseHttpRestController;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;

public abstract class ESBaseController extends BaseHttpRestController {
    protected static final ILog logger = LogFactory.getLog(ESBaseController.class);
}
