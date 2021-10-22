package com.didichuxing.datachannel.arius.admin.common.bean.entity.template;

import com.didichuxing.datachannel.arius.admin.client.bean.common.IndexTemplateValue;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppTemplateAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.quota.ESTemplateQuotaUsage;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithCluster;
import lombok.Data;

import java.io.Serializable;

/**
 * 逻辑模板聚合类
 * @author wangshu
 * @date 2020/09/11
 */
@Data
public class IndexTemplateLogicAggregate implements Serializable {

    /**
     * 具备逻辑集群信息的逻辑模板详情信息
     */
    private IndexTemplateLogicWithCluster indexTemplateLogicWithCluster;

    /**
     * APP对当前模板的权限
     */
    private AppTemplateAuth               appTemplateAuth;

    /**
     * 模板Quota使用
     */
    private ESTemplateQuotaUsage          esTemplateQuotaUsage;

    /**
     * 模板健康分
     */
    private IndexTemplateValue            indexTemplateValue;

    /**
     * 是否具有DCDR
     */
    private Boolean                       hasDCDR;
}
