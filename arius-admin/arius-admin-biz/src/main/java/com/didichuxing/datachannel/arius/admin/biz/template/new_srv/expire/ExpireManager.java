package com.didichuxing.datachannel.arius.admin.biz.template.new_srv.expire;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;

import java.util.Set;

/**
 * @author chengxiang
 * @date 2022/5/12
 */
public interface ExpireManager {

    /**
     * 删除过期索引
     * @param logicTemplateId 逻辑模板id
     * @return true/false
     */
    Result<Void> deleteExpireIndex(Integer logicTemplateId);

}
