package com.didichuxing.datachannel.arius.admin.biz.template.srv.aliases;

import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyAlias;

import java.util.List;
import java.util.Map;

/**
 * 物理模板别名服务类
 * @author wangshu
 * @date 2020/08/24
 */
public interface TemplatePhyAliasManager {
    /**
     * 获取模板别名列表
     * @param cluster 集群名称
     * @param templateName 模板名称
     * @return
     */
    List<IndexTemplatePhyAlias> fetchTemplateAliases(String cluster, String templateName) throws ESOperateException;

    /**
     * 获取所有模板别名列表
     * @param clusters 集群名
     * @return
     * @throws ESOperateException
     */
    Map<String, List<IndexTemplatePhyAlias>> fetchAllTemplateAliases(List<String> clusters) throws ESOperateException;

    /**
     * 创建模板别名
     * @param cluster 集群名称
     * @param templateName 模板名称
     * @param templateAlias 模板别名
     * @return
     */
    boolean createTemplateAlias(String cluster, String templateName,
                                IndexTemplatePhyAlias templateAlias) throws ESOperateException;

    /**
     * 创建模板别名
     * @param cluster 集群名称
     * @param templateName 模板名称
     * @param templateAliases 模板别名
     * @return
     */
    boolean batchCreateTemplateAliases(String cluster, String templateName,
                                       List<IndexTemplatePhyAlias> templateAliases) throws ESOperateException;

    /**
     * 删除模板别名
     * @param cluster 集群名称
     * @param templateName 模板名称
     * @param alias 别名名称
     * @return
     * @throws ESOperateException
     */
    boolean deleteTemplateAlias(String cluster, String templateName, String alias) throws ESOperateException;

    /**
     * 删除模板别名列表
     * @param cluster 集群名称
     * @param templateName 模板名称
     * @param aliases 别名列表
     * @return
     * @throws ESOperateException
     */
    boolean deleteTemplateAliases(String cluster, String templateName, List<String> aliases) throws ESOperateException;

    /**
     * 删除物理模板所有别名
     * @param cluster 集群名称
     * @param templateName 模板名称
     * @return
     * @throws ESOperateException
     */
    boolean clearTemplateAliases(String cluster, String templateName) throws ESOperateException;

    /**
     * 更新模板别名信息
     * @param cluster 集群名称
     * @param templateName 模板名称
     * @param templateAlias 模板别名信息
     * @return
     * @throws ESOperateException
     */
    boolean modifyTemplateAlias(String cluster, String templateName,
                                IndexTemplatePhyAlias templateAlias) throws ESOperateException;

    /**
     * 更新模板别名信息
     * @param cluster 集群名称
     * @param templateName 模板名称
     * @param templateAliases 模板别名信息
     * @return
     * @throws ESOperateException
     */
    boolean modifyTemplateAliases(String cluster, String templateName,
                                  List<IndexTemplatePhyAlias> templateAliases) throws ESOperateException;

}
