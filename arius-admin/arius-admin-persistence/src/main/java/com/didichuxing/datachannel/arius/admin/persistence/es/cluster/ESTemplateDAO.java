package com.didichuxing.datachannel.arius.admin.persistence.es.cluster;

import static com.didichuxing.datachannel.arius.admin.common.RetryUtils.performTryTimesMethods;
import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateConstant.ES_OPERATE_TIMEOUT;
import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateConstant.INDEX_SHARD_NUM;
import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateConstant.TEMPLATE_DEFAULT_ORDER;

import com.didichuxing.datachannel.arius.admin.common.constant.ESSettingConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateHealthEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.function.BiFunctionWithESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ParsingExceptionUtils;
import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didiglobal.logi.elasticsearch.client.ESClient;
import com.didiglobal.logi.elasticsearch.client.gateway.direct.DirectRequest;
import com.didiglobal.logi.elasticsearch.client.gateway.direct.DirectResponse;
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
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.rest.RestStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author d06679
 * @date 2019/3/29
 */
@Repository
public class ESTemplateDAO extends BaseESDAO {
    public static final String CAT_INDIES_HEALTH = "/_cat/indices/%s?v=true&format=json&health=%s&filter_path=index";
    public static final String CAT_INDIES = "/_cat/indices/%s?v=true&format=json&filter_path=index";

