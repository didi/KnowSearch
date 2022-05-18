package com.didichuxing.datachannel.arius.admin.biz.template.new_srv.dcdr.impl;

import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.base.impl.BaseTemplateSrvImpl;
import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.dcdr.DCDRManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.constant.template.NewTemplateSrvEnum;
import org.springframework.stereotype.Service;

/**
 * @author chengxiang
 * @date 2022/5/16
 */
@Service
public class DCDRManagerImpl extends BaseTemplateSrvImpl implements DCDRManager {

    @Override
    public NewTemplateSrvEnum templateSrv() {
        return NewTemplateSrvEnum.TEMPLATE_DCDR;
    }

    @Override
    public Result<Void> isTemplateSrvAvailable(Integer logicTemplateId) {
        return Result.buildFail();
    }
}
