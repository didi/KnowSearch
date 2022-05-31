package com.didichuxing.datachannel.arius.admin.biz.template.manage.mapping.impl;

import com.didichuxing.datachannel.arius.admin.biz.template.manage.mapping.TemplateMappingManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import org.springframework.stereotype.Service;

/**
 * @author chengxiang
 * @date 2022/5/27
 */
@Service
public class TemplateMappingManagerImpl implements TemplateMappingManager {

    //todo: 整合原有mapping 相关代码
    @Override
    public Result<Void> validMapping(String mapping) {
        return Result.buildSucc();
    }
}
