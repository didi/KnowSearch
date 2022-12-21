package com.didichuxing.datachannel.arius.admin.biz.template.srv.aliases.impl;

import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.aliases.TemplatePhyAliasManager;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyAlias;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyAliases;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminESOpRetryConstants;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.impl.IndexTemplateServiceImpl;
import com.didiglobal.knowframework.elasticsearch.client.response.setting.template.TemplateConfig;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 模板别名服务
 * @author wangshu
 * @date 2020/08/24
 */
@Service
public class TemplatePhyAliasManagerImpl implements TemplatePhyAliasManager {

    private static final ILog Logger = LogFactory.getLog(IndexTemplateServiceImpl.class);

    @Autowired
    private ESTemplateService esTemplateService;

    /**
     * 获取模板别名列表
     * @param cluster 集群名称
     * @param templateName 模板名称
     * @return
     */
    @Override
    public List<IndexTemplatePhyAlias> fetchTemplateAliases(String cluster,
                                                            String templateName) throws ESOperateException {
        TemplateConfig templateConfig = fetchTemplateConfig(cluster, templateName);

        IndexTemplatePhyAliases templateAliases = new IndexTemplatePhyAliases(templateConfig.getAliases());
        return templateAliases.parseTemplateAliases();
    }

    /**
     * 获取所有模板别名列表
     * @param clusters 集群名
     * @return
     * @throws ESOperateException
     */
    @Override
    public Map<String, List<IndexTemplatePhyAlias>> fetchAllTemplateAliases(List<String> clusters) throws ESOperateException {
        Map<String, TemplateConfig> templateConfigMap = fetchAllTemplateConfig(clusters);

        Map<String, List<IndexTemplatePhyAlias>> templatePhyAliasMap = Maps.newHashMap();

        templateConfigMap.forEach((x, y) -> {
            IndexTemplatePhyAliases templateAliases = new IndexTemplatePhyAliases(y.getAliases());
            templatePhyAliasMap.put(x, templateAliases.parseTemplateAliases());
        });

        return templatePhyAliasMap;
    }

    /**
     * 创建模板别名
     * @param cluster 集群名称
     * @param templateName 模板名称
     * @param templateAlias 模板别名
     * @return
     */
    @Override
    public boolean createTemplateAlias(String cluster, String templateName,
                                       IndexTemplatePhyAlias templateAlias) throws ESOperateException {
        TemplateConfig templateConfig = fetchTemplateConfig(cluster, templateName);
        IndexTemplatePhyAliases aliases = new IndexTemplatePhyAliases(templateConfig.getAliases());
        if (!aliases.isAliasExists(templateAlias.getAlias())) {
            aliases.putAlias(templateAlias.getAlias(), templateAlias.getFilter());
            templateConfig.setAliases(aliases.getAliases());

            return esTemplateService.syncUpdateTemplateConfig(cluster, templateName, templateConfig,
                AdminESOpRetryConstants.DEFAULT_RETRY_COUNT);
        } else {
            throw new ESOperateException("别名已经存在，无法新建");
        }
    }

    @Override
    public boolean batchCreateTemplateAliases(String cluster, String templateName,
                                              List<IndexTemplatePhyAlias> templateAliases) throws ESOperateException {
        TemplateConfig templateConfig = fetchTemplateConfig(cluster, templateName);
        if (CollectionUtils.isNotEmpty(templateAliases)) {
            IndexTemplatePhyAliases aliases = new IndexTemplatePhyAliases(templateConfig.getAliases());
            for (IndexTemplatePhyAlias alias : templateAliases) {
                aliases.putAlias(alias.getAlias(), alias.getFilter());
                templateConfig.setAliases(aliases.getAliases());
            }

            return esTemplateService.syncUpdateTemplateConfig(cluster, templateName, templateConfig,
                AdminESOpRetryConstants.DEFAULT_RETRY_COUNT);
        }

        throw new ESOperateException("模板别名列表不能为空");
    }

    /**
     * 删除模板别名
     * @param cluster 集群名称
     * @param templateName 模板名称
     * @param alias 别名名称
     * @return
     * @throws ESOperateException
     */
    @Override
    public boolean deleteTemplateAlias(String cluster, String templateName, String alias) throws ESOperateException {

        TemplateConfig templateConfig = fetchTemplateConfig(cluster, templateName);

        IndexTemplatePhyAliases aliases = new IndexTemplatePhyAliases(templateConfig.getAliases());
        if (StringUtils.isNotBlank(alias) && aliases.isAliasExists(alias)) {
            aliases.removeAlias(alias);
            templateConfig.setAliases(aliases.getAliases());

            return esTemplateService.syncUpdateTemplateConfig(cluster, templateName, templateConfig,
                AdminESOpRetryConstants.DEFAULT_RETRY_COUNT);
        }

        return true;
    }

    /**
     * 删除模板别名列表
     * @param cluster 集群名称
     * @param templateName 模板名称
     * @param aliases 别名列表
     * @return
     * @throws ESOperateException
     */
    @Override
    public boolean deleteTemplateAliases(String cluster, String templateName,
                                         List<String> aliases) throws ESOperateException {
        if (CollectionUtils.isEmpty(aliases)) {
            throw new ESOperateException("待删除模板别名列表不能为空");
        }

        TemplateConfig templateConfig = fetchTemplateConfig(cluster, templateName);
        IndexTemplatePhyAliases physicalAliases = new IndexTemplatePhyAliases(templateConfig.getAliases());
        for (String alias : aliases) {
            physicalAliases.removeAlias(alias);
        }

        templateConfig.setAliases(physicalAliases.getAliases());
        return esTemplateService.syncUpdateTemplateConfig(cluster, templateName, templateConfig,
            AdminESOpRetryConstants.DEFAULT_RETRY_COUNT);
    }

