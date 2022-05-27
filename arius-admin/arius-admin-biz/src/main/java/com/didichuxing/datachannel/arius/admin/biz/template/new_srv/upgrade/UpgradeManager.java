package com.didichuxing.datachannel.arius.admin.biz.template.new_srv.upgrade;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;

/**
 * @author chengxiang
 * @date 2022/5/16
 */
public interface UpgradeManager {

    /**
     * 模板升级
     * @param logicTemplateId 模板id
     * @return
     */
    Result<Void> upgradeTemplate(Integer logicTemplateId);

}
