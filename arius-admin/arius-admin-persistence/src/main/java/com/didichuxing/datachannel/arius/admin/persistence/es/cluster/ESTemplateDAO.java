package com.didichuxing.datachannel.arius.admin.persistence.es.cluster;

import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateConstant.ES_OPERATE_MIN_TIMEOUT;
import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateConstant.ES_OPERATE_TIMEOUT;
import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateConstant.INDEX_SHARD_NUM;
import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateConstant.SINGLE_TYPE;
import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateConstant.TEMPLATE_DEFAULT_ORDER;

import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didiglobal.logi.elasticsearch.client.ESClient;
import com.didiglobal.logi.elasticsearch.client.model.type.ESVersion;
import com.didiglobal.logi.elasticsearch.client.request.index.gettemplate.ESIndicesGetTemplateRequest;
import com.didiglobal.logi.elasticsearch.client.request.index.puttemplate.ESIndicesPutTemplateRequest;
import com.didiglobal.logi.elasticsearch.client.response.indices.deletetemplate.ESIndicesDeleteTemplateResponse;
import com.didiglobal.logi.elasticsearch.client.response.indices.gettemplate.ESIndicesGetTemplateResponse;
import com.didiglobal.logi.elasticsearch.client.response.indices.puttemplate.ESIndicesPutTemplateResponse;
import com.didiglobal.logi.elasticsearch.client.response.setting.common.MappingConfig;
import com.didiglobal.logi.elasticsearch.client.response.setting.template.MultiTemplatesConfig;
import com.didiglobal.logi.elasticsearch.client.response.setting.template.TemplateConfig;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

/**
 * @author d06679
 * @date 2019/3/29
 */
@Repository
public class ESTemplateDAO extends BaseESDAO {

    /**
     * 修改模板表达式
     * @param cluster 集群
     * @param name 模板名字
     * @param expression 表达式
     * @return result
     */
    public boolean updateExpression(String cluster, String name, String expression) {
        ESClient client = esOpClient.getESClient(cluster);

        // 获取es中原来index template的配置
        ESIndicesGetTemplateResponse getTemplateResponse = client.admin().indices().prepareGetTemplate(name).execute()
            .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
        TemplateConfig templateConfig = getTemplateResponse.getMultiTemplatesConfig().getSingleConfig();

        // 修改表达式
        if (StringUtils.isNotBlank(expression)) {
            templateConfig.setTemplate(expression);
        }

        // 设置ES版本
        templateConfig.setVersion(client.getEsVersion());

        ESIndicesPutTemplateResponse response = client.admin().indices().preparePutTemplate(name)
            .setTemplateConfig(templateConfig).execute().actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);

