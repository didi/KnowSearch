package com.didichuxing.datachannel.arius.admin.core.service.es.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant.ARIUS_COMMON_GROUP;

import java.util.Map;
import java.util.Set;

import com.didichuxing.datachannel.arius.elasticsearch.client.response.setting.common.MappingConfig;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.setting.template.MultiTemplatesConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESTemplateService;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESOpTimeoutRetry;
import com.didichuxing.datachannel.arius.admin.persistence.es.cluster.ESTemplateDAO;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.setting.template.TemplateConfig;

import javax.annotation.Nullable;

/**
 * @author d06679
 * @date 2019/4/2
 */
@Service
public class ESTemplateServiceImpl implements ESTemplateService {

    @Autowired
    private ESTemplateDAO          esTemplateDAO;

    @Autowired
    private AriusConfigInfoService ariusConfigInfoService;

    /**
     * 删除模板
     * @param cluster    集群名字
     * @param name       模板名字
     * @param retryCount 重试次数
     * @return result
     */
    @Override
    public boolean syncDelete(String cluster, String name, int retryCount) throws ESOperateException {
        return ESOpTimeoutRetry.esRetryExecute("deleteTemplate", retryCount, () -> esTemplateDAO.delete(cluster, name));
    }

    /**
     * 修改模板rack和shard
     *
     * @param cluster    集群
     * @param name       模板明细
     * @param rack       rack
     * @param shard      shard
     * @param retryCount 重试次数
     * @return result
     */
    @Override
    public boolean syncUpdateRackAndShard(String cluster, String name, String rack, Integer shard, Integer shardRouting,
                                          int retryCount) throws ESOperateException {
        Set<String> shardRoutingEnableClusters = ariusConfigInfoService.stringSettingSplit2Set(ARIUS_COMMON_GROUP,
            "shard.routing.enable.clusters", "", ",");

        return ESOpTimeoutRetry.esRetryExecute("updateTemplateRackAndShard", retryCount,
            () -> esTemplateDAO.updateRackAndShard(cluster, name, rack, shard,
                shardRoutingEnableClusters.contains(cluster) ? shardRouting : null));
    }

    /**
     * 创建模板, 会覆盖之前的存在的
     * @param cluster    集群
     * @param name       模板名字
     * @param expression 表达式
     * @param rack       rack
     * @param shard      shard
     * @param retryCount 重试次数
     * @return result
     */
    @Override
    public boolean syncCreate(String cluster, String name, String expression, String rack, Integer shard,
                              Integer shardRouting, int retryCount) throws ESOperateException {
        Set<String> shardRoutingEnableClusters = ariusConfigInfoService.stringSettingSplit2Set(ARIUS_COMMON_GROUP,
            "shard.routing.enable.clusters", "", ",");
        return ESOpTimeoutRetry.esRetryExecute("createTemplate", retryCount, () -> esTemplateDAO.create(cluster, name,
            expression, rack, shard, shardRoutingEnableClusters.contains(cluster) ? shardRouting : null));
    }

    /**
     * 修改模板
     * @param cluster    集群
     * @param name       模板名字
     * @param expression 表达式
     * @param retryCount 重试次数
     * @return result
     * @throws ESOperateException
     */
    @Override
    public boolean syncUpdateExpression(String cluster, String name, String expression,
                                        int retryCount) throws ESOperateException {
        return ESOpTimeoutRetry.esRetryExecute("updateExpression", retryCount,
            () -> esTemplateDAO.updateExpression(cluster, name, expression));
    }

    /**
     * 修改模板setting
     *
     * @param cluster    集群
     * @param name       模板明细
     * @param setting    配置
     * @param retryCount 重试次数
     * @return result
     * @throws ESOperateException
     */
    @Override
    public boolean syncUpsertSetting(String cluster, String name, Map<String, String> setting,
                                     int retryCount) throws ESOperateException {
        return ESOpTimeoutRetry.esRetryExecute("upsertSetting", retryCount,
            () -> esTemplateDAO.upsertSetting(cluster, name, setting));
    }