    /**
     * 修改模板表达式
     * @param cluster 集群
     * @param name 模板名字
     * @param expression 表达式
     * @return result
     */
    public boolean updateExpression(String cluster, String name, String expression) {
        ESClient client = esOpClient.getESClient(cluster);
        if (client == null) {
            LOGGER.warn("class={}||method=updateExpression||clusterName={}||expression={}||errMsg=esClient is null",
                    getClass().getSimpleName(), cluster, expression);
            return false;
        }

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
    public boolean updateShardNum(String cluster, String name, Integer shardNum) throws ESOperateException{
        ESClient client = esOpClient.getESClient(cluster);
        if (client == null) {
            LOGGER.warn("class={}||method=updateShardNum||clusterName={}||shardNum={}||errMsg=esClient is null",
                    getClass().getSimpleName(), cluster, shardNum);
            throw new ESOperateException(String.format("集群【%s】异常，无法更新模版【%s】分片",cluster,name));
        }

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
        if (client == null) {
            LOGGER.warn("class={}||method=updateShard||clusterName={}||shardNum={}||errMsg=esClient is null",
                    getClass().getSimpleName(), cluster, shard);
            return false;
        }

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
        if (client == null) {
            LOGGER.warn("class={}||method=delete||clusterName={}||templateName={}||errMsg=esClient is null",
                    getClass().getSimpleName(), cluster, templateName);
            return false;
        }
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
    public boolean create(String cluster, String name, String expression, Integer shard, Integer shardRouting) throws ESOperateException {
        ESClient client = esOpClient.getESClient(cluster);
        if (client == null) {
            LOGGER.warn(
                    "class=ESTemplateDAO||method=create||msg=es client is null||cluster={}||name={}||expression={}||shard={}||shardRouting={}",
                    cluster, name, expression, shard, shardRouting);
            return Boolean.FALSE;
        }
    
        // 获取es中原来index template的配置
        BiFunctionWithESOperateException<Long, TimeUnit, ESIndicesGetTemplateResponse> getTemplateResponseBiFunction = (time, unit) -> {
            try {
                return client.admin().indices().prepareGetTemplate(name + "*").execute()
                        .actionGet(time,unit);
            } catch (Exception e) {
                final String exception = ParsingExceptionUtils.getESErrorMessageByException(e);
                if (Objects.nonNull(exception)) {
                    throw new ESOperateException(exception);
                }
                LOGGER.warn("class=ESTemplateDAO||method=create||msg=get src template fail||cluster={}||name={}",
                        cluster, name);
                throw new ESOperateException(e.getMessage());
            }
        };
        TemplateConfig templateConfig = Optional.ofNullable(
                        performTryTimesMethods(getTemplateResponseBiFunction, Objects::isNull, 1))
                .map(ESIndicesGetTemplateResponse::getMultiTemplatesConfig).map(MultiTemplatesConfig::getSingleConfig)
                .orElse(new TemplateConfig());
       

       
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

        // 设置ES版本
        templateConfig.setVersion(client.getEsVersion());
        
        BiFunctionWithESOperateException<Long, TimeUnit, ESIndicesPutTemplateResponse> getESIndicesPutTemplateResponseBiFunction = (time, unit) -> {
            try {
                return client.admin().indices().preparePutTemplate(name).setTemplateConfig(templateConfig)
                        .execute().actionGet(time, unit);
            } catch (Exception e) {
                 final String exception = ParsingExceptionUtils.getESErrorMessageByException(e);
                if (Objects.nonNull(exception)) {
                    throw new ESOperateException(exception);
                }
                LOGGER.error("class=ESTemplateDAO||method=create||msg=put template fail||cluster={}||name={}", cluster,
                        name, e);
                throw new ESOperateException(e.getMessage());
            }
        
        };
    
        return Optional.ofNullable(
                        performTryTimesMethods(getESIndicesPutTemplateResponseBiFunction, Objects::isNull, 1))
                .map(ESIndicesPutTemplateResponse::getAcknowledged).orElse(false);
                
    }

    public boolean create(String cluster, String name, TemplateConfig templateConfig) throws ESOperateException{
        ESClient client = esOpClient.getESClient(cluster);
        if (client==null){
            LOGGER.warn("class=ESTemplateDAO||method=create||msg=es client is null ||cluster={}||name={}||templateConfig={}", cluster,
                name,templateConfig.toString());
            return Boolean.FALSE;
        }

        // 设置ES版本
        templateConfig.setVersion(client.getEsVersion());
        // 向ES中创建模板流程
        BiFunctionWithESOperateException<Long, TimeUnit, ESIndicesPutTemplateResponse> responseBiFunction = (time, unit) -> {
            try {
                return client.admin().indices().preparePutTemplate(name).setTemplateConfig(templateConfig).execute()
                        .actionGet(time, unit);
            
            } catch (Exception e) {
                final String exception = ParsingExceptionUtils.getESErrorMessageByException(e);
                if (Objects.nonNull(exception)) {
                    throw new ESOperateException(exception);
                }
                LOGGER.error("class=ESTemplateDAO||method=create||put templates fail||clusterName={}||templateName={}",
                        cluster, name, e);
                throw new ESOperateException("模板创建失败");
            }
        };
    
        return Optional.ofNullable(performTryTimesMethods(responseBiFunction, Objects::isNull, 1))
                .map(ESIndicesPutTemplateResponse::getAcknowledged).orElse(false);
    }

    /**
     * 更新模板配置
     * @param clusterName       集群名
     * @param templateName      模版名
     * @param templateConfig    模版配置
     * @return result
     */
    public boolean updateTemplate(String clusterName, String templateName, TemplateConfig templateConfig)
            throws ESOperateException {
        ESClient esClient = esOpClient.getESClient(clusterName);
        if (esClient == null) {
            LOGGER.warn(
                    "class=ESTemplateDAO||method=updateTemplate||update template  fail||clusterName={}||templateName={}||esVersion={}||templateConfig={}||msg=es client is null",
                    clusterName, templateName, templateConfig.toString());
            return Boolean.FALSE;
        }

        // 设置ES版本
        templateConfig.setVersion(esClient.getEsVersion());

        ESIndicesPutTemplateRequest request = new ESIndicesPutTemplateRequest();
        request.setTemplate(templateName);
        request.setTemplateConfig(templateConfig);

        ESIndicesPutTemplateResponse response = null;
        try {
            response = esClient.admin().indices().putTemplate(request).actionGet(120, TimeUnit.SECONDS);
        } catch (Exception e) {
            final String exception = ParsingExceptionUtils.getESErrorMessageByException(e);
            if (StringUtils.isNotBlank(exception)){
                 throw new ESOperateException(exception);
            }
            LOGGER.warn(
                "class=ESTemplateDAO||method=updateTemplate||update template fail||clusterName={}||templateName={}||esVersion={}||templateConfig={}||msg={}",
                clusterName, templateName, esClient.getEsVersion(),
                templateConfig.toJson(ESVersion.valueBy(esClient.getEsVersion())), e.getMessage(), e);
            throw new ESOperateException(e.getMessage(),e);
        }

        return response.getAcknowledged();
    }
    
    /**
     * 同步获取集群是正常
     *
     * @param clusterName 集群名称
     * @return boolean
     */
    public boolean syncGetClusterIsNormal(String clusterName) {
        return esOpClient.getESClient(clusterName) != null;
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
    public MappingConfig getTemplateMapping(String clusterName, String templateName) throws ESOperateException {
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

      

        ESClient esClient = esOpClient.getESClient(clusterName);

        if (null == esClient) {
            LOGGER.warn("class=ESTemplateDAO||method=getTemplates||clusterName={}||templateName={}||msg= es client is null",
                    clusterName,
            templateName);
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
    
        if (esClient == null) {
            LOGGER.error("class={}||method=delete||clusterName={}||templateName={}||errMsg=esClient is null",
                    getClass().getSimpleName(), clusterName, templateName);
            return null;
        }
        ESIndicesGetTemplateRequest request = new ESIndicesGetTemplateRequest();
        request.setTemplates(templateName);
    
        BiFunction<Long, TimeUnit, ESIndicesGetTemplateResponse> responseBiFunction = (time, unit) -> {
            try {
                return esClient.admin().indices().getTemplate(request).actionGet(time, unit);
            
            } catch (Exception e) {
                LOGGER.warn(
                        "class=ESTemplateDAO||method=getTemplates||get templates fail||clusterName={}||templateName={}||msg={}",
                        clusterName, templateName, e.getMessage(), e);
                return null;
            }
        };
        
        return performTryTimesMethods(responseBiFunction, Objects::isNull, tryTimes);
    }

    /**
     * 更新配置
     * @param cluster 集群
     * @param name    模板
     * @param setting setting
     * @return result
     */
    public boolean upsertSetting(String cluster, String name, Map<String, String> setting) throws ESOperateException {
        ESClient client = esOpClient.getESClient(cluster);
          if (client == null ) {
           
            LOGGER.warn("class={}||method=upsertSetting||cluster={}||name={}||errMsg=client is null ",
                    getClass().getSimpleName(), cluster, name);
            return false;
        }

        // 获取es中原来index template的配置
        ESIndicesGetTemplateResponse getTemplateResponse = client.admin().indices().prepareGetTemplate(name).execute()
            .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
        TemplateConfig templateConfig = getTemplateResponse.getMultiTemplatesConfig().getSingleConfig();

        for (Map.Entry<String, String> entry : setting.entrySet()) {
            templateConfig.setSettings(entry.getKey(), entry.getValue());
        }

        try {
            ESIndicesPutTemplateResponse response = client.admin().indices().preparePutTemplate(name)
                    .setTemplateConfig(templateConfig).execute().actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
            return response.getAcknowledged();
        } catch (Exception e) {
            final String exception = ParsingExceptionUtils.getESErrorMessageByException(e);
            if (StringUtils.isNotBlank(exception)) {
                throw new ESOperateException(exception);
            }
            throw new ESOperateException(e.getMessage(), e);
        }
       
    }
    
    
    /**
     * 它更新索引模板设置并检查分配包含名称。
     *
     * @param cluster 集群名称
     * @param name 索引模板名称
     * @param setting 索引模板的配置
     */
    public boolean updateSettingCheckAllocationAndShard(String cluster, String name,
                                                        Map<String, String> setting) throws ESOperateException {
        ESClient client = esOpClient.getESClient(cluster);
          if (client == null ) {
           
            LOGGER.warn("class={}||method=upsertSetting||cluster={}||name={}||errMsg=client is null ",
                    getClass().getSimpleName(), cluster, name);
            return false;
        }

        // 获取es中原来index template的配置
        ESIndicesGetTemplateResponse getTemplateResponse = client.admin().indices().prepareGetTemplate(name).execute()
            .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
        TemplateConfig templateConfig = getTemplateResponse.getMultiTemplatesConfig().getSingleConfig();
        final String indexRoutingAllocationIncludeName =
                templateConfig.getSetttings().get(ESSettingConstant.INDEX_ROUTING_ALLOCATION_INCLUDE_NAME);
        //index.routing.allocation.include._name
        if (!StringUtils.equals(setting.get(ESSettingConstant.INDEX_ROUTING_ALLOCATION_INCLUDE_NAME),
                indexRoutingAllocationIncludeName)) {
            throw new ESOperateException(
                    "模版分片分配节点配置属于系统权限，不允许变更 index.routing.allocation.include._name");
        }
        //shard
        if (!StringUtils.equals(setting.get(ESSettingConstant.INDEX_NUMBER_OF_SHARDS),
                indexRoutingAllocationIncludeName)) {
            throw new ESOperateException(
                    "模版设置 shard 大小设置属于系统权限, 非运维人员不允许变更 index.number_of_shards");
        
        }
        templateConfig.setSetttings(setting);
        

        try {
            ESIndicesPutTemplateResponse response = client.admin().indices().preparePutTemplate(name)
                    .setTemplateConfig(templateConfig).execute().actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
            return response.getAcknowledged();
        } catch (Exception e) {
            final String exception = ParsingExceptionUtils.getESErrorMessageByException(e);
            if (StringUtils.isNotBlank(exception)) {
                throw new ESOperateException(exception);
            }
            throw new ESOperateException(e.getMessage(), e);
        }
       
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
                                       String tgtTemplateName) throws ESOperateException {

        ESClient srcClient = esOpClient.getESClient(srcCluster);
        ESClient tgtClient = esOpClient.getESClient(tgtCluster);
        if (srcClient == null || tgtClient == null) {
            String msg = Objects.isNull(srcClient) && Objects.isNull(tgtClient)
                    ? String.format("{%s} and {%s} client is null", srcCluster, tgtCluster)
                    : Objects.isNull(srcClient)
                            ? String.format("{%s} client is null", srcCluster)
                            : String.format("{%s} client  is null", tgtCluster);
            LOGGER.warn("class={}||method=copyMappingAndAlias||srcCluster={}||tgtCluster={}||srcTemplateName={}||tgtTemplateName={}||errMsg={} ",
                    getClass().getSimpleName(), srcCluster, tgtCluster, srcTemplateName, tgtTemplateName,msg);
            return false;
        }
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
            final String exception = ParsingExceptionUtils.getESErrorMessageByException(e);
            if (StringUtils.isNotBlank(exception)){
                throw new ESOperateException(exception);
            }
            LOGGER.error(
                "class=ESTemplateDAO||method=copyMappingAndAlias||srcCluster={}||srcTemplateName={}||tgtCluster={}||tgtTemplateName={}",
                srcCluster, srcTemplateName, tgtCluster, tgtTemplateName, e);
        }

        return response.getAcknowledged();
    }
    
    
  
    /**
     * > 通过表达式模板健康枚举判断是否有匹配索引
     *
     * @param cluster 集群名称
     * @param expression 索引名称表达式，可以是通配符表达式，如“logstash-*”
     * @param templateHealthEnum 索引的状态，为枚举类，枚举类如下：
     */
    public boolean hasMatchHealthIndexByExpressionTemplateHealthEnum(String cluster, String expression,
                                                                     TemplateHealthEnum templateHealthEnum) throws ESOperateException{
        ESClient client = esOpClient.getESClient(cluster);
        if (client == null) {
            return false;
        }
        DirectResponse response = getDirectResponseByClusterAndUrl(client,
                String.format(CAT_INDIES_HEALTH, expression, templateHealthEnum.getDesc()));
    
        if (templateHealthEnum.equals(TemplateHealthEnum.GREEN)) {
            DirectResponse directResponse = getDirectResponseByClusterAndUrl(client,
                    String.format(CAT_INDIES, expression));
            return (response.getRestStatus() == RestStatus.OK && StringUtils.isNotBlank(response.getResponseContent()))
                   || (directResponse.getRestStatus() == RestStatus.OK && StringUtils.isEmpty(
                    directResponse.getResponseContent()));
        }
        return (response.getRestStatus() == RestStatus.OK && StringUtils.isNotBlank(response.getResponseContent()));
    }
  
    /**
     * > 通过集群和url获取模板的健康度
     *
     * @param client 用于连接 ES 集群的客户端对象。
     * @param uri    请求地址，即模板的地址。
     */
    private DirectResponse getDirectResponseByClusterAndUrl(ESClient client, String uri) throws ESOperateException {
        DirectRequest directRequest = new DirectRequest(RequestMethod.GET.name(), uri);
        try {
            return client.direct(directRequest).actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) {
            String exception = ParsingExceptionUtils.getESErrorMessageByException(e);
            if (Objects.nonNull(exception)) {
                throw new ESOperateException(exception);
            }
            throw new ESOperateException(e.getMessage(), e);
        }
    
    }
}