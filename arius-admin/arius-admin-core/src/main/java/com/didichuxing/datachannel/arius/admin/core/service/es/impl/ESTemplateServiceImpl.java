package com.didichuxing.datachannel.arius.admin.core.service.es.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.ESHttpRequestContent.getTemplateNameRequestContent;
import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateConstant.TEMPLATE_DEFAULT_ORDER;

import com.didichuxing.datachannel.arius.admin.common.bean.po.template.IndexTemplatePhyPO;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateHealthEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESTemplateService;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESOpTimeoutRetry;
import com.didichuxing.datachannel.arius.admin.persistence.es.cluster.ESTemplateDAO;
import com.didiglobal.logi.elasticsearch.client.gateway.direct.DirectResponse;
import com.didiglobal.logi.elasticsearch.client.response.setting.common.MappingConfig;
import com.didiglobal.logi.elasticsearch.client.response.setting.template.MultiTemplatesConfig;
import com.didiglobal.logi.elasticsearch.client.response.setting.template.TemplateConfig;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author d06679
 * @date 2019/4/2
 */
@Service
public class ESTemplateServiceImpl implements ESTemplateService {
    private static final ILog LOGGER = LogFactory.getLog(ESTemplateServiceImpl.class);

    @Autowired
    private ESTemplateDAO     esTemplateDAO;

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
     * @param shard      shard
     * @param retryCount 重试次数
     * @return result
     */
    @Override
    public boolean syncUpdateShard(String cluster, String name, Integer shard, Integer shardRouting,
                                   int retryCount) throws ESOperateException {
        return ESOpTimeoutRetry.esRetryExecute("updateTemplateRackAndShard", retryCount,
            () -> esTemplateDAO.updateShard(cluster, name, shard, shardRouting));
    }

    /**
     * 创建模板, 会覆盖之前的存在的
     * @param cluster    集群
     * @param name       模板名字
     * @param expression 表达式
     * @param shard      shard
     * @param retryCount 重试次数
     * @return result
     */
    @Override
    public boolean syncCreate(String cluster, String name, String expression, Integer shard, Integer shardRouting,
                              int retryCount) throws ESOperateException {
        return ESOpTimeoutRetry.esRetryExecute("createTemplate", retryCount,
            () -> esTemplateDAO.create(cluster, name, expression, shard, shardRouting));
    }

