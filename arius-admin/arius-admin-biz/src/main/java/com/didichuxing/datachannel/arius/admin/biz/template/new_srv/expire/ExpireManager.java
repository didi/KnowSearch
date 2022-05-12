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

    /**
     * 获取模板过期的索引
     * @param physicalId 模板物理ID
     * @return set集合
     */
    Set<String> getExpireIndex(Long physicalId);

    /**
     * 删除模板过期索引
     *  1、可以是当前集群存在的物理模板
     *  2、可以是已经从当前集群迁移走的模板,但是还有数据在当前集群
     * @param physicalId 物理模板id
     * @return true/false
     */
    Result<Void> deleteTemplatePhyExpireIndex(Long physicalId);

    /**
     * 删除已经被删除的模板的索引
     * @param physical 物理模板信息
     * @return true/false
     */
    Result<Void> deleteTemplatePhyDeletedIndex(IndexTemplatePhy physical);
}
