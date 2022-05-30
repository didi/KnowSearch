package com.didichuxing.datachannel.arius.admin.biz.template.manage.create;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.TemplateCreateDTO;

/**
 * @author chengxiang
 * @date 2022/5/27
 */
public interface TemplateCreateManager {

    /**
     * 创建模板
     * @param param 模板信息
     * @param operator 操作者
     * @return
     */
    Result<Void> create(TemplateCreateDTO param, String operator, Integer appId);
}