    /**
     * 删除物理模板所有别名
     * @param cluster 集群名称
     * @param templateName 模板名称
     * @return
     * @throws ESOperateException
     */
    @Override
    public boolean clearTemplateAliases(String cluster, String templateName) throws ESOperateException {

        TemplateConfig templateConfig = fetchTemplateConfig(cluster, templateName);
        templateConfig.setAliases(new JSONObject());
        return esTemplateService.syncUpdateTemplateConfig(cluster, templateName, templateConfig,
            AdminESOpRetryConstants.DEFAULT_RETRY_COUNT);
    }

    /**
     * 更新模板别名信息
     * @param cluster 集群名称
     * @param templateName 模板名称
     * @param templateAlias 模板别名信息
     * @return
     * @throws ESOperateException
     */
    @Override
    public boolean modifyTemplateAlias(String cluster, String templateName,
                                       IndexTemplatePhyAlias templateAlias) throws ESOperateException {
        if (isValidAlias(cluster, templateName, templateAlias)) {
            TemplateConfig templateConfig = fetchTemplateConfig(cluster, templateName);
            IndexTemplatePhyAliases aliases = new IndexTemplatePhyAliases(templateConfig.getAliases());
            if (aliases.isAliasExists(templateAlias.getAlias())) {
                aliases.putAlias(templateAlias.getAlias(), templateAlias.getFilter());
                templateConfig.setAliases(aliases.getAliases());
                return esTemplateService.syncUpdateTemplateConfig(cluster, templateName, templateConfig,
                    AdminESOpRetryConstants.DEFAULT_RETRY_COUNT);
            }

        }

        return false;
    }

    /**
     * 批量变更模板别名
     * @param cluster 集群名称
     * @param templateName 模板名称
     * @param templateAliases 模板别名信息
     * @return
     * @throws ESOperateException
     */
    @Override
    public boolean modifyTemplateAliases(String cluster, String templateName,
                                         List<IndexTemplatePhyAlias> templateAliases) throws ESOperateException {
        TemplateConfig templateConfig = fetchTemplateConfig(cluster, templateName);
        if (CollectionUtils.isEmpty(templateAliases)) {
            throw new ESOperateException("非法的参数");
        }

        IndexTemplatePhyAliases aliases = new IndexTemplatePhyAliases(templateConfig.getAliases());
        boolean isAliasesChanged = false;
        for (IndexTemplatePhyAlias physicalAlias : templateAliases) {
            if (aliases.isAliasExists(physicalAlias.getAlias())) {
                aliases.putAlias(physicalAlias.getAlias(), physicalAlias.getFilter());
                isAliasesChanged = true;
            }
        }

        if (isAliasesChanged) {
            templateConfig.setAliases(aliases.getAliases());
            return esTemplateService.syncUpdateTemplateConfig(cluster, templateName, templateConfig,
                AdminESOpRetryConstants.DEFAULT_RETRY_COUNT);
        }

        return true;
    }

    /**************************************** private method ****************************************************/
    /**
     * Check 是否是合法的别名
     * @param cluster 集群名称
     * @param templateName 模板名称
     * @param templateAlias 模板别名
     * @return
     */
    private boolean isValidAlias(String cluster, String templateName, IndexTemplatePhyAlias templateAlias) {
        if (StringUtils.isBlank(cluster) || templateAlias == null || StringUtils.isBlank(templateName)
            || StringUtils.isBlank(templateAlias.getAlias())) {
            return false;
        }

        return true;
    }

    /**
     * 获取模板配置
     * @param cluster 集群名称
     * @param templateName 模板名称
     * @return
     * @throws ESOperateException
     */
    private TemplateConfig fetchTemplateConfig(String cluster, String templateName) throws ESOperateException {
        if (StringUtils.isBlank(cluster) || StringUtils.isBlank(templateName)) {
            throw new ESOperateException("非法请求");
        }

        TemplateConfig templateConfig = esTemplateService.syncGetTemplateConfig(cluster, templateName);
        if (templateConfig == null) {
            Logger.info("class=TemplatePhyAliasesManagerImpl||method=fetchTemplateConfig||"
                        + "msg=templateNotFound||cluster={}||template={}",
                cluster, templateName);
            throw new ESOperateException("模板配置不存在");
        }

        return templateConfig;
    }

    private Map<String, TemplateConfig> fetchAllTemplateConfig(List<String> clusters) throws ESOperateException {
        if (CollectionUtils.isEmpty(clusters)) {
            throw new ESOperateException("非法请求");
        }

        Map<String, TemplateConfig> templateConfigMap = esTemplateService.syncGetAllTemplates(clusters);
        if (templateConfigMap == null) {
            Logger.info("class=TemplatePhyAliasesManagerImpl||method=fetchAllTemplateConfig||"
                        + "msg=templateNotFound||cluster={}",
                clusters);
            throw new ESOperateException("模板配置不存在");
        }

        return templateConfigMap;
    }
}