    /**
     * 同步更新物理模板配置
     * @param cluster 集群名称
     * @param templateName 物理模板名称
     * @param templateConfig 模板配置
     * @param retryCount 重试次数
     * @return
     * @throws ESOperateException
     */
    @Override
    public boolean syncUpdateTemplateConfig(String cluster, String templateName, TemplateConfig templateConfig,
                                            int retryCount) throws ESOperateException {
        return ESOpTimeoutRetry.esRetryExecute("updateTemplateConfig", retryCount,
            () -> esTemplateDAO.updateTemplate(cluster, templateName, templateConfig));
    }

    /**
     * 跨集群拷贝模板mapping和索引
     *
     * @param srcCluster      源集群
     * @param srcTemplateName 原模板
     * @param tgtCluster      目标集群
     * @param tgtTemplateName 目标模板
     * @param retryCount      重试次数
     * @return result
     */
    @Override
    public boolean syncCopyMappingAndAlias(String srcCluster, String srcTemplateName, String tgtCluster,
                                           String tgtTemplateName, int retryCount) throws ESOperateException {
        return ESOpTimeoutRetry.esRetryExecute("syncCopyMappingAndAlias", retryCount,
            () -> esTemplateDAO.copyMappingAndAlias(srcCluster, srcTemplateName, tgtCluster, tgtTemplateName));
    }

    /**
     * 获取模板信息
     *
     * @param cluster 集群
     * @param name    名字
     * @return Config
     */
    @Override
    public TemplateConfig syncGetTemplateConfig(String cluster, String name) {
        if (StringUtils.isBlank(cluster) || StringUtils.isBlank(name)) {
            return null;
        }

        return esTemplateDAO.getTemplate(cluster, name);
    }

    /**
     * 获取mapping配置
     *
     * @param clusterName
     * @param templateName
     * @return
     */
    @Override
    public MappingConfig syncGetMappingsByClusterName(String clusterName, String templateName) {
        MultiTemplatesConfig templatesConfig = syncGetTemplates(clusterName, templateName);

        if (templatesConfig == null || templatesConfig.getSingleConfig() == null) {
            return null;
        }

        return templatesConfig.getSingleConfig().getMappings();
    }

    /**
     * 获取集群模板配置
     * @param clusterName 集群名称
     * @param templateName 模板名称
     * @return
     */
    @Override
    public MultiTemplatesConfig syncGetTemplates(String clusterName, String templateName) {
        return esTemplateDAO.getTemplates(clusterName, templateName);
    }

    /**
     * 修改模板名称
     *
     * @param cluster    集群
     * @param srcName    源名称
     * @param tgtName    现名称
     * @param retryCount
     * @return
     */
    @Override
    public boolean syncUpdateName(String cluster, String srcName, String tgtName,
                                  int retryCount) throws ESOperateException {
        TemplateConfig templateConfig = esTemplateDAO.getTemplate(cluster, srcName);
        if (templateConfig == null) {
            return false;
        }

        if (syncDelete(cluster, srcName, retryCount)) {
            return ESOpTimeoutRetry.esRetryExecute("createTemplate", retryCount,
                () -> esTemplateDAO.create(cluster, tgtName, templateConfig));
        }

        return false;
    }

    @Override
    public boolean syncCheckTemplateConfig(String cluster, String name, TemplateConfig templateConfig,
                                           int retryCount) throws ESOperateException {
        try {
            return ESOpTimeoutRetry.esRetryExecute("preCreateTemplate", retryCount,
                () -> esTemplateDAO.create(cluster, name, templateConfig));
        } finally {
            try {
                ESOpTimeoutRetry.esRetryExecute("deleteTemplate", retryCount,
                    () -> esTemplateDAO.delete(cluster, name));
            } catch (Exception ignored) {
            }
        }
    }

}
