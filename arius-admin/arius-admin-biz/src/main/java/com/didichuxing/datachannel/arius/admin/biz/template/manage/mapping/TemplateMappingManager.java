package com.didichuxing.datachannel.arius.admin.biz.template.manage.mapping;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;

/**
 * @author chengxiang
 * @date 2022/5/27
 */
public interface TemplateMappingManager {

    /**
     * 校验模板mapping 字段是否合法
     * @param mapping
     * @return
     */
    Result<Void> validMapping(String mapping);
}