    @Override
    public boolean syncCreate(Map<String, String> settings, String cluster, String name, String expression,
                              MappingConfig mappings, int retryCount) throws ESOperateException {
        // 获取es中原来index template的配置
        TemplateConfig templateConfig = esTemplateDAO.getTemplate(cluster, name);
        if (templateConfig == null) {
            templateConfig = new TemplateConfig();
        }
        if (StringUtils.isNotBlank(expression)) {
            templateConfig.setTemplate(expression);
        }
        if (templateConfig.getOrder() == null) {
            templateConfig.setOrder(TEMPLATE_DEFAULT_ORDER);
        }
        if (MapUtils.isNotEmpty(settings)) {
            templateConfig.setSetttings(settings);
        }
        if (null != mappings) {
            templateConfig.setMappings(mappings);
        }
        TemplateConfig finalTemplateConfig = templateConfig;
        return ESOpTimeoutRetry.esRetryExecute("createTemplate", retryCount,
            () -> esTemplateDAO.create(cluster, name, finalTemplateConfig));
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

    @Override
    public boolean syncUpdateShardNum(String cluster, String name, Integer shardNum,
                                      int retryCount) throws ESOperateException {
        return ESOpTimeoutRetry.esRetryExecute("updateShardNum", retryCount,
            () -> esTemplateDAO.updateShardNum(cluster, name, shardNum));
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
    
    
    @Override
    public boolean syncUpdateSettingCheckAllocationAndShard(String cluster, String name, Map<String, String> setting, int retryCount)
            throws ESOperateException {
        return ESOpTimeoutRetry.esRetryExecute("updateSettingCheckAllocationAndShard", retryCount,
            () -> esTemplateDAO.updateSettingCheckAllocationAndShard(cluster, name, setting));
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
    public TemplateConfig syncGetTemplateConfig(String cluster, String name) throws ESOperateException {
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
    public MappingConfig syncGetMappingsByClusterName(String clusterName, String templateName)
            throws ESOperateException {
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
    public MultiTemplatesConfig syncGetTemplates(String clusterName, String templateName) throws ESOperateException {
        return ESOpTimeoutRetry.esRetryExecute("syncGetTemplates", 3,
                () -> esTemplateDAO.getTemplates(clusterName, templateName), Objects::isNull);
    }

    /**
     * 获取所有引擎模板
     * @param clusters 集群名
     * @return
     */
    @Override
    public Map<String, TemplateConfig> syncGetAllTemplates(List<String> clusters) throws ESOperateException {
        return esTemplateDAO.getAllTemplate(clusters);
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
            try {
                return ESOpTimeoutRetry.esRetryExecute("createTemplate", retryCount,
                    () -> esTemplateDAO.create(cluster, tgtName, templateConfig));
            } catch (Exception e) {
                LOGGER.error(
                    "class=ESTemplateServiceImpl||method=syncUpdateName||clusterName={}||srcName={}||tgtName={}||errMsg=exception",
                    cluster, srcName, tgtName);
            }
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
            } catch (Exception e) {
                LOGGER.error(
                    "class=ESTemplateServiceImpl||method=syncCheckTemplateConfig||clusterName={}||name={}||errMsg=exception",
                    cluster, name);
            }
        }
    }

    @Override
    public long syncGetTemplateNum(String cluster) {
        String templateNameRequestContent = getTemplateNameRequestContent();
        try {
            DirectResponse directResponse = esTemplateDAO.getDirectResponse(cluster, "Get", templateNameRequestContent);
            if (directResponse.getRestStatus() == RestStatus.OK
                && StringUtils.isNoneBlank(directResponse.getResponseContent())) {
                List<IndexTemplatePhyPO> indexBelongNodes = ConvertUtil
                    .str2ObjArrayByJson(directResponse.getResponseContent(), IndexTemplatePhyPO.class);

                return indexBelongNodes.stream().map(IndexTemplatePhyPO::getName).filter(r -> !r.startsWith("."))
                    .count();
            }
        } catch (Exception e) {
            LOGGER.error("class=ESTemplateServiceImpl||method=syncGetTemplateNum||clusterName={}||errMsg=exception",
                cluster, e.getMessage());
            return 0;
        }

        return 0;
    }

    @Override
    public long synGetTemplateNumForAllVersion(String cluster) throws ESOperateException {
        Map<String, TemplateConfig> allTemplate = esTemplateDAO.getAllTemplate(Collections.singletonList(cluster));
        return MapUtils.isEmpty(allTemplate) ? 0 : allTemplate.size();
    }
    
    /**
     * @param cluster
     * @return
     */
    @Override
    public boolean syncGetEsClusterIsNormal(String cluster) {
        return esTemplateDAO.syncGetClusterIsNormal(cluster);
    }
    
    /**
     * > 检查指定集群的索引是否匹配指定的表达式和模板健康状态
     *
     * @param cluster            集群名称
     * @param expression         索引的表达式，如“log-*”
     * @param templateHealthEnum 模板的健康状态，为枚举类型，枚举值如下：
     * @return 布尔值
     */
    @Override
    public boolean hasMatchHealthIndexByExpressionTemplateHealthEnum(String cluster, String expression,
                                                                     TemplateHealthEnum templateHealthEnum) {
        try {
            //无需关心底层的异常原因，这里在于，我们获取的的分区和非分区，一般来说，分区索引会自动+*，所以不会有错误的异常抛出
            //那么非分区的话，就会精确到每一个索引名称，此时有可能出现index_not_found_exception，所以这里默认返回false即可
            return esTemplateDAO.hasMatchHealthIndexByExpressionTemplateHealthEnum(cluster, expression,
                    templateHealthEnum);
        } catch (ESOperateException e) {
            LOGGER.error("class={}||method=hasMatchHealthIndexByExpressionTemplateHealthEnum||clusterName={}",
                    getClass().getSimpleName(), cluster, e);
            return false;
        }
        
    }
}