package com.didichuxing.datachannel.arius.admin.biz.cluster.impl;

import com.didichuxing.datachannel.arius.admin.biz.extend.intfc.TemplateClusterConfigProvider;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.common.bean.common.TemplateResourceConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;

/**
 * @author d06679
 * @date 2019-07-09
 */
@Service("defaultTemplateClusterConfigProvider")
public class DefaultTemplateClusterConfigProvider implements TemplateClusterConfigProvider {

    @Override
    public Result<TemplateResourceConfig> getTemplateResourceConfig(Long physicalId) {
        return Result.buildSucc(new TemplateResourceConfig());
    }

}
