package com.didichuxing.datachannel.arius.admin.biz.template;

import com.didichuxing.datachannel.arius.admin.common.bean.common.TemplateResourceConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;

/**
 * @author d06679
 * @date 2019-08-04
 */
public interface TemplateAction {

    /**
     * 自动获取资源
     * @param logicDTO 模板
     * @param operator 操作人
     * @return result
     * @throws AdminOperateException exception
     */
    Result<Integer> createWithAutoDistributeResource(IndexTemplateInfoDTO logicDTO,
                                                     String operator) throws AdminOperateException;

    /**
     * 扩缩容
     * @param logicId 逻辑id
     * @param expectHotTime 期望热数据保存天数
     * @param expectExpireTime 期望保存周期
     * @param expectQuota 期望quota
     * @param submitor 操作人
     * @return result
     */
    Result<Void> indecreaseWithAutoDistributeResource(Integer logicId, Integer expectHotTime, Integer expectExpireTime, Double expectQuota,
                                                      String submitor) throws AdminOperateException;

    /**
     * 获取物理模板资源配置
     * @param physicalId 物理模板id
     * @return result
     */
    TemplateResourceConfig getPhysicalTemplateResourceConfig(Long physicalId);
}