        return response.getAcknowledged();
    }

    /**
     * 修改模板分片
     * @param cluster 集群
     * @param name 模板名字
     * @param shardNum 表达式
     * @return
     */
    public boolean updateShardNum(String cluster, String name, Integer shardNum) {
        ESClient client = esOpClient.getESClient(cluster);

        // 获取es中原来index template的配置
         ESIndicesGetTemplateResponse getTemplateResponse = getESIndicesGetTemplateResponse(cluster,
                name, 3);
        TemplateConfig templateConfig = getTemplateResponse.getMultiTemplatesConfig().getSingleConfig();

        // 修改分片数目
        if (shardNum != null && shardNum > 0) {
            templateConfig.setSettings(INDEX_SHARD_NUM, String.valueOf(shardNum));
        }

        // 设置ES版本
        templateConfig.setVersion(client.getEsVersion());

        ESIndicesPutTemplateResponse response = client.admin().indices().preparePutTemplate(name)
            .setTemplateConfig(templateConfig).execute().actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);

        return response.getAcknowledged();
    }

    /**
     * 修改模板rack和shard
     * @param cluster 集群
     * @param name 模板名字
     * @param shard shard
     * @return result
     */
    public boolean updateShard(String cluster, String name, Integer shard, Integer shardRouting) {
        ESClient client = esOpClient.getESClient(cluster);

        // 获取es中原来index template的配置
        ESIndicesGetTemplateResponse getTemplateResponse = client.admin().indices().prepareGetTemplate(name).execute()
            .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
        TemplateConfig templateConfig = getTemplateResponse.getMultiTemplatesConfig().getSingleConfig();

        if (shard != null && shard > 0) {
            templateConfig.setSettings(INDEX_SHARD_NUM, String.valueOf(shard));
        }

        // 设置ES版本
        templateConfig.setVersion(client.getEsVersion());

        ESIndicesPutTemplateResponse response = client.admin().indices().preparePutTemplate(name)
            .setTemplateConfig(templateConfig).execute().actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);

        return response.getAcknowledged();
    }

    /**
     * 删除模板
     * @param cluster 集群
     * @param templateName 模板名字
     * @return result
     */
    public boolean delete(String cluster, String templateName) {
        ESClient client = esOpClient.getESClient(cluster);
        ESIndicesDeleteTemplateResponse response = client.admin().indices().prepareDeleteTemplate(templateName)
            .execute().actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
        return response.getAcknowledged();
    }

    /**
     * 保存模板
     * @param cluster 集群
     * @param name 名字
     * @param expression 表达式
     * @param shard shard
     * @param shardRouting shardRouting
     * @return result
     */
    public boolean create(String cluster, String name, String expression, Integer shard, Integer shardRouting) {
        ESClient client = esOpClient.getESClient(cluster);

        // 获取es中原来index template的配置
        TemplateConfig templateConfig = null;
        try {
            ESIndicesGetTemplateResponse getTemplateResponse = client.admin().indices().prepareGetTemplate(name + "*")
                .execute().actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
            templateConfig = getTemplateResponse.getMultiTemplatesConfig().getSingleConfig();
        } catch (Exception e) {
            LOGGER.warn("class=ESTemplateDAO||method=create||msg=get src template fail||cluster={}||name={}", cluster,
                name);
        }

        if (templateConfig == null) {
            templateConfig = new TemplateConfig();
        }

        if (StringUtils.isNotBlank(expression)) {
            templateConfig.setTemplate(expression);
        }

        if (shard != null && shard > 0) {
            templateConfig.setSettings(INDEX_SHARD_NUM, String.valueOf(shard));
        }

        //开源版本不支持这个参数，先注释
        /*if (shardRouting != null) {
            templateConfig.setSettings(INDEX_SHARD_ROUTING_NUM, String.valueOf(shardRouting));
        }*/

        if (templateConfig.getOrder() == null) {
            templateConfig.setOrder(TEMPLATE_DEFAULT_ORDER);
        }

        templateConfig.setSettings(SINGLE_TYPE, "true");

        // 设置ES版本
        templateConfig.setVersion(client.getEsVersion());

        ESIndicesPutTemplateResponse response = client.admin().indices().preparePutTemplate(name)
            .setTemplateConfig(templateConfig).execute().actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);

        return response.getAcknowledged();
    }

    public boolean create(String cluster, String name, TemplateConfig templateConfig) {
        ESClient client = esOpClient.getESClient(cluster);

        // 设置ES版本
        templateConfig.setVersion(client.getEsVersion());

        // 向ES中创建模板流程
        ESIndicesPutTemplateResponse response = client.admin().indices().preparePutTemplate(name)
            .setTemplateConfig(templateConfig).execute().actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);

        return response.getAcknowledged();
    }

    /**
     * 更新模板配置
     * @param clusterName       集群名
     * @param templateName      模版名
     * @param templateConfig    模版配置
     * @return result
     */
    public boolean updateTemplate(String clusterName, String templateName, TemplateConfig templateConfig) {
        ESClient esClient = esOpClient.getESClient(clusterName);

        // 设置ES版本
        templateConfig.setVersion(esClient.getEsVersion());

        ESIndicesPutTemplateRequest request = new ESIndicesPutTemplateRequest();
        request.setTemplate(templateName);
        request.setTemplateConfig(templateConfig);

        ESIndicesPutTemplateResponse response = null;
        try {
            response = esClient.admin().indices().putTemplate(request).actionGet(120, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOGGER.warn(
                "class=ESTemplateDAO||method=updateTemplate||update template fail||clusterName={}||templateName={}||esVersion={}||templateConfig={}||msg={}",
                clusterName, templateName, esClient.getEsVersion(),
                templateConfig.toJson(ESVersion.valueBy(esClient.getEsVersion())), e.getMessage(), e);
            throw e;
        }

        return response.getAcknowledged();
    }

    /**
     * 获取模板信息
     * @param clusterName       集群名
     * @param templateName      模版名
     * @return result
     */
    public TemplateConfig getTemplate(String clusterName, String templateName) {
        MultiTemplatesConfig templatesConfig = getTemplates(clusterName, templateName,3);

        if (templatesConfig == null) {
            return null;
        }

        return templatesConfig.getSingleConfig();
    }

    /**
     * 获取所有引擎模板
     * @param clusters 集群列表
     * @return
     */
    public Map<String, TemplateConfig> getAllTemplate(List<String> clusters) {
        Map<String, TemplateConfig> map = new HashMap<>();
        for (String clusterName : clusters) {

            MultiTemplatesConfig templatesConfig = getTemplates(clusterName, null,3);

            if (null == templatesConfig) {
                return null;
            }
            map.putAll(templatesConfig.getTemplateConfigMap());
        }
        return map;
    }

    /**
     * 获取mapping配置
     * @param clusterName       集群名
     * @param templateName      模版名
     * @return result
     */
    public MappingConfig getTemplateMapping(String clusterName, String templateName) {
        MultiTemplatesConfig templatesConfig = getTemplates(clusterName, templateName,3);

        if (templatesConfig == null || templatesConfig.getSingleConfig() == null) {
            return null;
        }

        return templatesConfig.getSingleConfig().getMappings();
    }

    /**
     * 获取模板信息
     * @param clusterName       集群名
     * @param templateName      模版名
     * @return result
     */
    public MultiTemplatesConfig getTemplates(String clusterName, String templateName,Integer tryTimes) {

        LOGGER.warn("class=ESTemplateDAO||method=getTemplates||clusterName={}||templateName={}", clusterName,
            templateName);

        ESClient esClient = esOpClient.getESClient(clusterName);

        if (null == esClient) {
            return null;
        }
    
        ESIndicesGetTemplateResponse response = getESIndicesGetTemplateResponse(clusterName,
                templateName, tryTimes);
    
        if (response == null) {
            return null;
        }

        if (!EnvUtil.isOnline()) {
            LOGGER.warn("class=ESTemplateDAO||method=getTemplates||clusterName={}||templateName={}", clusterName,
                templateName);
        }

        return response.getMultiTemplatesConfig();
    }
    
    protected ESIndicesGetTemplateResponse getESIndicesGetTemplateResponse(String clusterName, String templateName,
                                                                           Integer tryTimes) {
        ESClient esClient = esOpClient.getESClient(clusterName);
    
        if (null == esClient) {
            return null;
        }
        Long minTimeoutNum = 1L;
        Long maxTimeoutNum = tryTimes.longValue();
        ESIndicesGetTemplateRequest request = new ESIndicesGetTemplateRequest();
        request.setTemplates(templateName);
        ESIndicesGetTemplateResponse response = null;
        do {
            try {
                response = esClient.admin().indices().getTemplate(request)
                        .actionGet(/*降低因为抖动导致的等待时常,等待时常从低到高进行重试*/minTimeoutNum * ES_OPERATE_MIN_TIMEOUT,
                                TimeUnit.SECONDS);
                
            } catch (Exception e) {
                LOGGER.warn(
                        "class=ESTemplateDAO||method=getTemplates||get templates fail||clusterName={}||templateName={}||msg={}",
                        clusterName, templateName, e.getMessage(), e);
            }
            minTimeoutNum++;
            if (minTimeoutNum > maxTimeoutNum) {
                minTimeoutNum = maxTimeoutNum;
            }
        } while (tryTimes-- > 0 && null == response);
        return response;
    }

    /**
     * 更新配置
     * @param cluster 集群
     * @param name    模板
     * @param setting setting
     * @return result
     */
    public boolean upsertSetting(String cluster, String name, Map<String, String> setting) {
        ESClient client = esOpClient.getESClient(cluster);

        // 获取es中原来index template的配置
        ESIndicesGetTemplateResponse getTemplateResponse = client.admin().indices().prepareGetTemplate(name).execute()
            .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
        TemplateConfig templateConfig = getTemplateResponse.getMultiTemplatesConfig().getSingleConfig();

        for (Map.Entry<String, String> entry : setting.entrySet()) {
            templateConfig.setSettings(entry.getKey(), entry.getValue());
        }

        ESIndicesPutTemplateResponse response = client.admin().indices().preparePutTemplate(name)
            .setTemplateConfig(templateConfig).execute().actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);

        return response.getAcknowledged();
    }

    /**
     * 拷贝模板
     * @param srcCluster 源集群
     * @param srcTemplateName 原模板
     * @param tgtCluster 目标集群
     * @param tgtTemplateName 目标模板
     * @return result
     */
    public boolean copyMappingAndAlias(String srcCluster, String srcTemplateName, String tgtCluster,
                                       String tgtTemplateName) {

        ESClient srcClient = esOpClient.getESClient(srcCluster);
        ESClient tgtClient = esOpClient.getESClient(tgtCluster);
        ESIndicesPutTemplateResponse response = new ESIndicesPutTemplateResponse();
        response.setAcknowledged(false);

        try {
            // 获取es中原来index template的配置
            ESIndicesGetTemplateResponse getSrcTemplateResponse = srcClient.admin().indices()
                .prepareGetTemplate(srcTemplateName).execute().actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
            TemplateConfig srcTemplateConfig = getSrcTemplateResponse.getMultiTemplatesConfig().getSingleConfig();

            // 获取es中目标index template的配置
            ESIndicesGetTemplateResponse getTgtTemplateResponse = tgtClient.admin().indices()
                .prepareGetTemplate(tgtTemplateName).execute().actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
            TemplateConfig tgtTemplateConfig = getTgtTemplateResponse.getMultiTemplatesConfig().getSingleConfig();

            if (srcTemplateConfig == null || tgtTemplateConfig == null) {
                return false;
            }

            tgtTemplateConfig.setMappings(srcTemplateConfig.getMappings());
            tgtTemplateConfig.setAliases(srcTemplateConfig.getAliases());
            tgtTemplateConfig.setVersion(tgtClient.getEsVersion());

            response = tgtClient.admin().indices().preparePutTemplate(tgtTemplateName)
                .setTemplateConfig(tgtTemplateConfig).execute().actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOGGER.error(
                "class=ESTemplateDAO||method=copyMappingAndAlias||srcCluster={}||srcTemplateName={}||tgtCluster={}||tgtTemplateName={}",
                srcCluster, srcTemplateName, tgtCluster, tgtTemplateName, e);
        }

        return response.getAcknowledged();
    }
}