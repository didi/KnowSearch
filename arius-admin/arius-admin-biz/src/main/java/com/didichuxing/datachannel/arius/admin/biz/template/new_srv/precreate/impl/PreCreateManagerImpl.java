package com.didichuxing.datachannel.arius.admin.biz.template.new_srv.precreate.impl;

import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.base.impl.BaseTemplateSrvImpl;
import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.precreate.PreCreateManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;

/**
 * @author chengxiang, zqr
 * @date 2022/5/11
 */
public class PreCreateManagerImpl extends BaseTemplateSrvImpl implements PreCreateManager {

    @Override
    public TemplateServiceEnum templateSrv() {
        return TemplateServiceEnum.TEMPLATE_PRE_CREATE;
    }

    @Override
    public Result<Void> preCreateIndex(Integer logicTemplateId) {
        return Result.buildSucc();
    }

    @Override
    public Result<Void> reBuildTomorrowIndex(Integer logicTemplateId) {
        return Result.buildSucc();
    }

    @Override
    public void asyncCreateTodayAndTomorrowIndexByPhysicalId(Long physicalId) {
    }
}
