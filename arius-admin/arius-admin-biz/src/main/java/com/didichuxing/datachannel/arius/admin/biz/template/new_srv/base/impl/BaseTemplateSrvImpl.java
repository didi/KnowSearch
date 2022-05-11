package com.didichuxing.datachannel.arius.admin.biz.template.new_srv.base.impl;

import com.didichuxing.datachannel.arius.admin.biz.template.TemplatePhyManager;
import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.TemplateSrvManager;
import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.base.BaseTemplateSrv;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author chengxiang
 * @date 2022/5/11
 */
public abstract class BaseTemplateSrvImpl implements BaseTemplateSrv {

    protected static final ILog LOGGER = LogFactory.getLog(BaseTemplateSrvImpl.class);

    @Autowired
    protected IndexTemplateService indexTemplateService;

    @Autowired
    protected IndexTemplatePhyService indexTemplatePhyService;

    @Autowired
    protected TemplatePhyManager templatePhyManager;

    @Autowired
    protected TemplateSrvManager templateSrvManager;

    @Override
    public boolean isTemplateSrvOpen(Integer logicTemplateId) {
        return templateSrvManager.isTemplateSrvOpen(logicTemplateId, templateSrv().getCode());
    }

    @Override
    public String templateSrvName() {
        return templateSrv().getServiceName();
    }

}
