package com.didichuxing.datachannel.arius.admin.biz.extend.intfc;

import com.didichuxing.datachannel.arius.admin.common.bean.common.TemplateResourceConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;

/**
 * @author d06679
 * @date 2019-07-09
 */
public interface TemplateClusterConfigProvider {

    /**
     * 获取模板所在资源的配置
     * @param physicalId 物理模板的id
     * @return 配置
     */
    Result<TemplateResourceConfig> getTemplateResourceConfig(Long physicalId);

}
